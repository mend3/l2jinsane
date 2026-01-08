/**/
package net.sf.l2j.loginserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FloodProtectedListener extends Thread {
    private static final CLogger LOGGER = new CLogger(FloodProtectedListener.class.getName());
    private final Map<String, FloodProtectedListener.ForeignConnection> _flooders = new ConcurrentHashMap<>();
    private final ServerSocket _serverSocket;

    public FloodProtectedListener(String listenIp, int port) throws IOException {
        if (listenIp.equals("*")) {
            this._serverSocket = new ServerSocket(port);
        } else {
            this._serverSocket = new ServerSocket(port, 50, InetAddress.getByName(listenIp));
        }

    }

    public abstract void addClient(Socket var1);

    public void run() {
        Socket connection = null;

        while (true) {
            while (true) {
                try {
                    connection = this._serverSocket.accept();
                    if (Config.FLOOD_PROTECTION) {
                        String address = connection.getInetAddress().getHostAddress();
                        long currentTime = System.currentTimeMillis();
                        FloodProtectedListener.ForeignConnection fc = this._flooders.get(address);
                        if (fc == null) {
                            this._flooders.put(address, new FloodProtectedListener.ForeignConnection(currentTime));
                        } else {
                            ++fc.attempts;
                            if (fc.attempts > Config.FAST_CONNECTION_LIMIT && currentTime - fc.lastConnection < (long) Config.NORMAL_CONNECTION_TIME || currentTime - fc.lastConnection < (long) Config.FAST_CONNECTION_TIME || fc.attempts > Config.MAX_CONNECTION_PER_IP) {
                                fc.lastConnection = currentTime;
                                --fc.attempts;
                                connection.close();
                                if (!fc.isFlooding) {
                                    LOGGER.info("Flood detected from {}.", address);
                                }

                                fc.isFlooding = true;
                                continue;
                            }

                            if (fc.isFlooding) {
                                fc.isFlooding = false;
                                LOGGER.info("{} isn't considered as flooding anymore.", address);
                            }

                            fc.lastConnection = currentTime;
                        }
                    }

                    this.addClient(connection);
                } catch (Exception var8) {
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Exception ignored) {
                    }

                    if (this.isInterrupted()) {
                        try {
                            this._serverSocket.close();
                        } catch (IOException var6) {
                            LOGGER.error(var6);
                        }

                        return;
                    }
                }
            }
        }
    }

    public void removeFloodProtection(String ip) {
        if (Config.FLOOD_PROTECTION) {
            FloodProtectedListener.ForeignConnection fc = this._flooders.get(ip);
            if (fc != null) {
                --fc.attempts;
                if (fc.attempts == 0) {
                    this._flooders.remove(ip);
                }
            }

        }
    }

    protected static class ForeignConnection {
        public int attempts;
        public long lastConnection;
        public boolean isFlooding = false;

        public ForeignConnection(long time) {
            this.lastConnection = time;
            this.attempts = 1;
        }
    }
}