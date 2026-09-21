import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StockCheckPanel extends JPanel {

    private JTextField searchField;
    private DefaultTableModel model;

    public StockCheckPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Check Medicine Availability"));

        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 102, 204));
        searchButton.setForeground(Color.WHITE);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        String[] columns = {"Medicine", "Type", "Price (R)", "In Stock"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchStock());
        searchField.addActionListener(e -> searchStock());
    }

    private void searchStock() {
        model.setRowCount(0);
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a medicine name.");
            return;
        }

        String sql = "SELECT name, medicine_type, price, quantity_in_stock " +
                "FROM medicines WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int stock = rs.getInt("quantity_in_stock");
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("medicine_type"),
                        String.format("R %.2f", rs.getDouble("price")),
                        stock == 0 ? "OUT OF STOCK" : stock
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}