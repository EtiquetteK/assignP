package com.PracticalAssignment.assignP.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private String status; // could map to Status enum

    @Column(name = "due_date")
    private LocalDate dueDate;

    // Many tasks belong to one project
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Task assignee can be null until the task is assigned.
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }
}
