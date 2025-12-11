package model;

import java.util.UUID;

public class Wish {
    public enum Status { PENDING, APPROVED, REJECTED }

    private String wishId;
    private String name;
    private int cost;
    private Status status;
    private String requestedById;
    private String approvedById;
    private int requiredLevel;

    public Wish(String name, int cost, String requestedById, int requiredLevel) {
        this.wishId = UUID.randomUUID().toString();
        this.name = name;
        this.cost = cost;
        this.status = Status.PENDING;
        this.requestedById = requestedById;
        this.requiredLevel = requiredLevel;
    }

    public String getWishId() { return wishId; }
    public String getName() { return name; }
    public int getCost() { return cost; }
    public Status getStatus() { return status; }
    public String getRequestedById() { return requestedById; }
    public String getApprovedById() { return approvedById; }
    public int getRequiredLevel() { return requiredLevel; }

    public void setStatus(Status status) { this.status = status; }
}
