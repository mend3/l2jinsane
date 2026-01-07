package enginemods.main.holders;

public class DonationShopHolder {
    String _name;

    boolean _allowMod;

    int _priceId;

    int _priceCount;

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public boolean isAllowMod() {
        return this._allowMod;
    }

    public void setAllowMod(boolean allowMod) {
        this._allowMod = allowMod;
    }

    public int getPriceId() {
        return this._priceId;
    }

    public void setPriceId(int priceId) {
        this._priceId = priceId;
    }

    public int getPriceCount() {
        return this._priceCount;
    }

    public void setPriceCount(int priceCount) {
        this._priceCount = priceCount;
    }
}
