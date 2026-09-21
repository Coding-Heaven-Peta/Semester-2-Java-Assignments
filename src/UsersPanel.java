import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UsersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, refreshButton;

    public UsersPanel() {
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Username", "Full Name", "Role"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);

        addButton = new JButton("Add User");
        deleteButton = new JButton("Delete User");
        refreshButton = new JButton("Refresh");

        addButton.setBackground(new Color(0, 153, 0));
        addButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(204, 0, 0));
        deleteButton.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadUsers();

        addButton.addActionListener(e -> openAddDialog());
        deleteButton.addActionListener(e -> deleteUser());
        refreshButton.addActionListener(e -> loadUsers());
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        // Note: we never display passwords in the table for security
        String sql = "SELECT user_id, username, full_name, role FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New User");
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Cashier"});

        panel.add(new JLabel("Username:"));  panel.add(usernameField);
        panel.add(new JLabel("Password:"));  panel.add(passwordField);
        panel.add(new JLabel("Full Name:")); panel.add(fullNameField);
        panel.add(new JLabel("Role:"));      panel.add(roleBox);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.WHITE);
        panel.add(new JLabel(""));
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String fullName = fullNameField.getText().trim();
            String role = roleBox.getSelectedItem().toString();

            // Input validation
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters.");
                return;
            }

            String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.setString(4, fullName);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "User added successfully.");
                dialog.dispose();
                loadUsers();

            } catch (SQLException ex) {
                // Check for duplicate username
                if (ex.getMessage().contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(dialog, "Username already exists. Choose a different one.");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = tableModel.getValueAt(selectedRow, 1).toString();
        String role = tableModel.getValueAt(selectedRow, 3).toString();

        // Prevent deleting the last admin
        if (role.equals("Admin")) {
            JOptionPane.showMessageDialog(this,
                    "Warning: You are deleting an Admin account. Make sure at least one Admin remains.");
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user: " + username + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadUsers();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}