package com.PracticalAssignment.assignP.dto;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchResultDTO {
    private String query;
    private List<ProjectDTO> projects = new ArrayList<>();
    private List<TaskDTO> tasks = new ArrayList<>();
    private List<UserDTO> users = new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
