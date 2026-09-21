import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private String fullName;

    public AdminDashboard(String fullName) {
        this.fullName = fullName;

        setTitle("HealthFirst PIMS - Admin Dashboard (" + fullName + ")");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(900, 50));

        JLabel titleLabel = new JLabel("  HealthFirst Pharmacy - Admin Panel");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel userLabel = new JLabel("Logged in as: " + fullName + "  ");
        userLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        // Tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Medicines", new MedicinesPanel());
        tabbedPane.addTab("Manage Suppliers", new SuppliersPanel());
        tabbedPane.addTab("Manage Users", new UsersPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginForm();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}