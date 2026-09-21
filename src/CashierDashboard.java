import javax.swing.*;
import java.awt.*;

public class CashierDashboard extends JFrame {

    private String fullName;

    public CashierDashboard(String fullName) {
        this.fullName = fullName;

        setTitle("HealthFirst PIMS - Cashier (" + fullName + ")");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 153, 76));
        headerPanel.setPreferredSize(new Dimension(900, 50));

        JLabel titleLabel = new JLabel("  HealthFirst Pharmacy - Point of Sale");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel userLabel = new JLabel("Cashier: " + fullName + "  ");
        userLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Point of Sale", new POSPanel(fullName));
        tabbedPane.addTab("Stock Check", new StockCheckPanel());

        // Logout
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