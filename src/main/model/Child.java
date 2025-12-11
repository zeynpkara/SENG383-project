package model;

import java.util.ArrayList;
import java.util.List;

public class Child extends User {
    private int totalPoints;
    private int level;
    private List<String> completedTaskIds;

    public Child(String userId, String email, String password) {
        super(userId, email, password, Role.CHILD);
        this.totalPoints = 0;
        this.level = 1;
        this.completedTaskIds = new ArrayList<>();
    }

    public void markCompleted(String taskId, int points){
        if(!completedTaskIds.contains(taskId)){
            completedTaskIds.add(taskId);
            totalPoints += points;
        }
    }
    public int getTotalPoints() { return totalPoints; }
    public int getLevel() { return level; }
    public List<String> getCompletedTaskIds() { return completedTaskIds; }

    public void setLevel(int level) { this.level = level; }
}
