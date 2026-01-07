package net.sf.l2j.gameserver.model.manor;

public final class CropProcure extends SeedProduction {
    private final int _rewardType;

    public CropProcure(int id, int amount, int type, int startAmount, int price) {
        super(id, amount, price, startAmount);
        this._rewardType = type;
    }

    public int getReward() {
        return this._rewardType;
    }
}
