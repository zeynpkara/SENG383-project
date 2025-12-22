package ui.panels;

import model.Task;
import model.User;
import model.Child;
import persistence.DataManager;
import ui.MainApp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardPanel extends JPanel {

    private DataManager dataManager;
    private MainApp mainApp;

    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private TaskApprovalPanel approvalPanel;
    private SummaryPanel summaryPanel;

    public TeacherDashboardPanel(DataManager dm, MainApp app) {
        this.dataManager = dm;
        this.mainApp = app;

        if (app.getLoggedUser() == null ||
                app.getLoggedUser().getRole() != User.Role.TEACHER) {
            JOptionPane.showMessageDialog(this,
                    "No teacher user logged in!");
            return;
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Teacher Dashboard");
        title.setFont(title.getFont().deriveFont(22f));

        JButton switchRole = new JButton("Switch Role");
        switchRole.addActionListener(e -> mainApp.showRoleSelect());

        top.add(title, BorderLayout.WEST);
        top.add(switchRole, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        approvalPanel = new TaskApprovalPanel();
        summaryPanel = new SummaryPanel();

        centerCards.add(approvalPanel, "APPROVAL");
        centerCards.add(summaryPanel, "SUMMARY");

        add(createLeftMenu(), BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        cardLayout.show(centerCards, "APPROVAL");
    }


    private JPanel createLeftMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(170, 0));

        JButton approveBtn = new JButton("Approve Tasks");
        JButton summaryBtn = new JButton("Summary");

        approveBtn.addActionListener(e -> {
            approvalPanel.reload();
            cardLayout.show(centerCards, "APPROVAL");
        });

        summaryBtn.addActionListener(e -> {
            summaryPanel.reload();
            cardLayout.show(centerCards, "SUMMARY");
        });

        for (JButton b : new JButton[]{approveBtn, summaryBtn}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(Box.createVerticalStrut(10));
            p.add(b);
        }

        p.add(Box.createVerticalGlue());
        return p;
    }


    private class TaskApprovalPanel extends JPanel {

        private JTable table;
        private TaskModel model = new TaskModel();

        public TaskApprovalPanel() {
            setLayout(new BorderLayout(10, 10));

            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JComboBox<Integer> ratingBox =
                    new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(new JLabel("Rating:"));
            bottom.add(ratingBox);

            JButton approve = new JButton("Approve");
            JButton reject = new JButton("Reject");

            approve.addActionListener(e ->
                    approveTask((Integer) ratingBox.getSelectedItem()));
            reject.addActionListener(e -> rejectTask());

            bottom.add(approve);
            bottom.add(reject);

            add(bottom, BorderLayout.SOUTH);

            reload();
        }

        private void approveTask(int rating) {
            int row = table.getSelectedRow();
            if (row < 0) return;

            Task selected = model.getAt(row);

            List<Task> tasks = dataManager.loadTasks();
            List<User> users = dataManager.loadUsers();

            for (Task t : tasks) {
                if (t.getTaskId().equals(selected.getTaskId())) {
                    t.setStatus(Task.Status.APPROVED);
                    t.setRating(rating);

                    for (User u : users) {
                        if (u instanceof Child ch &&
                                ch.getUserId().equals(t.getAssignedToId())) {
                            ch.addPoints(t.getPoints());
                        }
                    }
                }
            }

            dataManager.saveTasks(tasks);
            dataManager.saveUsers(users);

            reload();
            summaryPanel.reload(); 
        }

        private void rejectTask() {
            int row = table.getSelectedRow();
            if (row < 0) return;

            Task selected = model.getAt(row);
            List<Task> tasks = dataManager.loadTasks();

            for (Task t : tasks)
                if (t.getTaskId().equals(selected.getTaskId()))
                    t.setStatus(Task.Status.REJECTED);

            dataManager.saveTasks(tasks);

            reload();
            summaryPanel.reload(); 
        }

        private void reload() {
            model.reload(dataManager.loadTasks());
        }
    }


    private class SummaryPanel extends JPanel {

        private JLabel totalLbl = new JLabel();
        private JLabel approvedLbl = new JLabel();
        private JLabel rejectedLbl = new JLabel();
        private JLabel avgRatingLbl = new JLabel();

        private TaskChartPanel chartPanel = new TaskChartPanel();
        private JTable childTable;
        private ChildSummaryModel model = new ChildSummaryModel();

        public SummaryPanel() {
            setLayout(new BorderLayout(10, 10));

            JPanel stats = new JPanel(new GridLayout(1, 4, 10, 10));
            stats.setBorder(BorderFactory.createTitledBorder("Task Summary"));

            stats.add(totalLbl);
            stats.add(approvedLbl);
            stats.add(rejectedLbl);
            stats.add(avgRatingLbl);

            add(stats, BorderLayout.NORTH);

            chartPanel.setPreferredSize(new Dimension(450, 260));
            chartPanel.setBorder(
                    BorderFactory.createTitledBorder("Task Status Chart"));
            add(chartPanel, BorderLayout.CENTER);

            childTable = new JTable(model);
            add(new JScrollPane(childTable), BorderLayout.SOUTH);

            reload();
        }

        public void reload() {
            List<Task> tasks = dataManager.loadTasks();
            List<User> users = dataManager.loadUsers();

            int total = tasks.size();
            int approved = 0;
            int rejected = 0;
            int ratingSum = 0;
            int ratedCount = 0;

            for (Task t : tasks) {
                if (t.getStatus() == Task.Status.APPROVED) approved++;
                if (t.getStatus() == Task.Status.REJECTED) rejected++;
                if (t.getRating() > 0) {
                    ratingSum += t.getRating();
                    ratedCount++;
                }
            }

            double avgRating = ratedCount == 0 ? 0 :
                    (double) ratingSum / ratedCount;

            totalLbl.setText("📌 Total: " + total);
            approvedLbl.setText("✅ Approved: " + approved);
            rejectedLbl.setText("❌ Rejected: " + rejected);
            avgRatingLbl.setText("⭐ Avg Rating: " +
                    String.format("%.2f", avgRating));

            chartPanel.updateData(approved, rejected);
            model.reload(users);
        }
    }


    private class TaskChartPanel extends JPanel {

        private int approved;
        private int rejected;

        public void updateData(int approved, int rejected) {
            this.approved = approved;
            this.rejected = rejected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            int w = getWidth();
            int h = getHeight();

            int barWidth = 80;
            int gap = 60;

            int max = Math.max(approved, rejected);
            if (max == 0) {
                g2.drawString("No data available", w / 2 - 40, h / 2);
                return;
            }

            int approvedH = (int) ((approved / (double) max) * (h - 80));
            int rejectedH = (int) ((rejected / (double) max) * (h - 80));

            int x1 = w / 2 - barWidth - gap / 2;
            int x2 = w / 2 + gap / 2;

            g2.setColor(new Color(76, 175, 80));
            g2.fillRect(x1, h - approvedH - 40, barWidth, approvedH);

            g2.setColor(new Color(244, 67, 54));
            g2.fillRect(x2, h - rejectedH - 40, barWidth, rejectedH);

            g2.setColor(Color.BLACK);
            g2.drawString("Approved", x1 + 10, h - 15);
            g2.drawString("Rejected", x2 + 10, h - 15);
        }
    }


    private static class TaskModel extends AbstractTableModel {

        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Child", "Title", "Points"};

        public void reload(List<Task> tasks) {
            list.clear();
            for (Task t : tasks)
                if (t.getStatus() == Task.Status.COMPLETED)
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
                default -> t.getPoints();
            };
        }
    }

    private static class ChildSummaryModel extends AbstractTableModel {

        private List<Child> list = new ArrayList<>();
        private final String[] cols = {"Child ID", "Points", "Level"};

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
            return switch (c) {
                case 0 -> ch.getUserId();
                case 1 -> ch.getTotalPoints();
                default -> ch.getLevel();
            };
        }
    }
}
