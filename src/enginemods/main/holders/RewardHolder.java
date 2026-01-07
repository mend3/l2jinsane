package enginemods.main.holders;

public class RewardHolder {
    private int _id;

    private int _count;

    private int _chance;

    private int _min;

    private int _max;

    public RewardHolder(int rewardId, int rewardCount) {
        this._id = rewardId;
        this._count = rewardCount;
        this._chance = 100;
        this._min = 1;
        this._max = 1;
    }

    public RewardHolder(int rewardId, int rewardCount, int rewardChance) {
        this._id = rewardId;
        this._count = rewardCount;
        this._chance = rewardChance;
        this._min = 1;
        this._max = 1;
    }

    public RewardHolder(int rewardId, int min, int max, int rewardChance) {
        this._id = rewardId;
        this._min = min;
        this._max = max;
        this._chance = rewardChance;
    }

    public int getRewardId() {
        return this._id;
    }

    public int getRewardCount() {
        return this._count;
    }

    public int getRewardChance() {
        return this._chance;
    }

    public int getMin() {
        return this._min;
    }

    public void setMin(int min) {
        this._min = min;
    }

    public int getMax() {
        return this._max;
    }

    public void setMax(int max) {
        this._max = max;
    }

    public void setId(int id) {
        this._id = id;
    }

    public void setCount(int count) {
        this._count = count;
    }

    public void setChance(int chance) {
        this._chance = chance;
    }
}
