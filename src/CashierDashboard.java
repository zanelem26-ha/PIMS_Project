import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class CashierDashboard {

    private double totalAmount = 0;

    public CashierDashboard(String cashierName) {

        JFrame frame = new JFrame("Cashier POS - " + cashierName);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        //main panel
        JPanel topPanel = new JPanel(new GridLayout(2, 5, 10, 10));

        JComboBox<Medicine> cmbMedicines = new JComboBox<>();
        JTextField txtQuantity = new JTextField();
        JButton btnAddToCart = new JButton("Add to Cart");

        JLabel lblPrice = new JLabel("Price: ");
        JLabel lblStock = new JLabel("Stock: ");

        topPanel.add(new JLabel("Medicine:"));
        topPanel.add(cmbMedicines);
        topPanel.add(new JLabel("Quantity:"));
        topPanel.add(txtQuantity);
        topPanel.add(btnAddToCart);

        topPanel.add(lblPrice);
        topPanel.add(lblStock);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel(""));

        panel.add(topPanel, BorderLayout.NORTH);

        // cart table
        String[] columns = {"Medicine", "Price", "Quantity", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // price and checkout panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel lblTotal = new JLabel("Total: R0.00");
        JButton btnCheckout = new JButton("Checkout");
        bottomPanel.add(lblTotal, BorderLayout.WEST);
        bottomPanel.add(btnCheckout, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);


        loadMedicines(cmbMedicines, lblPrice, lblStock);

        // show selected item and price
        cmbMedicines.addActionListener(e -> {
            Medicine med = (Medicine) cmbMedicines.getSelectedItem();
            if (med != null) {
                lblPrice.setText("Price: R" + med.getPrice());
                lblStock.setText("Stock: " + med.getQuantity());
            }
        });

        // add to cart button
        btnAddToCart.addActionListener(e -> {
            try {
                Medicine selectedMed = (Medicine) cmbMedicines.getSelectedItem();
                int quantity = Integer.parseInt(txtQuantity.getText());

                if (quantity > selectedMed.getQuantity()) {
                    JOptionPane.showMessageDialog(null, "Not enough stock!");
                    return;
                }

                double price = selectedMed.getPrice();
                double itemTotal = price * quantity;


                model.addRow(new Object[]{
                        selectedMed.toString(),
                        price,
                        quantity,
                        itemTotal
                });


                totalAmount += itemTotal;
                lblTotal.setText("Total: R" + totalAmount);

                txtQuantity.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Enter a valid quantity!");
            }
        });

        // checkout button
        btnCheckout.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Cart is empty!");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {

                // safely query data
                String saleSql = "INSERT INTO sales (sale_date, total_amount, user_id) VALUES (NOW(), ?, ?)";
                PreparedStatement pstSale = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);


                pstSale.setDouble(1, totalAmount);
                pstSale.setInt(2, 2);
                pstSale.executeUpdate();

                ResultSet rsKeys = pstSale.getGeneratedKeys();
                rsKeys.next();
                int saleId = rsKeys.getInt(1);


                String itemSql = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
                PreparedStatement pstItem = conn.prepareStatement(itemSql);

                String updateStockSql = "UPDATE medicines SET quantity_in_stock=? WHERE medicine_id=?";
                PreparedStatement pstUpdateStock = conn.prepareStatement(updateStockSql);

                ArrayList<String> billLines = new ArrayList<>();
                billLines.add("Bill for Cashier: " + cashierName);
                billLines.add("Date: " + new Date());
                billLines.add("-----------------------------------");
                billLines.add(String.format("%-15s%-5s%-8s%-8s", "Medicine", "Qty", "Price", "Total"));

                for (int i = 0; i < model.getRowCount(); i++) {
                    String medName = model.getValueAt(i, 0).toString();
                    double price = (double) model.getValueAt(i, 1);
                    int qty = (int) model.getValueAt(i, 2);
                    double total = (double) model.getValueAt(i, 3);


                    int medicineId = getMedicineIdByName(conn, medName);

                    pstItem.setInt(1, saleId);
                    pstItem.setInt(2, medicineId);
                    pstItem.setInt(3, qty);
                    pstItem.setDouble(4, price);
                    pstItem.executeUpdate();


                    int currentStock = getMedicineQuantity(conn, medicineId);
                    pstUpdateStock.setInt(1, currentStock - qty);
                    pstUpdateStock.setInt(2, medicineId);
                    pstUpdateStock.executeUpdate();

                    billLines.add(String.format("%-15s%-5d%-8.2f%-8.2f", medName, qty, price, total));
                }

                billLines.add("-----------------------------------");
                billLines.add("TOTAL: R" + totalAmount);


                JTextArea txtBill = new JTextArea();
                for (String line : billLines) txtBill.append(line + "\n");
                JOptionPane.showMessageDialog(frame, new JScrollPane(txtBill), "Bill", JOptionPane.INFORMATION_MESSAGE);

                model.setRowCount(0);
                totalAmount = 0;
                lblTotal.setText("Total: R0.00");


                loadMedicines(cmbMedicines, lblPrice, lblStock);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    private void loadMedicines(JComboBox<Medicine> comboBox, JLabel lblPrice, JLabel lblStock) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM medicines";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            comboBox.removeAllItems();

            while (rs.next()) {
                Medicine med = new Medicine(
                        rs.getInt("medicine_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_in_stock")
                );
                comboBox.addItem(med);
            }

            if (comboBox.getItemCount() > 0) {
                Medicine first = comboBox.getItemAt(0);
                lblPrice.setText("Price: R" + first.getPrice());
                lblStock.setText("Stock: " + first.getQuantity());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int getMedicineIdByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT medicine_id FROM medicines WHERE name=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, name);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt("medicine_id");
    }


    private int getMedicineQuantity(Connection conn, int medicineId) throws SQLException {
        String sql = "SELECT quantity_in_stock FROM medicines WHERE medicine_id=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, medicineId);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt("quantity_in_stock");
    }


    class Medicine {
        private int id;
        private String name;
        private double price;
        private int quantity;

        public Medicine(int id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public int getId() { return id; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }

        @Override
        public String toString() { return name; }
    }


}