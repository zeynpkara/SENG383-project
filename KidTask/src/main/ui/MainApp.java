package ui;


import model.User;
import persistence.DataManager;


import javax.swing.*;
import java.awt.*;

import ui.panels.ChildDashboardPanel;
import ui.panels.ParentDashboardPanel;
import ui.panels.RoleSelectPanel;
import ui.panels.TeacherDashboardPanel;

public class MainApp {
    private JFrame frame;
    private DataManager dataManager;


    public MainApp(){
        dataManager = new DataManager();
        SwingUtilities.invokeLater(this::createAndShowGui);
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
            case CHILD: panel = new ChildDashboardPanel(dataManager); break;
            case PARENT: panel = new ParentDashboardPanel(dataManager); break;
            case TEACHER: panel = new TeacherDashboardPanel(dataManager); break;
            default: panel = new JPanel();
        }
        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }


    public static void main(String[] args){ new MainApp(); }
}