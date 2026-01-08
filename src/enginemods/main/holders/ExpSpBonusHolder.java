package enginemods.main.holders;

import enginemods.main.enums.ExpSpType;

public class ExpSpBonusHolder {
    private final ExpSpType _type;

    private final double _bonus;

    public ExpSpBonusHolder(ExpSpType type, int bonus) {
        this._type = type;
        this._bonus = ((double) bonus / 100);
    }

    public ExpSpType getType() {
        return this._type;
    }

    public double getBonus() {
        return this._bonus;
    }
}
