package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.ProjectMemberDTO;
import com.PracticalAssignment.assignP.dto.ProjectDTO;
import com.PracticalAssignment.assignP.model.Project;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.ProjectRepository;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
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

    public List<ProjectDTO> getAllProjects() {
        User currentUser = getCurrentUser();
        List<Project> projects;
        
        if (isAdmin(currentUser)) {
            // Admin sees all projects
            projects = projectRepository.findAll();
        } else {
            // Member sees only their owned or assigned projects
            projects = projectRepository.findAll().stream()
                    .filter(p -> p.getOwner().getId().equals(currentUser.getId()) ||
                       p.getMembers().stream()
                           .anyMatch(member -> member.getId().equals(currentUser.getId())))
                    .collect(Collectors.toList());
        }
        
        return projects.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO createProject(ProjectDTO dto) {
        User owner = getCurrentUser();

        Project project = new Project();
        project.setName(dto.getName());
        project.setOwner(owner);
        project.getMembers().add(owner); // Owner is automatically a member
        
        Project saved = projectRepository.save(project);
        return toDTO(saved);
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        
        if (!canAccessProject(project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this project");
        }
        
        return toDTO(project);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId()) && 
            !isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can delete project");
        }
        
        projectRepository.deleteById(id);
    }

    public ProjectDTO addMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId()) && 
            !isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can add members");
        }
        
        User memberToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        
        project.getMembers().add(memberToAdd);
        projectRepository.save(project);
        
        return toDTO(project);
    }

    public ProjectDTO removeMemberFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner or admin can remove members");
        }

        if (project.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project owner cannot be removed from members");
        }

        boolean removed = project.getMembers().removeIf(member -> member.getId().equals(userId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a project member");
        }

        projectRepository.save(project);
        return toDTO(project);
    }

    private ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());

        if (project.getOwner() != null) {
            dto.setOwnerId(project.getOwner().getId());
            dto.setOwnerUsername(project.getOwner().getUsername());
        }

        List<ProjectMemberDTO> members = project.getMembers().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(member -> {
                    ProjectMemberDTO memberDTO = new ProjectMemberDTO();
                    memberDTO.setId(member.getId());
                    memberDTO.setUsername(member.getUsername());
                    memberDTO.setRole(member.getRole());
                    memberDTO.setOwner(project.getOwner() != null && project.getOwner().getId().equals(member.getId()));
                    return memberDTO;
                })
                .collect(Collectors.toList());
        dto.setMembers(members);
        return dto;
    }
}
