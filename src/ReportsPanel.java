import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {

    private JTabbedPane reportTabs;

    public ReportsPanel() {
        setLayout(new BorderLayout());

        reportTabs = new JTabbedPane();

        reportTabs.addTab("Sales Report", createSalesReport());
        reportTabs.addTab("Item-Wise Report", createItemWiseReport());
        reportTabs.addTab("Low Stock Report", createLowStockReport());
        reportTabs.addTab("Expiry Report", createExpiryReport());

        add(reportTabs, BorderLayout.CENTER);
    }

    // ── Report 1: Sales Report ──────────────────────────────────────
    private JPanel createSalesReport() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Sale ID", "Date", "Total Amount (R)", "Processed By"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSalesReport(model));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadSalesReport(model);
        return panel;
    }

    private void loadSalesReport(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name " +
                "FROM sales s JOIN users u ON s.user_id = u.user_id " +
                "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("sale_id"),
                        rs.getString("sale_date"),
                        String.format("R %.2f", rs.getDouble("total_amount")),
                        rs.getString("full_name")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales report: " + e.getMessage());
        }
    }

    // ── Report 2: Item-Wise Sales Report ───────────────────────────
    private JPanel createItemWiseReport() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Medicine", "Total Units Sold", "Total Revenue (R)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadItemWiseReport(model));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadItemWiseReport(model);
        return panel;
    }

    private void loadItemWiseReport(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT m.name, " +
                "SUM(si.quantity_sold) AS total_units, " +
                "SUM(si.quantity_sold * si.price_at_sale) AS total_revenue " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "GROUP BY m.name " +
                "ORDER BY total_units DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getInt("total_units"),
                        String.format("R %.2f", rs.getDouble("total_revenue"))
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading item-wise report: " + e.getMessage());
        }
    }

    // ── Report 3: Low Stock Report ─────────────────────────────────
    private JPanel createLowStockReport() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Medicine", "Type", "Current Stock", "Reorder Level", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadLowStockReport(model));

        JLabel noteLabel = new JLabel("  ⚠ Medicines at or below their reorder level");
        noteLabel.setForeground(new Color(204, 102, 0));
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshButton);
        topPanel.add(noteLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadLowStockReport(model);
        return panel;
    }

    private void loadLowStockReport(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT name, medicine_type, quantity_in_stock, reorder_level " +
                "FROM medicines " +
                "WHERE quantity_in_stock <= reorder_level " +
                "ORDER BY quantity_in_stock ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int stock = rs.getInt("quantity_in_stock");
                int reorder = rs.getInt("reorder_level");
                String status = stock == 0 ? "OUT OF STOCK" : "LOW STOCK";

                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("medicine_type"),
                        stock,
                        reorder,
                        status
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading low stock report: " + e.getMessage());
        }
    }

    // ── Report 4: Expiry Report ────────────────────────────────────
    private JPanel createExpiryReport() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Medicine", "Type", "Quantity", "Expiry Date", "Days Until Expiry"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadExpiryReport(model));

        JLabel noteLabel = new JLabel("  ⚠ Medicines expiring within the next 30 days");
        noteLabel.setForeground(new Color(204, 0, 0));
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshButton);
        topPanel.add(noteLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadExpiryReport(model);
        return panel;
    }

    private void loadExpiryReport(DefaultTableModel model) {
        model.setRowCount(0);
        // CURDATE() = today, INTERVAL 30 DAY = next 30 days
        String sql = "SELECT name, medicine_type, quantity_in_stock, expiry_date, " +
                "DATEDIFF(expiry_date, CURDATE()) AS days_left " +
                "FROM medicines " +
                "WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                "ORDER BY expiry_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("medicine_type"),
                        rs.getInt("quantity_in_stock"),
                        rs.getString("expiry_date"),
                        rs.getInt("days_left") + " days"
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading expiry report: " + e.getMessage());
        }
    }

    // ── Shared table styling ───────────────────────────────────────
    private void styleTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(25);
    }
}