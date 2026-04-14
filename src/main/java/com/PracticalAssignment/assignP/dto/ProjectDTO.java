package com.PracticalAssignment.assignP.dto;

import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private List<ProjectMemberDTO> members = new ArrayList<>();

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public List<ProjectMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<ProjectMemberDTO> members) {
        this.members = members;
    }
}
