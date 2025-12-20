package model;

public class Child extends User {

    private int totalPoints;
    private int level;

    public Child(String userId, String email, String password) {
        super(userId, email, password, Role.CHILD);
        this.totalPoints = 0;
        this.level = 1;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getLevel() {
        return level;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
        updateLevel();
    }

    private void updateLevel() {
        this.level = totalPoints / 100 + 1;
    }
}
