package model;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    public enum Status { PENDING, COMPLETED, APPROVED, REJECTED }

    private String taskId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int points;
    private Status status;
    private String assignedToId;
    private int rating;

    public Task(String title, String description, LocalDate dueDate, int points, String assignedToId) {
        this.taskId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.points = points;
        this.status = Status.PENDING;
        this.assignedToId = assignedToId;
        this.rating = 0;

    }

    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public int getPoints() { return points; }
    public Status getStatus() { return status; }
    public String getAssignedToId() { return assignedToId; }
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setStatus(Status status) { this.status = status; }
}
