import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NetworkScannerGUI app = new NetworkScannerGUI();
            app.setVisible(true);
        });
    }
}