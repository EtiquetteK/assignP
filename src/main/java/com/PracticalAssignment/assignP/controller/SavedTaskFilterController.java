package com.PracticalAssignment.assignP.controller;

import com.PracticalAssignment.assignP.dto.SavedTaskFilterDTO;
import com.PracticalAssignment.assignP.service.SavedTaskFilterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-filters")
public class SavedTaskFilterController {

    private final SavedTaskFilterService savedTaskFilterService;

    public SavedTaskFilterController(SavedTaskFilterService savedTaskFilterService) {
        this.savedTaskFilterService = savedTaskFilterService;
    }

    @GetMapping
    public List<SavedTaskFilterDTO> getMyFilters() {
        return savedTaskFilterService.getMyFilters();
    }

    @PostMapping
    public SavedTaskFilterDTO saveFilter(@RequestBody SavedTaskFilterDTO dto) {
        return savedTaskFilterService.saveFilter(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteFilter(@PathVariable Long id) {
        savedTaskFilterService.deleteFilter(id);
    }
}
