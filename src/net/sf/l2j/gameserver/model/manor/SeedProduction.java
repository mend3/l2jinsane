package net.sf.l2j.gameserver.model.manor;

import java.util.concurrent.atomic.AtomicInteger;

public class SeedProduction {
    private final int _seedId;

    private final int _price;

    private final int _startAmount;

    private final AtomicInteger _amount;

    public SeedProduction(int id, int amount, int price, int startAmount) {
        this._seedId = id;
        this._amount = new AtomicInteger(amount);
        this._price = price;
        this._startAmount = startAmount;
    }

    public final int getId() {
        return this._seedId;
    }

    public final int getAmount() {
        return this._amount.get();
    }

    public final void setAmount(int amount) {
        this._amount.set(amount);
    }

    public final int getPrice() {
        return this._price;
    }

    public final int getStartAmount() {
        return this._startAmount;
    }

    public final boolean decreaseAmount(int val) {
        int current;
        int next;
        do {
            current = this._amount.get();
            next = current - val;
            if (next < 0)
                return false;
        } while (!this._amount.compareAndSet(current, next));
        return true;
    }
}
