package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.buylist.Product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BuyListTaskManager implements Runnable {
    private final Map<Product, Long> _products = new ConcurrentHashMap<>();

    private BuyListTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static BuyListTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._products.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<Product, Long> entry : this._products.entrySet()) {
            if (time < entry.getValue())
                continue;
            Product product = entry.getKey();
            product.setCount(product.getMaxCount());
            product.delete();
            this._products.remove(product);
        }
    }

    public void add(Product product, long interval) {
        long newRestockTime = System.currentTimeMillis() + interval;
        if (this._products.putIfAbsent(product, Long.valueOf(newRestockTime)) == null)
            product.save(newRestockTime);
    }

    public void test(Product product, int currentCount, long nextRestockTime) {
        if (nextRestockTime - System.currentTimeMillis() > 0L) {
            product.setCount(currentCount);
            this._products.putIfAbsent(product, Long.valueOf(nextRestockTime));
        } else {
            product.setCount(product.getMaxCount());
            product.delete();
        }
    }

    private static final class SingletonHolder {
        private static final BuyListTaskManager INSTANCE = new BuyListTaskManager();
    }
}
