package com.PracticalAssignment.assignP.controller;

import com.PracticalAssignment.assignP.dto.GlobalSearchResultDTO;
import com.PracticalAssignment.assignP.service.GlobalSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final GlobalSearchService globalSearchService;

    public SearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    @GetMapping("/global")
    public GlobalSearchResultDTO globalSearch(@RequestParam String q) {
        return globalSearchService.globalSearch(q);
    }
}
