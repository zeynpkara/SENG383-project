package ui.panels;

import model.Task;
import model.User;
import model.Wish;
import model.Child;
import persistence.DataManager;
import ui.MainApp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ParentDashboardPanel extends JPanel {

    private DataManager dataManager;
    private MainApp mainApp;

    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private PendingTasksPanel pendingPanel;
    private ChildProgressPanel progressPanel;
    private WishApprovalPanel wishPanel;

    public ParentDashboardPanel(DataManager dm, MainApp app) {
        this.dataManager = dm;
        this.mainApp = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Parent Dashboard");
        title.setFont(title.getFont().deriveFont(22f));

        JButton switchRole = new JButton("Switch Role");
        switchRole.addActionListener(e -> mainApp.showRoleSelect());

        top.add(title, BorderLayout.WEST);
        top.add(switchRole, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        add(createLeftMenu(), BorderLayout.WEST);

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
        p.setPreferredSize(new Dimension(170, 0));

        JButton b1 = new JButton("Pending Tasks");
        JButton b2 = new JButton("Children Progress");
        JButton b3 = new JButton("Wish Approvals");
        JButton b4 = new JButton("Assign Task");

        b1.addActionListener(e -> {
            pendingPanel.reload();
            cardLayout.show(centerCards, "PENDING");
        });

        b2.addActionListener(e -> {
            progressPanel.reload();
            cardLayout.show(centerCards, "PROGRESS");
        });

        b3.addActionListener(e -> {
            wishPanel.reload();
            cardLayout.show(centerCards, "WISHES");
        });

        b4.addActionListener(e -> openAssignTaskDialog());

        for (JButton b : new JButton[]{b1, b2, b3, b4}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(Box.createVerticalStrut(10));
            p.add(b);
        }

        p.add(Box.createVerticalGlue());
        return p;
    }


    private void openAssignTaskDialog() {
        JTextField title = new JTextField();
        JTextField desc = new JTextField();
        JTextField points = new JTextField();
        JTextField due = new JTextField();

        JComboBox<String> childBox = new JComboBox<>();
        for (User u : dataManager.loadUsers())
            if (u.getRole() == User.Role.CHILD)
                childBox.addItem(u.getUserId());

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Child ID:")); form.add(childBox);
        form.add(new JLabel("Title:")); form.add(title);
        form.add(new JLabel("Description:")); form.add(desc);
        form.add(new JLabel("Points:")); form.add(points);
        form.add(new JLabel("Due Date (YYYY-MM-DD):")); form.add(due);

        if (JOptionPane.showConfirmDialog(this, form,
                "Assign Task", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;

        Task t = new Task(
                title.getText(),
                desc.getText(),
                LocalDate.parse(due.getText()),
                Integer.parseInt(points.getText()),
                (String) childBox.getSelectedItem()
        );

        t.setStatus(Task.Status.PENDING);

        List<Task> list = dataManager.loadTasks();
        list.add(t);
        dataManager.saveTasks(list);

        pendingPanel.reload();
        cardLayout.show(centerCards, "PENDING");
    }


    private class PendingTasksPanel extends JPanel {
        private JTable table;
        private TaskTableModel model = new TaskTableModel();

        public PendingTasksPanel() {
            setLayout(new BorderLayout());

            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton approve = new JButton("Approve");
            JButton reject = new JButton("Reject");

            approve.addActionListener(e -> approveTask());
            reject.addActionListener(e -> rejectTask());

            buttons.add(approve);
            buttons.add(reject);
            add(buttons, BorderLayout.SOUTH);

            reload();
        }

        private void approveTask() {
            int row = table.getSelectedRow();
            if (row < 0) return;

            Task task = model.getAt(row);
            task.setStatus(Task.Status.APPROVED);

            List<Task> tasks = dataManager.loadTasks();
            List<User> users = dataManager.loadUsers();

            for (Task t : tasks) {
                if (t.getTaskId().equals(task.getTaskId())) {
                    t.setStatus(Task.Status.APPROVED);
                }
            }

            for (User u : users) {
                if (u.getUserId().equals(task.getAssignedToId())
                        && u instanceof Child) {

                    Child ch = (Child) u;
                    ch.addPoints(task.getPoints());
                }
            }

            dataManager.saveTasks(tasks);
            dataManager.saveUsers(users);

            reload();
        }


        private void rejectTask() {
            int row = table.getSelectedRow();
            if (row < 0) return;

            Task t = model.getAt(row);
            t.setStatus(Task.Status.REJECTED);

            List<Task> tasks = dataManager.loadTasks();
            for (Task x : tasks)
                if (x.getTaskId().equals(t.getTaskId()))
                    x.setStatus(Task.Status.REJECTED);

            dataManager.saveTasks(tasks);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadTasks());
        }

        private void rewardChild(Task task) {
            List<User> users = dataManager.loadUsers();
            for (User u : users) {
                if (u instanceof Child ch &&
                        ch.getUserId().equals(task.getAssignedToId())) {

                    ch.addPoints(task.getPoints());
                }
            }
            dataManager.saveUsers(users);
        }
    }

    private static class TaskTableModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Child", "Title", "Due", "Points"};

        public void reload(List<Task> tasks) {
            list.clear();
            for (Task t : tasks)
                if (t.getStatus() == Task.Status.PENDING)
                    list.add(t);
            fireTableDataChanged();
        }

        public Task getAt(int r) { return list.get(r); }
        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            return switch (c) {
                case 0 -> t.getAssignedToId();
                case 1 -> t.getTitle();
                case 2 -> t.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                case 3 -> t.getPoints();
                default -> "";
            };
        }
    }


    private class ChildProgressPanel extends JPanel {

        private JTable table;
        private ProgressModel model = new ProgressModel();

        public ChildProgressPanel() {
            setLayout(new BorderLayout());
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadUsers());
        }
    }

    private static class ProgressModel extends AbstractTableModel {

        private List<Child> list = new ArrayList<>();
        private final String[] cols = {"Child ID", "Level", "Points"};

        public void reload(List<User> users) {
            list.clear();
            for (User u : users)
                if (u instanceof Child)
                    list.add((Child) u);
            fireTableDataChanged();
        }

        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Child ch = list.get(r);
            if (c == 0) return ch.getUserId();
            if (c == 1) return ch.getLevel();
            return ch.getTotalPoints();
        }
    }


    private class WishApprovalPanel extends JPanel {

        private JTable table;
        private WishModel model = new WishModel();

        public WishApprovalPanel() {
            setLayout(new BorderLayout());
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton approve = new JButton("Approve");
            JButton reject = new JButton("Reject");

            approve.addActionListener(e -> updateWish(Wish.Status.APPROVED));
            reject.addActionListener(e -> updateWish(Wish.Status.REJECTED));

            buttons.add(approve);
            buttons.add(reject);
            add(buttons, BorderLayout.SOUTH);

            reload();
        }

        private void updateWish(Wish.Status status) {
            int r = table.getSelectedRow();
            if (r < 0) return;

            Wish w = model.getAt(r);
            w.setStatus(status);

            List<Wish> wishes = dataManager.loadWishes();
            for (Wish x : wishes)
                if (x.getWishId().equals(w.getWishId()))
                    x.setStatus(status);

            dataManager.saveWishes(wishes);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadWishes());
        }
    }

    private static class WishModel extends AbstractTableModel {

        private List<Wish> list = new ArrayList<>();
        private final String[] cols = {"Child", "Name", "Cost", "Status"};

        public void reload(List<Wish> wishes) {
            list.clear();
            list.addAll(wishes);
            fireTableDataChanged();
        }

        public Wish getAt(int r) { return list.get(r); }
        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Wish w = list.get(r);
            return switch (c) {
                case 0 -> w.getRequestedById();
                case 1 -> w.getName();
                case 2 -> w.getCost();
                default -> w.getStatus();
            };
        }
    }
}
