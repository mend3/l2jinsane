/**/
package net.sf.l2j.util;

import net.sf.l2j.commons.mmocore.IAcceptFilter;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IPv4Filter implements IAcceptFilter, Runnable {
    private static final long SLEEP_TIME = 5000L;
    private final Map<Integer, IPv4Filter.FloodHolder> _floods = new ConcurrentHashMap<>();

    public IPv4Filter() {
        Thread t = new Thread(this);
        t.setName(this.getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
    }

    private static int hash(byte[] ip) {
        return ip[0] & 255 | ip[1] << 8 & '\uff00' | ip[2] << 16 & 16711680 | ip[3] << 24 & -16777216;
    }

    public boolean accept(SocketChannel sc) {
        int hash = hash(sc.socket().getInetAddress().getAddress());
        IPv4Filter.FloodHolder flood = this._floods.get(hash);
        if (flood != null) {
            long currentTime = System.currentTimeMillis();
            if (flood.tries == -1) {
                flood.lastAccess = currentTime;
                return false;
            }

            if (flood.lastAccess + 1000L > currentTime) {
                flood.lastAccess = currentTime;
                if (flood.tries >= 3) {
                    flood.tries = -1;
                    return false;
                }

                ++flood.tries;
            } else {
                flood.lastAccess = currentTime;
            }
        } else {
            this._floods.put(hash, new FloodHolder());
        }

        return true;
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        while (true) {
            long referenceTime = System.currentTimeMillis() - 300000L;
            this._floods.values().removeIf((f) -> f.lastAccess < referenceTime);

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException var4) {
                return;
            }
        }
    }

    protected static final class FloodHolder {
        protected long lastAccess = System.currentTimeMillis();
        protected int tries;

        protected FloodHolder() {
        }
    }
}