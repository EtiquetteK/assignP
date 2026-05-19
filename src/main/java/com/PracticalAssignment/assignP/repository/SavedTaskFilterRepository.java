package com.PracticalAssignment.assignP.repository;

import com.PracticalAssignment.assignP.model.SavedTaskFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedTaskFilterRepository extends JpaRepository<SavedTaskFilter, Long> {
    List<SavedTaskFilter> findByOwnerIdOrderByIdDesc(Long ownerId);
    Optional<SavedTaskFilter> findByIdAndOwnerId(Long id, Long ownerId);
}
