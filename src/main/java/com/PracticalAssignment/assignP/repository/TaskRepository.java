package com.PracticalAssignment.assignP.repository;

import com.PracticalAssignment.assignP.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {}
