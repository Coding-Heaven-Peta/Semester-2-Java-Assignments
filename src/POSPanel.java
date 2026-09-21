import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {

    private JTextField searchField;
    private JButton searchButton, addToCartButton, removeButton, checkoutButton, clearButton;
    private JTable cartTable, searchResultTable;
    private DefaultTableModel cartModel, searchModel;
    private JLabel totalLabel;
    private double total = 0.0;
    private String cashierName;

    // Cart item structure
    private List<int[]> cartItems = new ArrayList<>(); // [medicine_id, quantity]
    private List<double[]> cartPrices = new ArrayList<>(); // [price_at_sale]

    public POSPanel(String cashierName) {
        this.cashierName = cashierName;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Search Panel ──────────────────────────────────────────
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Medicine"));

        searchField = new JTextField();
        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 102, 204));
        searchButton.setForeground(Color.WHITE);

        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        // Search results table
        String[] searchColumns = {"ID", "Name", "Type", "Price (R)", "In Stock"};
        searchModel = new DefaultTableModel(searchColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        searchResultTable = new JTable(searchModel);
        searchResultTable.setRowHeight(22);
        searchResultTable.getTableHeader().setBackground(new Color(0, 102, 204));
        searchResultTable.getTableHeader().setForeground(Color.WHITE);

        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchResultTable), BorderLayout.CENTER);

        // Add to cart button
        addToCartButton = new JButton("Add to Cart →");
        addToCartButton.setBackground(new Color(0, 153, 0));
        addToCartButton.setForeground(Color.WHITE);
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addPanel.add(addToCartButton);
        searchPanel.add(addPanel, BorderLayout.SOUTH);

        // ── Cart Panel ────────────────────────────────────────────
        JPanel cartPanel = new JPanel(new BorderLayout(5, 5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        String[] cartColumns = {"Medicine", "Qty", "Unit Price (R)", "Subtotal (R)"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(22);
        cartTable.getTableHeader().setBackground(new Color(0, 153, 76));
        cartTable.getTableHeader().setForeground(Color.WHITE);

        totalLabel = new JLabel("TOTAL: R 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(0, 102, 204));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        removeButton = new JButton("Remove Item");
        removeButton.setBackground(new Color(204, 0, 0));
        removeButton.setForeground(Color.WHITE);

        checkoutButton = new JButton("✔ Checkout");
        checkoutButton.setBackground(new Color(0, 153, 76));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 13));

        clearButton = new JButton("Clear Cart");

        JPanel cartButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartButtonPanel.add(removeButton);
        cartButtonPanel.add(clearButton);
        cartButtonPanel.add(checkoutButton);

        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(totalLabel, BorderLayout.NORTH);
        cartPanel.add(cartButtonPanel, BorderLayout.SOUTH);

        // ── Split Layout ──────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchPanel, cartPanel);
        splitPane.setDividerLocation(450);

        add(splitPane, BorderLayout.CENTER);

        // ── Button Actions ────────────────────────────────────────
        searchButton.addActionListener(e -> searchMedicine());
        searchField.addActionListener(e -> searchMedicine()); // enter key

        addToCartButton.addActionListener(e -> addToCart());
        removeButton.addActionListener(e -> removeFromCart());
        clearButton.addActionListener(e -> clearCart());
        checkoutButton.addActionListener(e -> checkout());
    }

    private void searchMedicine() {
        searchModel.setRowCount(0);
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a medicine name to search.");
            return;
        }

        String sql = "SELECT medicine_id, name, medicine_type, price, quantity_in_stock " +
                "FROM medicines WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                searchModel.addRow(new Object[]{
                        rs.getInt("medicine_id"),
                        rs.getString("name"),
                        rs.getString("medicine_type"),
                        String.format("%.2f", rs.getDouble("price")),
                        rs.getInt("quantity_in_stock")
                });
            }

            if (searchModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No medicines found matching: " + keyword);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addToCart() {
        int selectedRow = searchResultTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from search results.");
            return;
        }

        int medicineId = (int) searchModel.getValueAt(selectedRow, 0);
        String medicineName = searchModel.getValueAt(selectedRow, 1).toString();
        double price = Double.parseDouble(
                searchModel.getValueAt(selectedRow, 3).toString().replace(",", ".")
        );
        int inStock = (int) searchModel.getValueAt(selectedRow, 4);

        // Ask for quantity
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity for " + medicineName + ":");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
            return;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
            return;
        }

        if (qty > inStock) {
            JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + inStock);
            return;
        }

        double subtotal = qty * price;
        total += subtotal;

        cartModel.addRow(new Object[]{
                medicineName,
                qty,
                String.format("%.2f", price),
                String.format("%.2f", subtotal)
        });

        // Track IDs for checkout
        cartItems.add(new int[]{medicineId, qty});
        cartPrices.add(new double[]{price});

        totalLabel.setText("TOTAL: R " + String.format("%.2f", total));
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }

        double subtotal = Double.parseDouble(
                cartModel.getValueAt(selectedRow, 3).toString().replace(",", ".")
        );
        total -= subtotal;

        cartModel.removeRow(selectedRow);
        cartItems.remove(selectedRow);
        cartPrices.remove(selectedRow);

        totalLabel.setText("TOTAL: R " + String.format("%.2f", total));
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        cartItems.clear();
        cartPrices.clear();
        total = 0.0;
        totalLabel.setText("TOTAL: R 0.00");
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Process sale of R " + String.format("%.2f", total) + "?",
                "Confirm Checkout",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // Step 1: get cashier user_id
            int userId = getUserId(conn);

            // Step 2: insert into sales table
            String saleSql = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
            PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setDouble(1, total);
            saleStmt.setInt(2, userId);
            saleStmt.executeUpdate();

            // Step 3: get the generated sale_id
            ResultSet keys = saleStmt.getGeneratedKeys();
            int saleId = 0;
            if (keys.next()) saleId = keys.getInt(1);

            // Step 4: insert each cart item into sale_items
            // and reduce stock in medicines table
            for (int i = 0; i < cartItems.size(); i++) {
                int medicineId = cartItems.get(i)[0];
                int qty = cartItems.get(i)[1];
                double price = cartPrices.get(i)[0];

                String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, medicineId);
                itemStmt.setInt(3, qty);
                itemStmt.setDouble(4, price);
                itemStmt.executeUpdate();

                // Reduce stock
                String stockSql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
                PreparedStatement stockStmt = conn.prepareStatement(stockSql);
                stockStmt.setInt(1, qty);
                stockStmt.setInt(2, medicineId);
                stockStmt.executeUpdate();
            }

            conn.commit(); // all good — save everything

            // Step 5: show bill
            showBill(saleId);
            clearCart();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // something failed — undo everything
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int getUserId(Connection conn) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE full_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, cashierName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getInt("user_id");
        return 1; // fallback
    }

    private void showBill(int saleId) {
        StringBuilder bill = new StringBuilder();
        bill.append("========================================\n");
        bill.append("         HEALTHFIRST PHARMACY\n");
        bill.append("              RECEIPT\n");
        bill.append("========================================\n");
        bill.append(String.format("Sale ID   : #%d%n", saleId));
        bill.append(String.format("Cashier   : %s%n", cashierName));
        bill.append(String.format("Date      : %s%n", new java.util.Date()));
        bill.append("----------------------------------------\n");
        bill.append(String.format("%-20s %5s %10s%n", "Item", "Qty", "Subtotal"));
        bill.append("----------------------------------------\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name = cartModel.getValueAt(i, 0).toString();
            String qty = cartModel.getValueAt(i, 1).toString();
            String subtotal = cartModel.getValueAt(i, 3).toString();
            bill.append(String.format("%-20s %5s %10s%n", name, qty, "R" + subtotal));
        }

        bill.append("========================================\n");
        bill.append(String.format("TOTAL     : R %.2f%n", total));
        bill.append("========================================\n");
        bill.append("     Thank you for your purchase!\n");
        bill.append("========================================\n");

        JTextArea billArea = new JTextArea(bill.toString());
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        billArea.setEditable(false);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(billArea),
                "Receipt - Sale #" + saleId,
                JOptionPane.INFORMATION_MESSAGE);
    }
}