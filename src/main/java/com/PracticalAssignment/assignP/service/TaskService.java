package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.TaskActivityDTO;
import com.PracticalAssignment.assignP.dto.TaskCommentDTO;
import com.PracticalAssignment.assignP.dto.TaskDetailsDTO;
import com.PracticalAssignment.assignP.dto.TaskDTO;
import com.PracticalAssignment.assignP.model.Project;
import com.PracticalAssignment.assignP.model.TaskActivity;
import com.PracticalAssignment.assignP.model.TaskComment;
import com.PracticalAssignment.assignP.model.Task;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.ProjectRepository;
import com.PracticalAssignment.assignP.repository.TaskActivityRepository;
import com.PracticalAssignment.assignP.repository.TaskCommentRepository;
import com.PracticalAssignment.assignP.repository.TaskRepository;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       TaskCommentRepository taskCommentRepository,
                       TaskActivityRepository taskActivityRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskActivityRepository = taskActivityRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAdmin(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String normalizedRole = user.getRole().trim().toUpperCase();
        return normalizedRole.equals("ADMIN") || normalizedRole.equals("ROLE_ADMIN");
    }

    private boolean canAccessProject(Project project) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) {
            return true; // Admin can access all projects
        }
        // Member can only access projects they own or are members of
        return project.getOwner().getId().equals(currentUser.getId()) ||
               project.getMembers().stream()
                       .anyMatch(member -> member.getId().equals(currentUser.getId()));
    }

    public List<TaskDTO> getAllTasks() {
        return getTasksAdvanced(null, null, null, null);
    }

    public List<TaskDTO> getTasks(Long projectId, Long userId, String status) {
        List<Long> projectIds = projectId != null ? List.of(projectId) : null;
        List<Long> userIds = userId != null ? List.of(userId) : null;
        List<String> statuses = (status != null && !status.isBlank()) ? List.of(status) : null;
        return getTasksAdvanced(projectIds, userIds, statuses, null);
        }

        public List<TaskDTO> getTasksAdvanced(List<Long> projectIds,
                          List<Long> userIds,
                          List<String> statuses,
                          String query) {
        User currentUser = getCurrentUser();
        Set<String> normalizedStatuses = statuses == null ? null : statuses.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(value -> normalizeStatus(value))
            .collect(Collectors.toSet());
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        
        return taskRepository.findAll().stream()
            .filter(t -> projectIds == null || projectIds.isEmpty() || projectIds.contains(t.getProject().getId()))
            .filter(t -> userIds == null || userIds.isEmpty() ||
                (t.getAssignee() != null && userIds.contains(t.getAssignee().getId())))
            .filter(t -> normalizedStatuses == null || normalizedStatuses.isEmpty() || normalizedStatuses.contains(t.getStatus()))
            .filter(t -> normalizedQuery.isBlank() ||
                (t.getDescription() != null && t.getDescription().toLowerCase(Locale.ROOT).contains(normalizedQuery)) ||
                (t.getProject() != null && t.getProject().getName() != null &&
                    t.getProject().getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) ||
                (t.getAssignee() != null && t.getAssignee().getUsername() != null &&
                    t.getAssignee().getUsername().toLowerCase(Locale.ROOT).contains(normalizedQuery)))
            .filter(t -> isAdmin(currentUser) || canAccessProject(t.getProject()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO createTask(TaskDTO dto) {
        if (dto.getProjectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectId is required");
        }

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found"));

        if (!canAccessProject(project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project");
        }

        Task task = new Task();
        task.setDescription(dto.getDescription() != null ? dto.getDescription() : dto.getTitle());
        task.setStatus(normalizeStatus(dto.getStatus()));
        task.setDueDate(dto.getDueDate());
        task.setProject(project);

        if (dto.getUserId() != null && dto.getUserId() > 0) {
            User assignee = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
            task.setAssignee(assignee);
        }

        User actor = getCurrentUser();
        Task savedTask = taskRepository.save(task);
        logActivity(savedTask, actor, "CREATED", "Task created");
        notificationService.notifyAssignment(savedTask, actor, savedTask.getAssignee());
        return toDTO(savedTask);
    }

    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Long previousAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        if (!canAccessProject(task.getProject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project");
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            task.setDescription(dto.getDescription().trim());
        }
        if (dto.getStatus() != null) {
            task.setStatus(normalizeStatus(dto.getStatus()));
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found"));
            if (!canAccessProject(project)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to the target project");
            }
            task.setProject(project);
        }
        if (dto.getUserId() != null) {
            if (dto.getUserId() <= 0) {
                task.setAssignee(null);
            } else {
                User assignee = userRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                task.setAssignee(assignee);
            }
        }

        User actor = getCurrentUser();
        Task savedTask = taskRepository.save(task);
        logActivity(savedTask, actor, "UPDATED", "Task updated");
        Long currentAssigneeId = savedTask.getAssignee() != null ? savedTask.getAssignee().getId() : null;
        if (currentAssigneeId != null && !currentAssigneeId.equals(previousAssigneeId)) {
            notificationService.notifyAssignment(savedTask, actor, savedTask.getAssignee());
        }
        return toDTO(savedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!canAccessProject(task.getProject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this project");
        }

        logActivity(task, getCurrentUser(), "DELETED", "Task deleted");
        taskRepository.deleteById(id);
    }

    public TaskDetailsDTO getTaskDetails(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!canAccessProject(task.getProject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this task");
        }

        TaskDetailsDTO detailsDTO = new TaskDetailsDTO();
        detailsDTO.setTask(toDTO(task));
        detailsDTO.setComments(taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(id).stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList()));
        detailsDTO.setActivity(taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(id).stream()
                .map(this::toActivityDTO)
                .collect(Collectors.toList()));
        return detailsDTO;
    }

    public TaskCommentDTO addComment(Long taskId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content is required");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!canAccessProject(task.getProject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this task");
        }

        User currentUser = getCurrentUser();
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(currentUser);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());

        TaskComment savedComment = taskCommentRepository.save(comment);
        logActivity(task, currentUser, "COMMENTED", "Added a comment");
        notificationService.notifyMentions(task, comment.getContent(), currentUser);
        return toCommentDTO(savedComment);
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setProjectName(task.getProject() != null ? task.getProject().getName() : null);
        dto.setUserId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        dto.setAssigneeUsername(task.getAssignee() != null ? task.getAssignee().getUsername() : null);
        dto.setDueDate(task.getDueDate());
        return dto;
    }

    private TaskCommentDTO toCommentDTO(TaskComment comment) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(comment.getId());
        dto.setTaskId(comment.getTask() != null ? comment.getTask().getId() : null);
        dto.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        dto.setAuthorUsername(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null);
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    private TaskActivityDTO toActivityDTO(TaskActivity activity) {
        TaskActivityDTO dto = new TaskActivityDTO();
        dto.setId(activity.getId());
        dto.setTaskId(activity.getTask() != null ? activity.getTask().getId() : null);
        dto.setActorUsername(activity.getActor() != null ? activity.getActor().getUsername() : "System");
        dto.setType(activity.getType());
        dto.setMessage(activity.getMessage());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }

    private void logActivity(Task task, User actor, String type, String message) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setActor(actor);
        activity.setType(type);
        activity.setMessage(message);
        activity.setCreatedAt(LocalDateTime.now());
        taskActivityRepository.save(activity);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "TODO";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("TODO") && !normalized.equals("IN_PROGRESS") && !normalized.equals("DONE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be TODO, IN_PROGRESS or DONE");
        }
        return normalized;
    }
}
