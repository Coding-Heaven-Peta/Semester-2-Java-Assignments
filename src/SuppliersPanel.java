import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SuppliersPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public SuppliersPanel() {
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Name", "Contact Person", "Phone", "Email", "Address"};
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

        addButton = new JButton("Add Supplier");
        editButton = new JButton("Edit Supplier");
        deleteButton = new JButton("Delete Supplier");
        refreshButton = new JButton("Refresh");

        addButton.setBackground(new Color(0, 153, 0));
        addButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(204, 0, 0));
        deleteButton.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadSuppliers();

        addButton.addActionListener(e -> openAddDialog());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteSupplier());
        refreshButton.addActionListener(e -> loadSuppliers());
    }

    private void loadSuppliers() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM suppliers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
        }
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Supplier");
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        panel.add(new JLabel("Name:"));           panel.add(nameField);
        panel.add(new JLabel("Contact Person:")); panel.add(contactField);
        panel.add(new JLabel("Phone:"));          panel.add(phoneField);
        panel.add(new JLabel("Email:"));          panel.add(emailField);
        panel.add(new JLabel("Address:"));        panel.add(addressField);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.WHITE);
        panel.add(new JLabel(""));
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Supplier name is required.");
                return;
            }

            String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, contactField.getText().trim());
                stmt.setString(3, phoneField.getText().trim());
                stmt.setString(4, emailField.getText().trim());
                stmt.setString(5, addressField.getText().trim());

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Supplier added successfully.");
                dialog.dispose();
                loadSuppliers();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit.");
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Supplier");
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField    = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        JTextField contactField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
        JTextField phoneField   = new JTextField(tableModel.getValueAt(selectedRow, 3).toString());
        JTextField emailField   = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
        JTextField addressField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString());

        panel.add(new JLabel("Name:"));           panel.add(nameField);
        panel.add(new JLabel("Contact Person:")); panel.add(contactField);
        panel.add(new JLabel("Phone:"));          panel.add(phoneField);
        panel.add(new JLabel("Email:"));          panel.add(emailField);
        panel.add(new JLabel("Address:"));        panel.add(addressField);

        JButton updateButton = new JButton("Update");
        updateButton.setBackground(new Color(0, 102, 204));
        updateButton.setForeground(Color.WHITE);
        panel.add(new JLabel(""));
        panel.add(updateButton);

        updateButton.addActionListener(e -> {
            String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, contactField.getText().trim());
                stmt.setString(3, phoneField.getText().trim());
                stmt.setString(4, emailField.getText().trim());
                stmt.setString(5, addressField.getText().trim());
                stmt.setInt(6, supplierId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Supplier updated successfully.");
                dialog.dispose();
                loadSuppliers();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSupplier() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }

        int supplierId = (int) tableModel.getValueAt(selectedRow, 0);
        String supplierName = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + supplierName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, supplierId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully.");
                loadSuppliers();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}