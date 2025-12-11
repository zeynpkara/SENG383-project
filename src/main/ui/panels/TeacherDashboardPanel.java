package ui.panels;

import model.Task;
import model.User;
import model.Child;
import persistence.DataManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardPanel extends JPanel {

    private DataManager dataManager;
    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private ClassTasksPanel classTasksPanel;
    private StudentProgressPanel progressPanel;

    public TeacherDashboardPanel(DataManager dm) {
        this.dataManager = dm;
        setLayout(new BorderLayout(10,10));

        JLabel title = new JLabel("Teacher Dashboard");
        title.setFont(title.getFont().deriveFont(20f));
        add(title, BorderLayout.NORTH);

        JPanel menu = createMenu();
        add(menu, BorderLayout.WEST);

        classTasksPanel = new ClassTasksPanel();
        progressPanel = new StudentProgressPanel();

        centerCards.add(classTasksPanel, "TASKS");
        centerCards.add(progressPanel, "PROGRESS");

        add(centerCards, BorderLayout.CENTER);

        cardLayout.show(centerCards, "TASKS");
    }

    private JPanel createMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(160, 0));

        JButton bTasks = new JButton("Class Tasks");
        JButton bProgress = new JButton("Students Progress");
        JButton bAssign = new JButton("Assign Task");

        bTasks.setAlignmentX(Component.CENTER_ALIGNMENT);
        bProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        bAssign.setAlignmentX(Component.CENTER_ALIGNMENT);

        bTasks.addActionListener(e -> {
            classTasksPanel.reload();
            cardLayout.show(centerCards, "TASKS");
        });

        bProgress.addActionListener(e -> {
            progressPanel.reload();
            cardLayout.show(centerCards, "PROGRESS");
        });

        bAssign.addActionListener(e -> assignTaskToClass());

        p.add(Box.createVerticalStrut(10));
        p.add(bTasks);
        p.add(Box.createVerticalStrut(10));
        p.add(bProgress);
        p.add(Box.createVerticalStrut(20));
        p.add(bAssign);
        p.add(Box.createVerticalGlue());

        return p;
    }

    private void assignTaskToClass() {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JTextField points = new JTextField();

        JPanel form = new JPanel(new GridLayout(0,2,5,5));
        form.add(new JLabel("Title:")); form.add(title);
        form.add(new JLabel("Description:")); form.add(desc);
        form.add(new JLabel("Points:")); form.add(points);

        int ok = JOptionPane.showConfirmDialog(this, form, "Assign Class Task", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            List<Task> tasks = dataManager.loadTasks();
            LocalDate dueDate = LocalDate.now().plusDays(3);

            for (User u : dataManager.loadUsers()) {
                if (u.getRole() == User.Role.CHILD) {
                    Task t = new Task(title.getText(), desc.getText(), dueDate, Integer.parseInt(points.getText()), u.getUserId());
                    tasks.add(t);
                }
            }

            dataManager.saveTasks(tasks);
            JOptionPane.showMessageDialog(this, "Task assigned to entire class.");
            classTasksPanel.reload();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }


    private class ClassTasksPanel extends JPanel {

        private JTable table;
        private TaskModel model;

        public ClassTasksPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("All Class Tasks"));

            model = new TaskModel();
            table = new JTable(model);

            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadTasks());
        }
    }

    private static class TaskModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Child ID", "Title", "Status", "Points"};

        public void reload(List<Task> tasks) {
            list = tasks;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            if (c == 0) return t.getAssignedToId();
            if (c == 1) return t.getTitle();
            if (c == 2) return t.getStatus();
            if (c == 3) return t.getPoints();
            return "";
        }
    }

    private class StudentProgressPanel extends JPanel {

        private JTable table;
        private StudentModel model;

        public StudentProgressPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Student Progress"));

            model = new StudentModel();
            table = new JTable(model);

            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            List<Child> children = new ArrayList<>();
            for (User u : dataManager.loadUsers()) {
                if (u.getRole() == User.Role.CHILD) {
                    try { children.add((Child) u); }
                    catch (Exception ignored) {}
                }
            }
            model.setChildren(children);
        }
    }

    private static class StudentModel extends AbstractTableModel {

        private List<Child> list = new ArrayList<>();
        private final String[] cols = {"Child ID", "Level", "Points"};

        public void setChildren(List<Child> children) {
            list = children;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Child ch = list.get(r);
            if (c == 0) return ch.getUserId();
            if (c == 1) return ch.getLevel();
            if (c == 2) return ch.getTotalPoints();
            return "";
        }
    }
}
