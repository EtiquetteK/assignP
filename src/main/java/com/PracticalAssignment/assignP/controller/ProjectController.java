package com.PracticalAssignment.assignP.controller;

import org.springframework.web.bind.annotation.*;
import com.PracticalAssignment.assignP.dto.ProjectDTO;
import com.PracticalAssignment.assignP.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectDTO> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public ProjectDTO getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping
    public ProjectDTO createProject(@RequestBody ProjectDTO dto) {
        return projectService.createProject(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ProjectDTO addMemberToProject(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectService.addMemberToProject(projectId, userId);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ProjectDTO removeMemberFromProject(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectService.removeMemberFromProject(projectId, userId);
    }
}
