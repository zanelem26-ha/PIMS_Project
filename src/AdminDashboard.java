import com.mysql.cj.xdevapi.Column;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard {

    public AdminDashboard(String fullName) {

        JFrame frame = new JFrame("Admin Dashboard");

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel panel = new JPanel(new BorderLayout());


        JLabel lblWelcome = new JLabel("Welcome Admin: " + fullName);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(lblWelcome, BorderLayout.NORTH);


        JTabbedPane tabbedPane = new JTabbedPane();


        tabbedPane.addTab("Manage Medicines", createMedicinesPanel());
        tabbedPane.addTab("Manage Suppliers", createSuppliersPanel());
        tabbedPane.addTab("Manage Users", createUsersPanel());

        panel.add(tabbedPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);


    }



    // Medicine Panel
    private JPanel createMedicinesPanel() {

        JPanel panel = new JPanel(new BorderLayout());


        JPanel formPanel = new JPanel(new GridLayout(4, 4, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtCompany = new JTextField();
        JTextField txtType = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtQuantity = new JTextField();

        JButton btnAdd = new JButton("Add Medicine");



        btnAdd.addActionListener(e -> {

            try {
                String name = txtName.getText();
                String company = txtCompany.getText();
                String type = txtType.getText();
                double price = Double.parseDouble(txtPrice.getText());
                int quantity = Integer.parseInt(txtQuantity.getText());

                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, name);
                pst.setString(2, company);
                pst.setString(3, type);
                pst.setDouble(4, price);
                pst.setInt(5, quantity);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "Medicine added successfully!");


                // Clears fields
                txtName.setText("");
                txtCompany.setText("");
                txtType.setText("");
                txtPrice.setText("");
                txtQuantity.setText("");



                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error adding medicine!");
            }

        });

        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);

        formPanel.add(new JLabel("Company:"));
        formPanel.add(txtCompany);

        formPanel.add(new JLabel("Type:"));
        formPanel.add(txtType);

        formPanel.add(new JLabel("Price:"));
        formPanel.add(txtPrice);

        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(txtQuantity);

        formPanel.add(new JLabel());
        formPanel.add(btnAdd);



        panel.add(formPanel, BorderLayout.NORTH);

        // medicine table
        String[] columns = {"ID", "Name", "Company", "Type", "Price", "Stock"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int selectedRow = table.getSelectedRow();

                txtName.setText(model.getValueAt(selectedRow, 1).toString());
                txtCompany.setText(model.getValueAt(selectedRow, 2).toString());
                txtType.setText(model.getValueAt(selectedRow, 3).toString());
                txtPrice.setText(model.getValueAt(selectedRow, 4).toString());
                txtQuantity.setText(model.getValueAt(selectedRow, 5).toString());
            }
        });

        loadMedicines(model);


        JButton btnDelete = new JButton("Delete Medicine");

        btnDelete.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a medicine to delete!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this medicine?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                try {
                    int medicineId = (int) model.getValueAt(selectedRow, 0);

                    Connection conn = DBConnection.getConnection();

                    String sql = "DELETE FROM medicines WHERE medicine_id=?";
                    PreparedStatement pst = conn.prepareStatement(sql);

                    pst.setInt(1, medicineId);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Medicine deleted successfully!");

                    loadMedicines(model);

                    conn.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        formPanel.add(btnDelete);

        JButton btnUpdate = new JButton("Update Medicine");

        btnUpdate.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a medicine to update!");
                return;
            }

            try {
                int medicineId = (int) model.getValueAt(selectedRow, 0);

                String name = txtName.getText();
                String company = txtCompany.getText();
                String type = txtType.getText();
                double price = Double.parseDouble(txtPrice.getText());
                int quantity = Integer.parseInt(txtQuantity.getText());

                Connection conn = DBConnection.getConnection();

                String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=? WHERE medicine_id=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, name);
                pst.setString(2, company);
                pst.setString(3, type);
                pst.setDouble(4, price);
                pst.setInt(5, quantity);
                pst.setInt(6, medicineId);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "Medicine updated successfully!");

                loadMedicines(model);

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        formPanel.add(btnUpdate);

        return panel;
    }



    private void loadMedicines(DefaultTableModel model) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM medicines";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            // Clear table before loading
            model.setRowCount(0);

            while (rs.next()) {

                Object[] row = {
                        rs.getInt("medicine_id"),
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("medicine_type"),
                        rs.getDouble("price"),
                        rs.getInt("quantity_in_stock")
                };

                model.addRow(row);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Suppliers Panel
    private JPanel createSuppliersPanel() {


            JPanel panel = new JPanel(new BorderLayout());


            JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));

            JTextField txtName = new JTextField();
            JTextField txtContact = new JTextField();
            JTextField txtPhone = new JTextField();
            JTextField txtEmail = new JTextField();
            JTextField txtAddress = new JTextField();

            JButton btnAdd = new JButton("Add Supplier");
            JButton btnUpdate = new JButton("Update Supplier");
            JButton btnDelete = new JButton("Delete Supplier");

            formPanel.add(new JLabel("Name:"));
            formPanel.add(txtName);

            formPanel.add(new JLabel("Contact Person:"));
            formPanel.add(txtContact);

            formPanel.add(new JLabel("Phone:"));
            formPanel.add(txtPhone);

            formPanel.add(new JLabel("Email:"));
            formPanel.add(txtEmail);

            formPanel.add(new JLabel("Address:"));
            formPanel.add(txtAddress);

            formPanel.add(btnAdd);
            formPanel.add(btnUpdate);
            formPanel.add(btnDelete);

            panel.add(formPanel, BorderLayout.NORTH);

            // supplier table
            String[] columns = {"ID", "Name", "Contact", "Phone", "Email", "Address"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            JTable table = new JTable(model);

            panel.add(new JScrollPane(table), BorderLayout.CENTER);


            loadSuppliers(model);

            // listens for clicked row and highlight
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.getSelectedRow();

                    txtName.setText(model.getValueAt(row, 1).toString());
                    txtContact.setText(model.getValueAt(row, 2).toString());
                    txtPhone.setText(model.getValueAt(row, 3).toString());
                    txtEmail.setText(model.getValueAt(row, 4).toString());
                    txtAddress.setText(model.getValueAt(row, 5).toString());
                }
            });

            // add supplier button
            btnAdd.addActionListener(e -> {
                try {
                    Connection conn = DBConnection.getConnection();

                    String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pst = conn.prepareStatement(sql);

                    pst.setString(1, txtName.getText());
                    pst.setString(2, txtContact.getText());
                    pst.setString(3, txtPhone.getText());
                    pst.setString(4, txtEmail.getText());
                    pst.setString(5, txtAddress.getText());

                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Supplier added!");

                    loadSuppliers(model);

                    conn.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // update supplier button
            btnUpdate.addActionListener(e -> {

                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(null, "Select a supplier!");
                    return;
                }

                try {
                    int id = (int) model.getValueAt(row, 0);

                    Connection conn = DBConnection.getConnection();

                    String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";
                    PreparedStatement pst = conn.prepareStatement(sql);

                    pst.setString(1, txtName.getText());
                    pst.setString(2, txtContact.getText());
                    pst.setString(3, txtPhone.getText());
                    pst.setString(4, txtEmail.getText());
                    pst.setString(5, txtAddress.getText());
                    pst.setInt(6, id);

                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Supplier updated!");

                    loadSuppliers(model);

                    conn.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // delete supplier button
            btnDelete.addActionListener(e -> {

                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(null, "Select a supplier!");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(null, "Delete this supplier?");
                if (confirm != JOptionPane.YES_OPTION) return;

                try {
                    int id = (int) model.getValueAt(row, 0);

                    Connection conn = DBConnection.getConnection();

                    String sql = "DELETE FROM suppliers WHERE supplier_id=?";
                    PreparedStatement pst = conn.prepareStatement(sql);

                    pst.setInt(1, id);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Supplier deleted!");

                    loadSuppliers(model);

                    conn.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            return panel;
    }


    private void loadSuppliers(DefaultTableModel model) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM suppliers";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                });
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // users panel
    private JPanel createUsersPanel() {

        JPanel panel = new JPanel(new BorderLayout());


        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));

        JTextField txtUsername = new JTextField();
        JTextField txtPassword = new JTextField();
        JTextField txtFullName = new JTextField();

        String[] roles = {"Admin", "Cashier"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);

        JButton btnAdd = new JButton("Add User");
        JButton btnDelete = new JButton("Delete User");

        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(txtFullName);

        formPanel.add(new JLabel("Role:"));
        formPanel.add(cmbRole);

        formPanel.add(btnAdd);
        formPanel.add(btnDelete);

        panel.add(formPanel, BorderLayout.NORTH);

        // users table
        String[] columns = {"ID", "Username", "Role", "Full Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);


        loadUsers(model);

        // add user button
        btnAdd.addActionListener(e -> {

            try {
                Connection conn = DBConnection.getConnection();

                String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, txtUsername.getText());
                pst.setString(2, txtPassword.getText());
                pst.setString(3, cmbRole.getSelectedItem().toString());
                pst.setString(4, txtFullName.getText());

                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "User created!");

                loadUsers(model);

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // delete user button
        btnDelete.addActionListener(e -> {

            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Select a user!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(null, "Delete this user?");
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                int id = (int) model.getValueAt(row, 0);

                Connection conn = DBConnection.getConnection();

                String sql = "DELETE FROM users WHERE user_id=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setInt(1, id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "User deleted!");

                loadUsers(model);

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return panel;
    }

    private void loadUsers(DefaultTableModel model) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM users";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("full_name")
                });
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

