package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.NotificationDTO;
import com.PracticalAssignment.assignP.model.Notification;
import com.PracticalAssignment.assignP.model.Project;
import com.PracticalAssignment.assignP.model.Task;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.NotificationRepository;
import com.PracticalAssignment.assignP.repository.TaskRepository;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_.-]+)");

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               TaskRepository taskRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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

    private boolean canAccessProject(User currentUser, Project project) {
        if (isAdmin(currentUser)) {
            return true;
        }
        return project.getOwner().getId().equals(currentUser.getId())
                || project.getMembers().stream().anyMatch(member -> member.getId().equals(currentUser.getId()));
    }

    public List<NotificationDTO> getMyNotifications(boolean unreadOnly) {
        User currentUser = getCurrentUser();

        List<NotificationDTO> persisted = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        List<NotificationDTO> dueAlerts = buildDueDateNotifications(currentUser);

        List<NotificationDTO> all = new ArrayList<>();
        all.addAll(persisted);
        all.addAll(dueAlerts);

        all.sort(Comparator.comparing(NotificationDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        if (!unreadOnly) {
            return all;
        }
        return all.stream().filter(dto -> !dto.isRead()).collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void notifyAssignment(Task task, User actor, User assignee) {
        if (task == null || assignee == null) {
            return;
        }
        if (actor != null && assignee.getId().equals(actor.getId())) {
            return;
        }

        String actorName = actor != null ? actor.getUsername() : "System";
        String projectName = task.getProject() != null ? task.getProject().getName() : "Project";

        Notification notification = new Notification();
        notification.setRecipient(assignee);
        notification.setTask(task);
        notification.setType("ASSIGNMENT");
        notification.setMessage(actorName + " assigned you task #" + task.getId() + " in " + projectName);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public void notifyMentions(Task task, String content, User actor) {
        if (task == null || content == null || content.isBlank()) {
            return;
        }

        Set<String> usernames = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }

        if (usernames.isEmpty()) {
            return;
        }

        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(recipient -> {
                if (actor != null && recipient.getId().equals(actor.getId())) {
                    return;
                }
                if (!canAccessProject(recipient, task.getProject())) {
                    return;
                }

                Notification notification = new Notification();
                notification.setRecipient(recipient);
                notification.setTask(task);
                notification.setType("MENTION");
                notification.setMessage((actor != null ? actor.getUsername() : "Someone")
                        + " mentioned you on task #" + task.getId());
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                notificationRepository.save(notification);
            });
        }
    }

    private List<NotificationDTO> buildDueDateNotifications(User currentUser) {
        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(3);

        return taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> task.getProject() != null)
                .filter(task -> canAccessProject(currentUser, task.getProject()))
                .filter(task -> isAdmin(currentUser)
                        || (task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId())))
                .filter(task -> !"DONE".equalsIgnoreCase(task.getStatus()))
                .filter(task -> !task.getDueDate().isAfter(soonThreshold))
                .map(task -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setType(task.getDueDate().isBefore(today) ? "OVERDUE" : "DUE_SOON");
                    dto.setTaskId(task.getId());
                    dto.setRead(false);
                    dto.setCreatedAt(task.getDueDate().atStartOfDay());
                    if (task.getDueDate().isBefore(today)) {
                        dto.setMessage("Task #" + task.getId() + " is overdue since " + task.getDueDate());
                    } else {
                        dto.setMessage("Task #" + task.getId() + " is due soon on " + task.getDueDate());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());
        dto.setTaskId(notification.getTask() != null ? notification.getTask().getId() : null);
        return dto;
    }
}
