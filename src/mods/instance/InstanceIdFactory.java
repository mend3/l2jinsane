package mods.instance;

import java.util.concurrent.atomic.AtomicInteger;

public final class InstanceIdFactory {
    private static final AtomicInteger nextAvailable = new AtomicInteger(1);

    public static synchronized int getNextAvailable() {
        return nextAvailable.getAndIncrement();
    }
}
