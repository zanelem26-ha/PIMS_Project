import javax.swing.*;
import java.awt.*;
import java.sql.*;

class PIMS_swing {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Pharmacy Inventory Management System");


        frame.setSize(410, 200);
        frame.setBackground(Color.blue);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        JLabel juserName = new JLabel("Username:");
        juserName.setBounds(30, 30, 90, 25);
        juserName.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(juserName);

        JTextField txtUsername = new JTextField();
        txtUsername.setBounds(130, 30, 130, 25);
        frame.add(txtUsername);

        JLabel passField = new JLabel("Password:");
        passField.setBounds(30, 70, 90, 25);
        passField.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(passField);

        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setBounds(130, 70, 130, 25);
        frame.add(txtPassword);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(150, 120, 100, 30);
        frame.add(btnLogin);



        btnLogin.addActionListener(e -> {

            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            try {
                Connection con = DBConnection.getConnection();

                String sql = "SELECT * FROM users WHERE username=? AND password=?";

                // safely execute SQL queries
                PreparedStatement pst = con.prepareStatement(sql);

                pst.setString(1, username);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {

                    String role = rs.getString("role");
                    String fullName = rs.getString("full_name");


                    if (role.equals("Admin")) {
                        new AdminDashboard(fullName);
                    } else if (role.equals("Cashier")) {
                        new CashierDashboard(fullName);
                    }

                } else {

                    JOptionPane.showMessageDialog(frame,
                            "Invalid username or password!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                con.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }
}