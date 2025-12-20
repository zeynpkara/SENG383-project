package ui;

import model.Child;
import java.awt.BorderLayout;
import java.awt.*;

import model.User;
import persistence.DataManager;


import javax.swing.*;
import java.util.List;

import ui.panels.ChildDashboardPanel;
import ui.panels.ParentDashboardPanel;
import ui.panels.RoleSelectPanel;
import ui.panels.TeacherDashboardPanel;

public class MainApp {
    private JFrame frame;
    private DataManager dataManager;


    public MainApp(){
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


    public void showRoleSelect() {
        frame.getContentPane().removeAll();
        frame.add(new RoleSelectPanel(this::onRoleSelected), BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }


    private void createAndShowGui(){
        frame = new JFrame("KidTask");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,700);
        frame.setLayout(new BorderLayout());


        RoleSelectPanel rsp = new RoleSelectPanel(this::onRoleSelected);
        frame.add(rsp, BorderLayout.CENTER);
        frame.setVisible(true);
    }


    private void onRoleSelected(User.Role role){
        frame.getContentPane().removeAll();

        JPanel panel;
        switch(role){
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




    public static void main(String[] args){ new MainApp(); }
}
