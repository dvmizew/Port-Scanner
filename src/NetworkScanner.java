import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
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
    private final ConcurrentHashMap<Integer, String> portMap = new ConcurrentHashMap<>();

    public NetworkScanner(String target, int startPort, int endPort, int timeout) {
        this.target = target;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeout = timeout;
        loadPortMappings();
    }

    private void loadPortMappings() {
        try {
            File file = new File("res/ports.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList portList = doc.getElementsByTagName("port");
            for (int i = 0; i < portList.getLength(); i++) {
                Element portElement = (Element) portList.item(i);
                int portNumber = Integer.parseInt(portElement.getAttribute("number"));
                String service = portElement.getAttribute("service");
                portMap.put(portNumber, service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkTargetExists() {
        try {
            return InetAddress.getByName(target).isReachable(timeout);
        } catch (IOException e) {
            return false;
        }
    }

    public void scanNetwork(Consumer<String> resultCallback, Consumer<Integer> progressCallback) {
        if (!checkTargetExists()) {
            resultCallback.accept("Target " + target + " does not exist or is unreachable.");
            return;
        }

        try (ExecutorService executor = Executors.newFixedThreadPool(50)) {
            int totalTasks = (endPort - startPort + 1);
            final int[] completedTasks = {0};

            for (int port = startPort; port <= endPort; port++) {
                if (cancelled) break;
                int currentPort = port;
                executor.submit(() -> {
                    if (cancelled) return;
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(target, currentPort), timeout);
                        String service = portMap.getOrDefault(currentPort, "Unknown");
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
    }

    public void cancel() {
        this.cancelled = true;
    }
}
