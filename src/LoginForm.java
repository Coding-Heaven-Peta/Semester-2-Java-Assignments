import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginForm() {
        setTitle("HealthFirst PIMS - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);

        // Main panel
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Username row
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        // Password row
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        // Empty cell + Login button
        panel.add(new JLabel(""));
        loginButton = new JButton("Login");
        panel.add(loginButton);

        // Status label for error messages
        JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);

        add(panel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    statusLabel.setText("Please enter both username and password.");
                    return;
                }

                authenticateUser(username, password, statusLabel);
            }
        });

        setVisible(true);
    }

    private void authenticateUser(String username, String password, JLabel statusLabel) {
        String sql = "SELECT role, full_name FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");

                dispose(); // close login window

                if (role.equals("Admin")) {
                    new AdminDashboard(fullName);
                } else {
                    new CashierDashboard(fullName);
                }

            } else {
                statusLabel.setText("Invalid username or password.");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}