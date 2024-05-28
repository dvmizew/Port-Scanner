import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class NetworkScanner {
    private final String target;
    private final int startPort;
    private final int endPort;
    private final int timeout;
    private volatile boolean cancelled = false;

    public NetworkScanner(String target, int startPort, int endPort, int timeout) {
        this.target = target;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeout = timeout;
    }

    public void scanNetwork(Consumer<String> resultCallback, Consumer<Integer> progressCallback) {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        int totalTasks = (endPort - startPort + 1);
        final int[] completedTasks = {0};

        for (int port = startPort; port <= endPort; port++) {
            if (cancelled) break;
            int currentPort = port;
            executor.submit(() -> {
                if (cancelled) return;
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(target, currentPort), timeout);
                    String service = identifyService(currentPort);
                    resultCallback.accept("Open port: " + target + ":" + currentPort + " (" + service + ")");
                } catch (IOException ignored) {
                } finally {
                    synchronized (completedTasks) {
                        completedTasks[0]++;
                        int progress = (int) ((completedTasks[0] / (double) totalTasks) * 100);
                        progressCallback.accept(progress);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public void cancel() {
        this.cancelled = true;
    }

    private String identifyService(int port) {
        return switch (port) {
            case 21 -> "FTP";
            case 22 -> "SSH";
            case 23 -> "Telnet";
            case 25 -> "SMTP";
            case 53 -> "DNS";
            case 80 -> "HTTP";
            case 110 -> "POP3";
            case 143 -> "IMAP";
            case 443 -> "HTTPS";
            case 3389 -> "RDP";
            default -> "Unknown";
        };
    }
}
