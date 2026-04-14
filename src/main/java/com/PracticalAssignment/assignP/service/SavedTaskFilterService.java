package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.SavedTaskFilterDTO;
import com.PracticalAssignment.assignP.model.SavedTaskFilter;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.SavedTaskFilterRepository;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedTaskFilterService {

    private final SavedTaskFilterRepository savedTaskFilterRepository;
    private final UserRepository userRepository;

    public SavedTaskFilterService(SavedTaskFilterRepository savedTaskFilterRepository,
                                  UserRepository userRepository) {
        this.savedTaskFilterRepository = savedTaskFilterRepository;
        this.userRepository = userRepository;
    }

    public List<SavedTaskFilterDTO> getMyFilters() {
        User currentUser = getCurrentUser();
        return savedTaskFilterRepository.findByOwnerIdOrderByIdDesc(currentUser.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SavedTaskFilterDTO saveFilter(SavedTaskFilterDTO dto) {
        User currentUser = getCurrentUser();
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filter name is required");
        }

        SavedTaskFilter filter = new SavedTaskFilter();
        filter.setOwner(currentUser);
        filter.setName(dto.getName().trim());
        filter.setProjectIds(joinLongs(dto.getProjectIds()));
        filter.setUserIds(joinLongs(dto.getUserIds()));
        filter.setStatuses(joinStrings(dto.getStatuses()));
        filter.setQuery(dto.getQuery() == null ? null : dto.getQuery().trim());

        return toDTO(savedTaskFilterRepository.save(filter));
    }

    public void deleteFilter(Long id) {
        User currentUser = getCurrentUser();
        SavedTaskFilter filter = savedTaskFilterRepository.findByIdAndOwnerId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved filter not found"));
        savedTaskFilterRepository.delete(filter);
    }

    private SavedTaskFilterDTO toDTO(SavedTaskFilter filter) {
        SavedTaskFilterDTO dto = new SavedTaskFilterDTO();
        dto.setId(filter.getId());
        dto.setName(filter.getName());
        dto.setProjectIds(parseLongs(filter.getProjectIds()));
        dto.setUserIds(parseLongs(filter.getUserIds()));
        dto.setStatuses(parseStrings(filter.getStatuses()));
        dto.setQuery(filter.getQuery());
        return dto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String joinLongs(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String joinStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(String::trim).filter(v -> !v.isBlank()).collect(Collectors.joining(","));
    }

    private List<Long> parseLongs(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private List<String> parseStrings(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toList());
    }
}
