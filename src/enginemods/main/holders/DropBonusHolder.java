package enginemods.main.holders;

public class DropBonusHolder {
    private double _amountBonus = 1.0D;

    private double _chanceBonus = 1.0D;

    public void increaseAmountBonus(double amount) {
        this._amountBonus += amount - 1.0D;
    }

    public void increaseChanceBonus(double chance) {
        this._chanceBonus += chance - 1.0D;
    }

    public double getAmountBonus() {
        return this._amountBonus;
    }

    public double getChanceBonus() {
        return this._chanceBonus;
    }
}
