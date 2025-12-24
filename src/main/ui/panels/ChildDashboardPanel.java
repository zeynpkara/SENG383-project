package ui.panels;

import model.*;
import persistence.DataManager;
import ui.MainApp;

import javax.swing.*;
import java.awt.*;
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

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        private JLabel pointsLbl;
        private JLabel motivationLbl;
        private JProgressBar progressBar;
        private JPanel cardsPanel;

        HomePanel() {
            setLayout(new BorderLayout(15, 15));
            setBorder(BorderFactory.createTitledBorder("Overview"));

            JPanel hero = new JPanel();
            hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
            hero.setBackground(new Color(235, 245, 255));
            hero.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            JLabel welcome = new JLabel("🌟 Welcome back!");
            welcome.setFont(new Font("Arial", Font.BOLD, 20));

            pointsLbl = new JLabel();
            pointsLbl.setFont(new Font("Arial", Font.PLAIN, 16));

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setForeground(new Color(100, 170, 100));

            motivationLbl = new JLabel();
            motivationLbl.setFont(new Font("Arial", Font.ITALIC, 14));

            hero.add(welcome);
            hero.add(Box.createVerticalStrut(5));
            hero.add(pointsLbl);
            hero.add(Box.createVerticalStrut(5));
            hero.add(progressBar);
            hero.add(Box.createVerticalStrut(5));
            hero.add(motivationLbl);

            add(hero, BorderLayout.NORTH);

            cardsPanel = new JPanel(new GridLayout(1, 4, 15, 10));
            add(cardsPanel, BorderLayout.CENTER);

            add(new MiniChartPanel(), BorderLayout.SOUTH);

            reload();
        }

        private JPanel card(String title, String value, Color bg) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(bg);
            p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Arial", Font.BOLD, 14));
            JLabel v = new JLabel(value);
            v.setFont(new Font("Arial", Font.BOLD, 22));

            t.setAlignmentX(Component.CENTER_ALIGNMENT);
            v.setAlignmentX(Component.CENTER_ALIGNMENT);

            p.add(t);
            p.add(Box.createVerticalStrut(5));
            p.add(v);
            return p;
        }

        void reload() {
            long pending = dataManager.loadTasks().stream()
                    .filter(t -> t.getAssignedToId().equals(loggedChild.getUserId()))
                    .filter(t -> t.getStatus() == Task.Status.PENDING).count();

            long completed = dataManager.loadTasks().stream()
                    .filter(t -> t.getAssignedToId().equals(loggedChild.getUserId()))
                    .filter(t -> t.getStatus() != Task.Status.PENDING).count();

            long wishCount = dataManager.loadWishes().stream()
                    .filter(w -> w.getRequestedById().equals(loggedChild.getUserId()))
                    .count();

            int pts = loggedChild.getTotalPoints();

            pointsLbl.setText("⭐ Points: " + pts + " | 🏆 Level: " + loggedChild.getLevel());
            progressBar.setValue(pts % 100);

            motivationLbl.setText(
                    pts < 50 ? "💪 Keep going, you’re doing great!"
                            : pts < 150 ? "🔥 Amazing progress!"
                            : "🏆 You are a superstar!"
            );

            cardsPanel.removeAll();
            cardsPanel.add(card("📌 Pending", String.valueOf(pending), new Color(220,235,255)));
            cardsPanel.add(card("✅ Completed", String.valueOf(completed), new Color(220,255,230)));
            cardsPanel.add(card("🎁 Wishes", String.valueOf(wishCount), new Color(245,220,255)));
            cardsPanel.add(card("⭐ Points", String.valueOf(pts), new Color(255,245,200)));

            revalidate();
            repaint();
        }
    }


    private class MiniChartPanel extends JPanel {

        private final int[] data = {1, 2, 3, 2, 4, 3, 5};

        MiniChartPanel() {
            setPreferredSize(new Dimension(300, 150));
            setBorder(BorderFactory.createTitledBorder("📈 Weekly Activity"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();
            int barW = w / (data.length * 2);
            int x = barW;

            for (int v : data) {
                int barH = v * 20;
                g.setColor(new Color(120, 170, 220));
                g.fillRect(x, h - barH - 20, barW, barH);
                x += barW * 2;
            }
        }
    }


    private class AssignedTasksPanel extends JPanel {
        public void reload() {}
    }

    private class CompletedTasksPanel extends JPanel {
        public void reload() {}
    }

    private class WishesPanel extends JPanel {
        public void reload() {}
    }
}
