package ui.panels;

import model.Task;
import model.User;
import model.Wish;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ParentDashboardPanel extends JPanel {

    private DataManager dataManager;
    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private PendingTasksPanel pendingPanel;
    private ChildProgressPanel progressPanel;
    private WishApprovalPanel wishPanel;

    public ParentDashboardPanel(DataManager dm) {
        this.dataManager = dm;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Parent Dashboard");
        title.setFont(title.getFont().deriveFont(22f));
        add(title, BorderLayout.NORTH);

        JPanel left = createLeftMenu();
        add(left, BorderLayout.WEST);

        pendingPanel = new PendingTasksPanel();
        progressPanel = new ChildProgressPanel();
        wishPanel = new WishApprovalPanel();

        centerCards.add(pendingPanel, "PENDING");
        centerCards.add(progressPanel, "PROGRESS");
        centerCards.add(wishPanel, "WISHES");

        add(centerCards, BorderLayout.CENTER);

        cardLayout.show(centerCards, "PENDING");
    }

    private JPanel createLeftMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(160, 0));

        JButton b1 = new JButton("Pending Tasks");
        JButton b2 = new JButton("Children Progress");
        JButton b3 = new JButton("Wish Approvals");
        JButton b4 = new JButton("Assign Task");

        b1.setAlignmentX(Component.CENTER_ALIGNMENT);
        b2.setAlignmentX(Component.CENTER_ALIGNMENT);
        b3.setAlignmentX(Component.CENTER_ALIGNMENT);
        b4.setAlignmentX(Component.CENTER_ALIGNMENT);

        b1.addActionListener(e -> cardLayout.show(centerCards, "PENDING"));
        b2.addActionListener(e -> {
            progressPanel.reload();
            cardLayout.show(centerCards, "PROGRESS");
        });
        b3.addActionListener(e -> {
            wishPanel.reload();
            cardLayout.show(centerCards, "WISHES");
        });
        b4.addActionListener(e -> openAssignTaskDialog());

        p.add(Box.createVerticalStrut(10));
        p.add(b1); p.add(Box.createVerticalStrut(10));
        p.add(b2); p.add(Box.createVerticalStrut(10));
        p.add(b3); p.add(Box.createVerticalStrut(20));
        p.add(b4);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private void openAssignTaskDialog() {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JTextField points = new JTextField();
        JTextField due = new JTextField();

        JComboBox<String> childBox = new JComboBox<>();

        for (User u : dataManager.loadUsers()) {
            if (u.getRole() == User.Role.CHILD) childBox.addItem(u.getUserId());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Child ID:"));
        form.add(childBox);
        form.add(new JLabel("Title:"));
        form.add(title);
        form.add(new JLabel("Description:"));
        form.add(desc);
        form.add(new JLabel("Points:"));
        form.add(points);
        form.add(new JLabel("Due Date (YYYY-MM-DD):"));
        form.add(due);

        int ok = JOptionPane.showConfirmDialog(this, form, "Assign New Task", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            String cid = (String) childBox.getSelectedItem();
            Task t = new Task(title.getText(), desc.getText(), LocalDate.parse(due.getText()), Integer.parseInt(points.getText()), cid);

            List<Task> tasks = dataManager.loadTasks();
            tasks.add(t);
            dataManager.saveTasks(tasks);

            JOptionPane.showMessageDialog(this, "Task Assigned.");
            pendingPanel.reload();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }


    private class PendingTasksPanel extends JPanel {

        private JTable table;
        private TaskTableModel model;

        public PendingTasksPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("Pending Task Approvals"));

            model = new TaskTableModel();
            table = new JTable(model);

            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton approve = new JButton("Approve");
            JButton reject = new JButton("Reject");
            buttons.add(approve);
            buttons.add(reject);

            approve.addActionListener(e -> approveTask());
            reject.addActionListener(e -> rejectTask());

            add(buttons, BorderLayout.SOUTH);
            reload();
        }

        private void approveTask() {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task."); return; }

            Task t = model.getAt(row);
            t.setStatus(Task.Status.APPROVED);

            List<Task> list = dataManager.loadTasks();
            for (Task x : list) {
                if (x.getTaskId().equals(t.getTaskId())) x.setStatus(Task.Status.APPROVED);
            }
            dataManager.saveTasks(list);
            reload();
        }

        private void rejectTask() {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task."); return; }

            Task t = model.getAt(row);
            t.setStatus(Task.Status.REJECTED);

            List<Task> list = dataManager.loadTasks();
            for (Task x : list) {
                if (x.getTaskId().equals(t.getTaskId())) x.setStatus(Task.Status.REJECTED);
            }
            dataManager.saveTasks(list);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadTasks());
        }
    }

    private static class TaskTableModel extends AbstractTableModel {

        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Child", "Title", "Due", "Points"};

        public void reload(List<Task> tasks) {
            list.clear();
            for (Task t : tasks) {
                if (t.getStatus() == Task.Status.COMPLETED) list.add(t);
            }
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
            if (c == 2) {
                if (t.getDueDate() != null) return t.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                return "";
            }
            if (c == 3) return t.getPoints();
            return "";
        }

        public Task getAt(int row) { return list.get(row); }
    }


    private class ChildProgressPanel extends JPanel {

        private JTable table;
        private ProgressModel model;

        public ChildProgressPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("Children Progress"));

            model = new ProgressModel();
            table = new JTable(model);

            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            List<User> users = dataManager.loadUsers();
            List<Child> children = new ArrayList<>();
            for (User u : users) {
                if (u.getRole() == User.Role.CHILD) {
                    try { children.add((Child) u); }
                    catch (Exception ignored) {}
                }
            }
            model.setChildren(children);
        }
    }

    private static class ProgressModel extends AbstractTableModel {

        private List<Child> list = new ArrayList<>();
        private final String[] cols = {"Child ID", "Level", "Points"};

        public void setChildren(List<Child> c) {
            list = c;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length;}
        @Override public String getColumnName(int c) { return cols[c];}

        @Override
        public Object getValueAt(int r, int c) {
            Child ch = list.get(r);
            if (c == 0) return ch.getUserId();
            if (c == 1) return ch.getLevel();
            if (c == 2) return ch.getTotalPoints();
            return "";
        }
    }


    private class WishApprovalPanel extends JPanel {

        private JTable table;
        private WishModel model;

        public WishApprovalPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Wish Approvals"));

            model = new WishModel();
            table = new JTable(model);

            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton approve = new JButton("Approve");
            JButton reject = new JButton("Reject");
            bottom.add(approve);
            bottom.add(reject);

            approve.addActionListener(e -> approveWish());
            reject.addActionListener(e -> rejectWish());

            add(bottom, BorderLayout.SOUTH);
            reload();
        }

        private void approveWish() {
            int r = table.getSelectedRow();
            if (r < 0) return;

            Wish w = model.getAt(r);
            w.setStatus(Wish.Status.APPROVED);

            List<Wish> list = dataManager.loadWishes();
            for (Wish x : list)
                if (x.getWishId().equals(w.getWishId()))
                    x.setStatus(Wish.Status.APPROVED);

            dataManager.saveWishes(list);
            reload();
        }

        private void rejectWish() {
            int r = table.getSelectedRow();
            if (r < 0) return;

            Wish w = model.getAt(r);
            w.setStatus(Wish.Status.REJECTED);

            List<Wish> list = dataManager.loadWishes();
            for (Wish x : list)
                if (x.getWishId().equals(w.getWishId()))
                    x.setStatus(Wish.Status.REJECTED);

            dataManager.saveWishes(list);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadWishes());
        }
    }

    private static class WishModel extends AbstractTableModel {

        private final String[] cols = {"Child", "Name", "Cost", "Status"};
        private List<Wish> list = new ArrayList<>();

        public void reload(List<Wish> wishes) {
            list.clear();
            list.addAll(wishes);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Wish w = list.get(r);
            if (c == 0) return w.getRequestedById();
            if (c == 1) return w.getName();
            if (c == 2) return w.getCost();
            if (c == 3) return w.getStatus();
            return "";
        }

        public Wish getAt(int row) { return list.get(row); }
    }
}
