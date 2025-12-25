package ui;

import model.Child;
import model.User;
import persistence.DataManager;
import ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainApp {

    private JFrame frame;
    private DataManager dataManager;
    private User loggedUser;

    public MainApp() {
        dataManager = new DataManager();
        seedUsers();
        SwingUtilities.invokeLater(this::createAndShowGui);
    }


    private void seedUsers() {
        List<User> users = dataManager.loadUsers();

        boolean hasChild = false;
        for (User u : users) {
            if (u instanceof Child) {
                hasChild = true;
                break;
            }
        }

        if (!hasChild) {
            users.add(new Child(
                    "child1",
                    "child1@mail.com",
                    "1234"
            ));

            users.add(new User(
                    "parent1",
                    "parent@mail.com",
                    "1234",
                    User.Role.PARENT
            ));

            users.add(new User(
                    "teacher1",
                    "teacher@mail.com",
                    "1234",
                    User.Role.TEACHER
            ));

            dataManager.saveUsers(users);
        }
    }


    public void setLoggedUser(User u) {
        this.loggedUser = u;
        System.out.println("Logged user set: " + u.getUserId());
    }

    public User getLoggedUser() {
        return loggedUser;
    }


    private void createAndShowGui() {
        frame = new JFrame("KidTask");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        showRoleSelect();

        frame.setVisible(true);
    }

    public void showRoleSelect() {
        frame.getContentPane().removeAll();

        RoleSelectPanel rsp = new RoleSelectPanel(this::onRoleSelected);
        frame.add(rsp, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }


    private void onRoleSelected(User.Role role) {

        // 🔹 1️⃣ users.json’dan role’a uygun kullanıcıyı bul
        User selectedUser = null;
        for (User u : dataManager.loadUsers()) {
            if (u.getRole() == role) {
                selectedUser = u;
                break;
            }
        }

        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "No user found for role: " + role);
            return;
        }

        setLoggedUser(selectedUser);

        frame.getContentPane().removeAll();

        JPanel panel;
        switch (role) {
            case CHILD:
                panel = new ChildDashboardPanel(dataManager, this);
                break;
            case PARENT:
                panel = new ParentDashboardPanel(dataManager, this);
                break;
            case TEACHER:
                panel = new TeacherDashboardPanel(dataManager, this);
                break;
            default:
                panel = new JPanel();
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }


    public static void main(String[] args) {
        new MainApp();
    }
}
