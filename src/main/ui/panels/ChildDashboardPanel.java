package ui.panels;

import model.Task;
import model.Wish;
import model.Child;
import model.User;
import persistence.DataManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChildDashboardPanel extends JPanel {

    private DataManager dataManager;
    private CardLayout cardLayout = new CardLayout();
    private JPanel centerCards = new JPanel(cardLayout);

    private AssignedTasksPanel assignedPanel;
    private CompletedTasksPanel completedPanel;
    private WishesPanel wishesPanel;

    private Child loggedChild;

    public ChildDashboardPanel(DataManager dm) {
        this.dataManager = dm;

        // Giriş yapan çocuğu buluyoruz
        for (User u : dm.loadUsers()) {
            if (u.getRole() == User.Role.CHILD) {
                loggedChild = (Child) u;
                break;
            }
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Child Dashboard");
        title.setFont(title.getFont().deriveFont(22f));
        add(title, BorderLayout.NORTH);

        JPanel leftMenu = createLeftMenu();
        add(leftMenu, BorderLayout.WEST);

        assignedPanel = new AssignedTasksPanel();
        completedPanel = new CompletedTasksPanel();
        wishesPanel = new WishesPanel();

        centerCards.add(assignedPanel, "ASSIGNED");
        centerCards.add(completedPanel, "COMPLETED");
        centerCards.add(wishesPanel, "WISHES");

        add(centerCards, BorderLayout.CENTER);

        cardLayout.show(centerCards, "ASSIGNED");
    }

    private JPanel createLeftMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(160, 0));

        JButton b1 = new JButton("My Tasks");
        JButton b2 = new JButton("Completed");
        JButton b3 = new JButton("My Wishes");

        b1.setAlignmentX(Component.CENTER_ALIGNMENT);
        b2.setAlignmentX(Component.CENTER_ALIGNMENT);
        b3.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        p.add(Box.createVerticalStrut(10));
        p.add(b1);
        p.add(Box.createVerticalStrut(10));
        p.add(b2);
        p.add(Box.createVerticalStrut(10));
        p.add(b3);
        p.add(Box.createVerticalGlue());

        return p;
    }

    private class AssignedTasksPanel extends JPanel {

        private JTable table;
        private AssignedModel model;
        private JButton complete;

        public AssignedTasksPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("Assigned Tasks"));

            model = new AssignedModel();
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            complete = new JButton("Mark as Completed");
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(complete);
            add(bottom, BorderLayout.SOUTH);

            complete.addActionListener(e -> completeTask());

            reload();
        }

        public void reload() {
            new SwingWorker<List<Task>, Void>() {
                @Override
                protected List<Task> doInBackground() throws Exception {
                    return dataManager.loadTasks();
                }

                @Override
                protected void done() {
                    try {
                        List<Task> tasks = get();
                        model.reload(tasks, loggedChild.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(AssignedTasksPanel.this,
                                "Failed to load tasks.");
                    }
                }
            }.execute();
        }

        private void completeTask() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a task.");
                return;
            }

            Task t = model.getAt(row);
            t.setStatus(Task.Status.COMPLETED);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    List<Task> list = dataManager.loadTasks();
                    for (Task x : list) {
                        if (x.getTaskId().equals(t.getTaskId())) {
                            x.setStatus(Task.Status.COMPLETED);
                        }
                    }
                    return dataManager.saveTasks(list);
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(AssignedTasksPanel.this,
                                    "Task marked as completed!");
                            reload();
                            completedPanel.reload();
                        } else {
                            JOptionPane.showMessageDialog(AssignedTasksPanel.this,
                                    "Failed to save task.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(AssignedTasksPanel.this,
                                "Error while saving task.");
                    }
                }
            }.execute();
        }
    }

    private static class AssignedModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Title", "Due", "Points"};

        public void reload(List<Task> tasks, String childId) {
            list.clear();
            for (Task t : tasks) {
                if (t.getAssignedToId().equals(childId) &&
                        t.getStatus() == Task.Status.PENDING) {
                    list.add(t);
                }
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            if (c == 0) return t.getTitle();
            if (c == 1 && t.getDueDate() != null)
                return t.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            if (c == 2) return t.getPoints();
            return "";
        }

        public Task getAt(int row) { return list.get(row); }
    }


    private class CompletedTasksPanel extends JPanel {

        private JTable table;
        private CompletedModel model;

        public CompletedTasksPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Completed Tasks"));

            model = new CompletedModel();
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            reload();
        }

        public void reload() {
            new SwingWorker<List<Task>, Void>() {
                @Override
                protected List<Task> doInBackground() throws Exception {
                    return dataManager.loadTasks();
                }

                @Override
                protected void done() {
                    try {
                        List<Task> tasks = get();
                        model.reload(tasks, loggedChild.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(CompletedTasksPanel.this,
                                "Failed to load completed tasks.");
                    }
                }
            }.execute();
        }
    }

    private static class CompletedModel extends AbstractTableModel {
        private List<Task> list = new ArrayList<>();
        private final String[] cols = {"Title", "Due", "Status"};

        public void reload(List<Task> tasks, String childId) {
            list.clear();
            for (Task t : tasks) {
                if (t.getAssignedToId().equals(childId) &&
                        (t.getStatus() == Task.Status.COMPLETED ||
                                t.getStatus() == Task.Status.APPROVED)) {
                    list.add(t);
                }
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            if (c == 0) return t.getTitle();
            if (c == 1 && t.getDueDate() != null)
                return t.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            if (c == 2) return t.getStatus();
            return "";
        }
    }


    private class WishesPanel extends JPanel {

        private JTable table;
        private WishModel model;

        public WishesPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createTitledBorder("My Wishes"));

            model = new WishModel();
            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JButton addWish = new JButton("Add Wish");
            addWish.addActionListener(e -> openAddWishDialog());

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(addWish);
            add(bottom, BorderLayout.SOUTH);

            reload();
        }

        private void openAddWishDialog() {
            JTextField name = new JTextField();
            JTextField cost = new JTextField();

            JPanel form = new JPanel(new GridLayout(0,2,5,5));
            form.add(new JLabel("Wish Name:"));
            form.add(name);
            form.add(new JLabel("Cost (Points):"));
            form.add(cost);

            int ok = JOptionPane.showConfirmDialog(this, form, "New Wish", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            try {
                Wish w = new Wish(name.getText(), Integer.parseInt(cost.getText()),
                        loggedChild.getUserId(), loggedChild.getLevel());

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        List<Wish> wi = dataManager.loadWishes();
                        wi.add(w);
                        return dataManager.saveWishes(wi);
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean ok = get();
                            if (ok) {
                                JOptionPane.showMessageDialog(WishesPanel.this, "Wish added!");
                                reload();
                            } else {
                                JOptionPane.showMessageDialog(WishesPanel.this, "Failed to save wish.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(WishesPanel.this, "Error while saving wish.");
                        }
                    }
                }.execute();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }

        public void reload() {
            new SwingWorker<List<Wish>, Void>() {
                @Override
                protected List<Wish> doInBackground() throws Exception {
                    return dataManager.loadWishes();
                }

                @Override
                protected void done() {
                    try {
                        List<Wish> wishes = get();
                        model.reload(wishes, loggedChild.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(WishesPanel.this,
                                "Failed to load wishes.");
                    }
                }
            }.execute();
        }
    }

    private static class WishModel extends AbstractTableModel {
        private List<Wish> list = new ArrayList<>();
        private final String[] cols = {"Name", "Cost", "Status"};

        public void reload(List<Wish> wishes, String childId) {
            list.clear();
            for (Wish w : wishes) {
                if (w.getRequestedById().equals(childId)) list.add(w);
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return list.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Wish w = list.get(r);
            if (c == 0) return w.getName();
            if (c == 1) return w.getCost();
            if (c == 2) return w.getStatus();
            return "";
        }
    }
}
