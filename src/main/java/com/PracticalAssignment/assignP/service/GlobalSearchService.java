package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.GlobalSearchResultDTO;
import com.PracticalAssignment.assignP.dto.ProjectDTO;
import com.PracticalAssignment.assignP.dto.TaskDTO;
import com.PracticalAssignment.assignP.dto.UserDTO;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GlobalSearchService {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public GlobalSearchService(TaskService taskService,
                               ProjectService projectService,
                               UserRepository userRepository) {
        this.taskService = taskService;
        this.projectService = projectService;
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

    public GlobalSearchResultDTO globalSearch(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        GlobalSearchResultDTO result = new GlobalSearchResultDTO();
        result.setQuery(query == null ? "" : query.trim());

        if (normalizedQuery.isBlank()) {
            return result;
        }

        List<TaskDTO> matchedTasks = taskService.getTasksAdvanced(null, null, null, normalizedQuery);
        List<ProjectDTO> matchedProjects = projectService.getAllProjects().stream()
                .filter(project -> project.getName() != null
                        && project.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .limit(8)
                .collect(Collectors.toList());

        User currentUser = getCurrentUser();
        Set<Long> visibleUserIds = projectService.getAllProjects().stream()
                .flatMap(project -> project.getMembers().stream())
                .map(member -> member.getId())
                .collect(Collectors.toSet());
        visibleUserIds.add(currentUser.getId());

        List<UserDTO> matchedUsers = userRepository.findAll().stream()
                .filter(user -> user.getUsername() != null
                        && user.getUsername().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .filter(user -> isAdmin(currentUser) || visibleUserIds.contains(user.getId()))
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setRole(user.getRole());
                    return dto;
                })
                .limit(8)
                .collect(Collectors.toList());

        result.setTasks(matchedTasks.stream().limit(8).collect(Collectors.toList()));
        result.setProjects(matchedProjects);
        result.setUsers(matchedUsers);
        return result;
    }
}
