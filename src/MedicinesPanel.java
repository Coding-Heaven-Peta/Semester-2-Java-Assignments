import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MedicinesPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public MedicinesPanel() {
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = {"ID", "Name", "Company", "Type", "Price", "Quantity", "Reorder Level", "Expiry Date", "Supplier ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // makes table read-only
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);

        // Button panel
        addButton = new JButton("Add Medicine");
        editButton = new JButton("Edit Medicine");
        deleteButton = new JButton("Delete Medicine");
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

        // Load data on startup
        loadMedicines();

        // Button actions
        addButton.addActionListener(e -> openAddDialog());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteMedicine());
        refreshButton.addActionListener(e -> loadMedicines());
    }

    private void loadMedicines() {
        tableModel.setRowCount(0); // clear existing rows
        String sql = "SELECT * FROM medicines";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("medicine_id"),
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("medicine_type"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_in_stock"),
                        rs.getInt("reorder_level"),
                        rs.getString("expiry_date"),
                        rs.getInt("supplier_id")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
        }
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Medicine");
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JTextField companyField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream"});
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField reorderField = new JTextField();
        JTextField expiryField = new JTextField(); // format: YYYY-MM-DD
        JTextField supplierField = new JTextField();

        panel.add(new JLabel("Name:"));           panel.add(nameField);
        panel.add(new JLabel("Company:"));         panel.add(companyField);
        panel.add(new JLabel("Type:"));            panel.add(typeBox);
        panel.add(new JLabel("Price:"));           panel.add(priceField);
        panel.add(new JLabel("Quantity:"));        panel.add(quantityField);
        panel.add(new JLabel("Reorder Level:"));   panel.add(reorderField);
        panel.add(new JLabel("Expiry (YYYY-MM-DD):")); panel.add(expiryField);
        panel.add(new JLabel("Supplier ID:"));     panel.add(supplierField);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.WHITE);
        panel.add(new JLabel(""));
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            // Input validation
            if (nameField.getText().trim().isEmpty() ||
                    priceField.getText().trim().isEmpty() ||
                    quantityField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name, Price and Quantity are required.");
                return;
            }

            String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, companyField.getText().trim());
                stmt.setString(3, typeBox.getSelectedItem().toString());
                stmt.setDouble(4, Double.parseDouble(priceField.getText().trim()));
                stmt.setInt(5, Integer.parseInt(quantityField.getText().trim()));
                stmt.setInt(6, Integer.parseInt(reorderField.getText().trim()));
                stmt.setString(7, expiryField.getText().trim());
                stmt.setInt(8, Integer.parseInt(supplierField.getText().trim()));

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Medicine added successfully.");
                dialog.dispose();
                loadMedicines(); // refresh table

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price, Quantity, Reorder Level and Supplier ID must be numbers.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to edit.");
            return;
        }

        int medicineId = (int) tableModel.getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Medicine");
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(tableModel.getValueAt(selectedRow, 1).toString());
        JTextField companyField = new JTextField(tableModel.getValueAt(selectedRow, 2).toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream"});
        typeBox.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
        JTextField priceField = new JTextField(tableModel.getValueAt(selectedRow, 4).toString());
        JTextField quantityField = new JTextField(tableModel.getValueAt(selectedRow, 5).toString());
        JTextField reorderField = new JTextField(tableModel.getValueAt(selectedRow, 6).toString());
        JTextField expiryField = new JTextField(tableModel.getValueAt(selectedRow, 7).toString());
        JTextField supplierField = new JTextField(tableModel.getValueAt(selectedRow, 8).toString());

        panel.add(new JLabel("Name:"));           panel.add(nameField);
        panel.add(new JLabel("Company:"));         panel.add(companyField);
        panel.add(new JLabel("Type:"));            panel.add(typeBox);
        panel.add(new JLabel("Price:"));           panel.add(priceField);
        panel.add(new JLabel("Quantity:"));        panel.add(quantityField);
        panel.add(new JLabel("Reorder Level:"));   panel.add(reorderField);
        panel.add(new JLabel("Expiry (YYYY-MM-DD):")); panel.add(expiryField);
        panel.add(new JLabel("Supplier ID:"));     panel.add(supplierField);

        JButton updateButton = new JButton("Update");
        updateButton.setBackground(new Color(0, 102, 204));
        updateButton.setForeground(Color.WHITE);
        panel.add(new JLabel(""));
        panel.add(updateButton);

        updateButton.addActionListener(e -> {
            String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, companyField.getText().trim());
                stmt.setString(3, typeBox.getSelectedItem().toString());
                stmt.setDouble(4, Double.parseDouble(priceField.getText().trim()));
                stmt.setInt(5, Integer.parseInt(quantityField.getText().trim()));
                stmt.setInt(6, Integer.parseInt(reorderField.getText().trim()));
                stmt.setString(7, expiryField.getText().trim());
                stmt.setInt(8, Integer.parseInt(supplierField.getText().trim()));
                stmt.setInt(9, medicineId);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Medicine updated successfully.");
                dialog.dispose();
                loadMedicines();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteMedicine() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete.");
            return;
        }

        int medicineId = (int) tableModel.getValueAt(selectedRow, 0);
        String medicineName = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + medicineName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM medicines WHERE medicine_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, medicineId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Medicine deleted successfully.");
                loadMedicines();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}