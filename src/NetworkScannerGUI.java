import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class NetworkScannerGUI extends JFrame {
    private final JTextArea resultTextArea;
    private final JButton scanButton;
    private final JButton cancelButton;
    private final JTextField targetField;
    private final JTextField startPortField;
    private final JTextField endPortField;
    private final JProgressBar progressBar;
    private final DefaultListModel<String> scanResults;
    private SwingWorker<Void, String> currentWorker;

    public NetworkScannerGUI() {
        setTitle("Network Scanner");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultTextArea = new JTextArea(20, 70);
        resultTextArea.setEditable(false);

        scanButton = new JButton("Scan Network");
        cancelButton = new JButton("Cancel");
        JButton reportButton = new JButton("Generate Report");

        targetField = new JTextField("192.168.1.1", 20);
        startPortField = new JTextField("1", 5);
        endPortField = new JTextField("65535", 5);
        progressBar = new JProgressBar(0, 100);
        scanResults = new DefaultListModel<>();

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Target (IP/Hostname):"));
        inputPanel.add(targetField);
        inputPanel.add(new JLabel("Start Port:"));
        inputPanel.add(startPortField);
        inputPanel.add(new JLabel("End Port:"));
        inputPanel.add(endPortField);
        inputPanel.add(scanButton);
        inputPanel.add(cancelButton);
        inputPanel.add(reportButton);

        scanButton.addActionListener(new ScanButtonListener());
        cancelButton.addActionListener(new CancelButtonListener());
        reportButton.addActionListener(new ReportButtonListener());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        add(mainPanel);

        cancelButton.setEnabled(false);
    }

    private class ScanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Scan button pressed");
            String target = targetField.getText();
            int startPort;
            int endPort;
            try {
                startPort = Integer.parseInt(startPortField.getText());
                endPort = Integer.parseInt(endPortField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(NetworkScannerGUI.this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            resultTextArea.setText("Scanning in progress...\n");
            scanButton.setEnabled(false);
            cancelButton.setEnabled(true);
            progressBar.setValue(0);
            scanResults.clear();

            currentWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    NetworkScanner scanner = new NetworkScanner(target, startPort, endPort, 100);
                    scanner.scanNetwork(this::publish, this::setProgress);
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String result : chunks) {
                        resultTextArea.append(result + "\n");
                        scanResults.addElement(result);
                    }
                }

                @Override
                protected void done() {
                    scanButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    progressBar.setValue(100);
                    System.out.println("Scan complete");
                }
            };

            currentWorker.addPropertyChangeListener(evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });

            currentWorker.execute();
        }
    }

    private class CancelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Cancel button pressed");
            if (currentWorker != null) {
                currentWorker.cancel(true);
                scanButton.setEnabled(true);
                cancelButton.setEnabled(false);
                resultTextArea.append("Scan cancelled\n");
            }
        }
    }

    private class ReportButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Generate Report button clicked");
            generateReport();
        }
    }

    private void generateReport() {
        System.out.println("Generating report");
        StringBuilder report = new StringBuilder();
        report.append("Scan Report\n");
        report.append("===================\n\n");
        for (int i = 0; i < scanResults.getSize(); i++) {
            report.append(scanResults.getElementAt(i)).append("\n");
        }

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(NetworkScannerGUI.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fileChooser.getSelectedFile().toPath(), report.toString().getBytes());
                JOptionPane.showMessageDialog(NetworkScannerGUI.this, "Report saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(NetworkScannerGUI.this, "Error saving report", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NetworkScannerGUI app = new NetworkScannerGUI();
            app.setVisible(true);
        });
    }
}
