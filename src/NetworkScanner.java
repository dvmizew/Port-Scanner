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

    public NetworkScanner(String target, int startPort, int endPort, int timeout) {
        this.target = target;
        this.startPort = startPort;
        this.endPort = endPort;
        this.timeout = timeout;
    }

    public boolean checkTargetExists() {
        try {
            InetAddress address = InetAddress.getByName(target);
            return address.isReachable(timeout);
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
    }

    public void cancel() {
        this.cancelled = true;
    }

    private String identifyService(int port) {
        return switch (port) {
            case 20, 21 -> "FTP";
            case 22 -> "SSH";
            case 23 -> "Telnet";
            case 25 -> "SMTP";
            case 53 -> "DNS";
            case 67, 68 -> "DHCP";
            case 69 -> "TFTP";
            case 80 -> "HTTP";
            case 110 -> "POP3";
            case 119 -> "NNTP";
            case 123 -> "NTP";
            case 137, 138, 139 -> "NetBIOS";
            case 143 -> "IMAP";
            case 161, 162 -> "SNMP";
            case 179 -> "BGP";
            case 194 -> "IRC";
            case 389 -> "LDAP";
            case 443 -> "HTTPS";
            case 445 -> "SMB";
            case 465 -> "SMTPS";
            case 514 -> "Syslog";
            case 515 -> "LPD";
            case 520 -> "RIP";
            case 587 -> "SMTP Submission";
            case 631 -> "IPP";
            case 993 -> "IMAPS";
            case 995 -> "POP3S";
            case 1080 -> "SOCKS";
            case 1194 -> "OpenVPN";
            case 1433 -> "MSSQL";
            case 1434 -> "MSSQL Monitor";
            case 1521 -> "Oracle";
            case 1723 -> "PPTP";
            case 1900 -> "SSDP, UPnP";
            case 2049 -> "NFS";
            case 2082, 2083 -> "cPanel";
            case 3128 -> "Squid";
            case 3260 -> "iSCSI";
            case 3306 -> "MySQL";
            case 3389 -> "RDP";
            case 3690 -> "Subversion";
            case 4369 -> "Erlang Port Mapper";
            case 5432 -> "PostgreSQL";
            case 5900 -> "VNC";
            case 5984 -> "CouchDB";
            case 6379 -> "Redis";
            case 6667 -> "IRC";
            case 8000, 8001, 8002 -> "Web Servers";
            case 8080 -> "HTTP Proxy";
            case 8086 -> "InfluxDB";
            case 8443 -> "HTTPS Alt";
            case 8888 -> "HTTP Alt";
            case 9200 -> "Elasticsearch";
            case 11211 -> "Memcached";
            case 27017 -> "MongoDB";
            case 32400 -> "Plex Media Server";
            case 37777 -> "Dahua DVR";
            case 44818 -> "EtherNet/IP";
            case 47808 -> "BACnet";
            case 50000 -> "Synology DSM";
            case 50070 -> "Hadoop NameNode";
            case 60000 -> "BitTorrent";
            default -> "Unknown";
        };
    }
}
