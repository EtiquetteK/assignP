package com.PracticalAssignment.assignP.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailsDTO {
    private TaskDTO task;
    private List<TaskCommentDTO> comments = new ArrayList<>();
    private List<TaskActivityDTO> activity = new ArrayList<>();

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public List<TaskCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<TaskCommentDTO> comments) {
        this.comments = comments;
    }

    public List<TaskActivityDTO> getActivity() {
        return activity;
    }

    public void setActivity(List<TaskActivityDTO> activity) {
        this.activity = activity;
    }
}
