import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class AccountLogin {
    private static final String FILE_NAME = "user_accounts.txt";
    private static Map<String, String> accounts = new HashMap<>();
    private static String loggedInUser = null;

    public static void main(String[] args) {
        loadAccountsFromFile();
        showLoginDialog();
    }

    private static void showLoginDialog() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int option = JOptionPane.showOptionDialog(null, panel, "Login or Register",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Login", "Register", "Exit"}, "Login");

        if (option == 0) { // Login
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (accounts.containsKey(user) && accounts.get(user).equals(pass)) {
                loggedInUser = user;
                launchGame();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
            }
        } else if (option == 1) { // Register
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
            } else if (accounts.containsKey(user)) {
                JOptionPane.showMessageDialog(null, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
            } else {
                accounts.put(user, pass);
                saveAccountsToFile();
                JOptionPane.showMessageDialog(null, "Registration successful. You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                showLoginDialog();
            }
        } else {
            System.exit(0); // Exit option
        }
    }

    private static void loadAccountsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    accounts.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            // File will be created on save
        }
    }

    private static void saveAccountsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, String> entry : accounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void launchGame() {
        SwingUtilities.invokeLater(() -> {
            SlotMachine game = new SlotMachine();
            game.setPlayerName(loggedInUser);  // Pass the logged-in user's name
            game.setVisible(true);  // Show the SlotMachine window
        });
    }
}
