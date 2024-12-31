package main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // MASUKKAN USERNAME DARI PLAYER
        String username = JOptionPane.showInputDialog("Enter your username: ");

        // CEK DATA DARI PLAYER JIKA BERMAIN LAGI
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new AnimeRNG(username).setVisible(true));
    }
}
