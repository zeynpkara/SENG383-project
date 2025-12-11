package ui.panels;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class RoleSelectPanel extends JPanel {

    public RoleSelectPanel(Consumer<User.Role> onRoleSelected) {
        initializeUi(onRoleSelected);
    }

    private void initializeUi(Consumer<User.Role> onRoleSelected) {
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(400, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,12,12,12);

        JLabel title = new JLabel("Select role to continue");
        title.setFont(title.getFont().deriveFont(18f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(title, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));

        JButton childBtn = new JButton("Child");
        JButton parentBtn = new JButton("Parent");
        JButton teacherBtn = new JButton("Teacher");

        Dimension btnSize = new Dimension(140, 36);
        childBtn.setPreferredSize(btnSize);
        parentBtn.setPreferredSize(btnSize);
        teacherBtn.setPreferredSize(btnSize);

        btnPanel.add(childBtn);
        btnPanel.add(parentBtn);
        btnPanel.add(teacherBtn);

        gbc.gridy = 1;
        add(btnPanel, gbc);

        childBtn.addActionListener(e -> {
            System.out.println("Child button clicked!");
            onRoleSelected.accept(User.Role.CHILD);
        });
        parentBtn.addActionListener(e -> {
            System.out.println("Parent button clicked!");
            onRoleSelected.accept(User.Role.PARENT);
        });
        teacherBtn.addActionListener(e -> {
            System.out.println("Teacher button clicked!");
            onRoleSelected.accept(User.Role.TEACHER);
        });
    }
}
