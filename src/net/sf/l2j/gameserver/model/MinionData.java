package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.random.Rnd;

public class MinionData {
    private int _minionId;
    private int _minionAmount;
    private int _minionAmountMin;
    private int _minionAmountMax;

    public int getMinionId() {
        return this._minionId;
    }

    public void setMinionId(int id) {
        this._minionId = id;
    }

    public void setAmountMin(int amountMin) {
        this._minionAmountMin = amountMin;
    }

    public void setAmountMax(int amountMax) {
        this._minionAmountMax = amountMax;
    }

    public int getAmount() {
        if (this._minionAmountMax > this._minionAmountMin) {
            this._minionAmount = Rnd.get(this._minionAmountMin, this._minionAmountMax);
            return this._minionAmount;
        } else {
            return this._minionAmountMin;
        }
    }

    public void setAmount(int amount) {
        this._minionAmount = amount;
    }
}
