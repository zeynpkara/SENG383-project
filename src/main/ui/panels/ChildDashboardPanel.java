package ui.panels;

import model.*;
import persistence.DataManager;
import ui.MainApp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChildDashboardPanel extends JPanel {

    private final DataManager dataManager;
    private final MainApp mainApp;
    private final Child loggedChild;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(cardLayout);

    private HomePanel homePanel;
    private AssignedTasksPanel assignedPanel;
    private CompletedTasksPanel completedPanel;
    private WishesPanel wishesPanel;

    public ChildDashboardPanel(DataManager dm, MainApp app) {
        this.dataManager = dm;
        this.mainApp = app;

        User user = app.getLoggedUser();
        if (user == null || user.getRole() != User.Role.CHILD) {
            JOptionPane.showMessageDialog(this, "No child user logged in!");
            throw new IllegalStateException();
        }

        this.loggedChild = new Child(
                user.getUserId(),
                user.getEmail(),
                user.getPassword()
        );

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(createTopBar(), BorderLayout.NORTH);
        add(createLeftMenu(), BorderLayout.WEST);

        homePanel = new HomePanel();
        assignedPanel = new AssignedTasksPanel();
        completedPanel = new CompletedTasksPanel();
        wishesPanel = new WishesPanel();

        centerCards.add(homePanel, "HOME");
        centerCards.add(assignedPanel, "ASSIGNED");
        centerCards.add(completedPanel, "COMPLETED");
        centerCards.add(wishesPanel, "WISHES");

        add(centerCards, BorderLayout.CENTER);
        cardLayout.show(centerCards, "HOME");
    }


    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Child Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JButton switchRole = new JButton("Switch Role");
        switchRole.addActionListener(e -> mainApp.showRoleSelect());

        top.add(title, BorderLayout.WEST);
        top.add(switchRole, BorderLayout.EAST);
        return top;
    }


    private JPanel createLeftMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(160, 0));

        JButton home = new JButton("Home");
        JButton tasks = new JButton("My Tasks");
        JButton completed = new JButton("Completed");
        JButton wishes = new JButton("My Wishes");

        home.addActionListener(e -> { homePanel.reload(); cardLayout.show(centerCards, "HOME"); });
        tasks.addActionListener(e -> { assignedPanel.reload(); cardLayout.show(centerCards, "ASSIGNED"); });
        completed.addActionListener(e -> { completedPanel.reload(); cardLayout.show(centerCards, "COMPLETED"); });
        wishes.addActionListener(e -> { wishesPanel.reload(); cardLayout.show(centerCards, "WISHES"); });

        for (JButton b : List.of(home, tasks, completed, wishes)) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(Box.createVerticalStrut(10));
            p.add(b);
        }

        p.add(Box.createVerticalGlue());
        return p;
    }


    private class HomePanel extends JPanel {

        private JLabel pointsLbl = new JLabel();
        private JLabel motivationLbl = new JLabel();
        private JProgressBar progressBar = new JProgressBar(0,100);
        private JPanel cardsPanel = new JPanel(new GridLayout(1,4,15,10));

        HomePanel() {
            setLayout(new BorderLayout(15,15));
            setBorder(BorderFactory.createTitledBorder("Overview"));

            JPanel hero = new JPanel();
            hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
            hero.setBackground(new Color(235,245,255));
            hero.setOpaque(true);
            hero.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            JLabel welcome = new JLabel("🌟 Welcome back!");
            welcome.setFont(new Font("Arial", Font.BOLD, 20));

            progressBar.setStringPainted(true);

            hero.add(welcome);
            hero.add(Box.createVerticalStrut(5));
            hero.add(pointsLbl);
            hero.add(Box.createVerticalStrut(5));
            hero.add(progressBar);
            hero.add(Box.createVerticalStrut(5));
            hero.add(motivationLbl);

            add(hero, BorderLayout.NORTH);
            add(cardsPanel, BorderLayout.CENTER);
            add(new MiniChartPanel(), BorderLayout.SOUTH);

            reload();
        }

        void reload() {
            long pending = dataManager.loadTasks().stream()
                    .filter(t -> t.getAssignedToId().equals(loggedChild.getUserId()))
                    .filter(t -> t.getStatus() == Task.Status.PENDING)
                    .count();

            long completed = dataManager.loadTasks().stream()
                    .filter(t -> t.getAssignedToId().equals(loggedChild.getUserId()))
                    .filter(t -> t.getStatus() != Task.Status.PENDING)
                    .count();

            long wishCount = dataManager.loadWishes().stream()
                    .filter(w -> w.getRequestedById().equals(loggedChild.getUserId()))
                    .count();

            int pts = loggedChild.getTotalPoints();

            pointsLbl.setText("⭐ Points: " + pts + " | Level: " + loggedChild.getLevel());
            progressBar.setValue(pts % 100);

            motivationLbl.setText(
                    pts < 50 ? "💪 Keep going!"
                            : pts < 150 ? "🔥 Great job!"
                            : "🏆 Superstar!"
            );

            cardsPanel.removeAll();
            cardsPanel.add(infoCard("📌 Pending", pending, new Color(220,235,255)));
            cardsPanel.add(infoCard("✅ Completed", completed, new Color(220,255,230)));
            cardsPanel.add(infoCard("🎁 Wishes", wishCount, new Color(245,220,255)));
            cardsPanel.add(infoCard("⭐ Points", pts, new Color(255,245,200)));

            cardsPanel.revalidate();
            cardsPanel.repaint();
        }

        private JPanel infoCard(String title, long value, Color bg) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(bg);
            p.setOpaque(true);
            p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

            JLabel t = new JLabel(title);
            JLabel v = new JLabel(String.valueOf(value));
            v.setFont(new Font("Arial", Font.BOLD, 22));

            t.setAlignmentX(Component.CENTER_ALIGNMENT);
            v.setAlignmentX(Component.CENTER_ALIGNMENT);

            p.add(t);
            p.add(Box.createVerticalStrut(5));
            p.add(v);
            return p;
        }
    }


    private class MiniChartPanel extends JPanel {
        private final int[] data = {1,2,3,2,4,3,5};

        MiniChartPanel() {
            setPreferredSize(new Dimension(300,140));
            setBorder(BorderFactory.createTitledBorder("📈 Weekly Activity"));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            int bw = w / (data.length * 2);
            int x = bw;

            for (int v : data) {
                g.setColor(new Color(120,170,220));
                g.fillRect(x, h - v*20 - 20, bw, v*20);
                x += bw * 2;
            }
        }
    }


    private class AssignedTasksPanel extends JPanel {
        private TaskTableModel model = new TaskTableModel(false);

        AssignedTasksPanel() {
            setLayout(new BorderLayout());
            add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
            reload();
        }

        void reload() {
            model.reload(dataManager.loadTasks(), loggedChild.getUserId());
        }
    }

    private class CompletedTasksPanel extends JPanel {
        private TaskTableModel model = new TaskTableModel(true);

        CompletedTasksPanel() {
            setLayout(new BorderLayout());
            add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
            reload();
        }

        void reload() {
            model.reload(dataManager.loadTasks(), loggedChild.getUserId());
        }
    }

    private static class TaskTableModel extends AbstractTableModel {
        private final boolean completedOnly;
        private final String[] cols = {"Title", "Due Date", "Points", "Status"};
        private List<Task> list = new ArrayList<>();

        TaskTableModel(boolean completedOnly) {
            this.completedOnly = completedOnly;
        }

        void reload(List<Task> tasks, String childId) {
            list.clear();
            for (Task t : tasks) {
                if (!t.getAssignedToId().equals(childId)) continue;
                if (completedOnly && t.getStatus() == Task.Status.PENDING) continue;
                if (!completedOnly && t.getStatus() != Task.Status.PENDING) continue;
                list.add(t);
            }
            fireTableDataChanged();
        }

        public int getRowCount() { return list.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Task t = list.get(r);
            return switch (c) {
                case 0 -> t.getTitle();
                case 1 -> t.getDueDate();
                case 2 -> t.getPoints();
                default -> t.getStatus();
            };
        }
    }


    private class WishesPanel extends JPanel {

        private JPanel listPanel = new JPanel();
        private JScrollPane scroll;

        WishesPanel() {
            setLayout(new BorderLayout(10,10));
            setBorder(BorderFactory.createTitledBorder("My Wishes"));

            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            scroll = new JScrollPane(listPanel);
            add(scroll, BorderLayout.CENTER);

            JButton add = new JButton("➕ Request New Wish");
            add.addActionListener(e -> createWish());

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(add);
            add(bottom, BorderLayout.SOUTH);

            reload();
        }

        void reload() {
            listPanel.removeAll();

            for (Wish w : dataManager.loadWishes()) {
                if (!w.getRequestedById().equals(loggedChild.getUserId())) continue;

                JPanel card = new JPanel(new BorderLayout(5,5));
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                card.setBackground(getColorForStatus(w.getStatus()));

                JLabel titleLabel = new JLabel("🎁 " + w.getName() + " | Cost: " + w.getCost() + " pts");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

                JLabel statusLabel = new JLabel(getStatusText(w.getStatus()));
                statusLabel.setOpaque(true);
                statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
                statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
                statusLabel.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));
                statusLabel.setBackground(getColorForStatus(w.getStatus()).darker());

                card.add(titleLabel, BorderLayout.WEST);
                card.add(statusLabel, BorderLayout.EAST);

                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        card.setBackground(getColorForStatus(w.getStatus()).brighter());
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        card.setBackground(getColorForStatus(w.getStatus()));
                    }
                });

                listPanel.add(Box.createVerticalStrut(5));
                listPanel.add(card);
            }

            listPanel.revalidate();
            listPanel.repaint();
        }

        private Color getColorForStatus(Wish.Status status) {
            return switch(status) {
                case PENDING -> new Color(255, 255, 200);   // açık sarı
                case APPROVED -> new Color(200, 255, 200);  // açık yeşil
                case REJECTED -> new Color(255, 200, 200);  // açık kırmızı
            };
        }

        private String getStatusText(Wish.Status status) {
            return switch(status) {
                case PENDING -> "⏳ Pending";
                case APPROVED -> "✅ Approved";
                case REJECTED -> "❌ Rejected";
            };
        }

        private void createWish() {
            JTextField titleField = new JTextField();
            JTextField costField = new JTextField();

            Object[] msg = {
                    "Wish Title:", titleField,
                    "Cost:", costField
            };

            if (JOptionPane.showConfirmDialog(
                    this, msg, "New Wish",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                try {
                    String title = titleField.getText().trim();
                    int cost = Integer.parseInt(costField.getText().trim());

                    Wish wish = new Wish(
                            title,
                            cost,
                            loggedChild.getUserId(),
                            0
                    );


                    dataManager.addWish(wish);
                    reload();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input!");
                }
            }
        }
    }
}
