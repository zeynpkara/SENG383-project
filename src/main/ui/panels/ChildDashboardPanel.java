package ui.panels;

import model.Task;
import model.Wish;
import model.Child;
import model.User;
import persistence.DataManager;
import ui.MainApp;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChildDashboardPanel extends JPanel {

    private DataManager dataManager;
    private MainApp mainApp;

    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private AssignedTasksPanel assignedPanel;
    private CompletedTasksPanel completedPanel;
    private WishesPanel wishesPanel;
    private HomePanel homePanel;

    private Child loggedChild;

    public ChildDashboardPanel(DataManager dm, MainApp app) {
        this.dataManager = dm;
        this.mainApp = app;

        // 🔹 Find logged child
        for (User u : dm.loadUsers()) {
            if (u.getRole() == User.Role.CHILD) {
                loggedChild = new Child(
                        u.getUserId(),
                        u.getEmail(),
                        u.getPassword()
                );
                break;
            }
        }

        if (loggedChild == null) {
            JOptionPane.showMessageDialog(this, "No child user found!");
            return;
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Child Dashboard");
        title.setFont(title.getFont().deriveFont(22f));

        JButton switchRole = new JButton("Switch Role");
        switchRole.addActionListener(e -> mainApp.showRoleSelect());

        top.add(title, BorderLayout.WEST);
        top.add(switchRole, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        homePanel = new HomePanel();
        assignedPanel = new AssignedTasksPanel();
        completedPanel = new CompletedTasksPanel();
        wishesPanel = new WishesPanel();

        centerCards.add(homePanel, "HOME");
        centerCards.add(assignedPanel, "ASSIGNED");
        centerCards.add(completedPanel, "COMPLETED");
        centerCards.add(wishesPanel, "WISHES");

        add(createLeftMenu(), BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        cardLayout.show(centerCards, "HOME");
    }


    private JPanel createLeftMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(160, 0));

        JButton homeBtn = new JButton("Home");
        JButton b1 = new JButton("My Tasks");
        JButton b2 = new JButton("Completed");
        JButton b3 = new JButton("My Wishes");

        homeBtn.addActionListener(e -> {
            homePanel.reload();
            cardLayout.show(centerCards, "HOME");
        });

        b1.addActionListener(e -> {
            assignedPanel.reload();
            cardLayout.show(centerCards, "ASSIGNED");
        });

        b2.addActionListener(e -> {
            completedPanel.reload();
            cardLayout.show(centerCards, "COMPLETED");
        });

        b3.addActionListener(e -> {
            wishesPanel.reload();
            cardLayout.show(centerCards, "WISHES");
        });

        for (JButton b : new JButton[]{homeBtn, b1, b2, b3}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(Box.createVerticalStrut(10));
            p.add(b);
        }

        p.add(Box.createVerticalGlue());
        return p;
    }


    private class HomePanel extends JPanel {

        private JLabel infoLabel;

        public HomePanel() {
            setLayout(new GridLayout(2, 1, 10, 10));
            setBorder(BorderFactory.createTitledBorder("Overview"));

            infoLabel = new JLabel();
            infoLabel.setFont(infoLabel.getFont().deriveFont(16f));

            add(new JLabel("Welcome 👋"));
            add(infoLabel);

            reload();
        }

        public void reload() {
            int pending = 0;
            for (Task t : dataManager.loadTasks()) {
                if (t.getAssignedToId().equals(loggedChild.getUserId())
                        && t.getStatus() == Task.Status.PENDING) {
                    pending++;
                }
            }

            infoLabel.setText(
                    "⭐ Points: " + loggedChild.getTotalPoints()
                            + " | 🏆 Level: " + loggedChild.getLevel()
                            + " | 📌 Pending: " + pending
            );
        }
    }


    private class AssignedTasksPanel extends JPanel {

        private JTable table;
        private AssignedModel model = new AssignedModel();
        private JComboBox<String> filterBox;

        public AssignedTasksPanel() {

            filterBox = new JComboBox<>(new String[]{"All", "Today", "This Week"});

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(new JLabel("Filter:"));
            top.add(filterBox);
            add(top, BorderLayout.NORTH);

            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("Assigned Tasks"));

            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JButton complete = new JButton("Mark as Completed");
            complete.addActionListener(e -> completeTask());

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(complete);
            add(bottom, BorderLayout.SOUTH);

            reload();
        }

        public void reload() {
            List<Task> tasks = dataManager.loadTasks();
            String filter = (String) filterBox.getSelectedItem();
            model.reload(tasks, loggedChild.getUserId(), filter);
        }


        private void completeTask() {
            int row = table.getSelectedRow();
            if (row < 0) return;

            Task t = model.getAt(row);
            t.setStatus(Task.Status.COMPLETED);

            List<Task> tasks = dataManager.loadTasks();
            for (Task x : tasks)
                if (x.getTaskId().equals(t.getTaskId()))
                    x.setStatus(Task.Status.COMPLETED);

            dataManager.saveTasks(tasks);

            reload();
            completedPanel.reload();
            homePanel.reload();
        }
    }

    private static class AssignedModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Title", "Due", "Points"};

        public void reload(List<Task> tasks, String childId, String filter) {
            list.clear();
            LocalDate today = LocalDate.now();
            WeekFields wf = WeekFields.of(Locale.getDefault());

            for (Task t : tasks) {
                if (!t.getAssignedToId().equals(childId)) continue;
                if (t.getStatus() != Task.Status.PENDING) continue;
                if (t.getDueDate() == null) continue;

                boolean include = switch (filter) {
                    case "Today" ->
                            t.getDueDate().equals(today);
                    case "This Week" ->
                            t.getDueDate().get(wf.weekOfWeekBasedYear())
                                    == today.get(wf.weekOfWeekBasedYear());
                    default -> true;
                };

                if (include) list.add(t);
            }
            fireTableDataChanged();
        }


        public Task getAt(int r) { return list.get(r); }
        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            return c == 0 ? t.getTitle()
                    : c == 1 ? t.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : t.getPoints();
        }
    }


    private class CompletedTasksPanel extends JPanel {

        private JTable table;
        private CompletedModel model = new CompletedModel();

        public CompletedTasksPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Completed Tasks"));
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadTasks(), loggedChild.getUserId());
        }
    }

    private static class CompletedModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Title", "Status"};

        public void reload(List<Task> tasks, String childId) {
            list.clear();
            for (Task t : tasks)
                if (t.getAssignedToId().equals(childId)
                        && t.getStatus() != Task.Status.PENDING)
                    list.add(t);
            fireTableDataChanged();
        }

        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            return c == 0 ? t.getTitle() : t.getStatus();
        }
    }


    private class WishesPanel extends JPanel {

        private JTable table;
        private WishModel model = new WishModel();

        public WishesPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("My Wishes"));
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            reload();
        }

        public void reload() {
            model.reload(dataManager.loadWishes(), loggedChild.getUserId());
        }
    }

    private static class WishModel extends AbstractTableModel {
        private List<Wish> list = new ArrayList<>();
        private final String[] cols = {"Name", "Cost", "Status"};

        public void reload(List<Wish> wishes, String childId) {
            list.clear();
            for (Wish w : wishes)
                if (w.getRequestedById().equals(childId))
                    list.add(w);
            fireTableDataChanged();
        }

        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        public Object getValueAt(int r, int c) {
            Wish w = list.get(r);
            return c == 0 ? w.getName()
                    : c == 1 ? w.getCost()
                    : w.getStatus();
        }
    }
}
