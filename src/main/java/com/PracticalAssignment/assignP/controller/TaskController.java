package com.PracticalAssignment.assignP.controller;

import com.PracticalAssignment.assignP.dto.TaskCommentCreateRequest;
import com.PracticalAssignment.assignP.dto.TaskCommentDTO;
import com.PracticalAssignment.assignP.dto.TaskDetailsDTO;
import org.springframework.web.bind.annotation.*;
import com.PracticalAssignment.assignP.dto.TaskDTO;
import com.PracticalAssignment.assignP.service.TaskService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDTO> getAllTasks(@RequestParam(required = false) Long projectId,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String projectIds,
                                     @RequestParam(required = false) String userIds,
                                     @RequestParam(required = false) String statuses,
                                     @RequestParam(required = false) String query) {
        if (projectIds != null || userIds != null || statuses != null || (query != null && !query.isBlank())) {
            return taskService.getTasksAdvanced(parseLongCsv(projectIds), parseLongCsv(userIds), parseStringCsv(statuses), query);
        }
        if (projectId == null && userId == null && (status == null || status.isBlank())) {
            return taskService.getAllTasks();
        }
        return taskService.getTasks(projectId, userId, status);
    }

    @GetMapping("/{id}/details")
    public TaskDetailsDTO getTaskDetails(@PathVariable Long id) {
        return taskService.getTaskDetails(id);
    }

    @PostMapping("/{id}/comments")
    public TaskCommentDTO addComment(@PathVariable Long id, @RequestBody TaskCommentCreateRequest request) {
        return taskService.addComment(id, request.getContent());
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO dto) {
        return taskService.createTask(dto);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody TaskDTO dto) {
        return taskService.updateTask(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    private List<Long> parseLongCsv(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private List<String> parseStringCsv(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
