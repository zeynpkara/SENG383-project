package model;

public class User {

    public enum Role { CHILD, PARENT, TEACHER }

    protected String userId;
    protected String email;
    protected String password;
    protected Role role;

    public User(String userId, String email, String password, Role role){
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
