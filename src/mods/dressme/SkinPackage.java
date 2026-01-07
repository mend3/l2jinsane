package mods.dressme;

import net.sf.l2j.commons.util.StatSet;

public class SkinPackage {
    private final String _type;

    private final String _name;

    private final int _id;

    private final int _weaponId;

    private final int _shieldId;

    private final int _chestId;

    private final int _hairId;

    private final int _faceId;

    private final int _legsId;

    private final int _glovesId;

    private final int _feetId;

    private final int _priceId;

    private final int _priceCount;

    public SkinPackage(StatSet set) {
        this._type = set.getString("type", "default");
        this._name = set.getString("name", "NoName");
        this._id = set.getInteger("id", 0);
        this._weaponId = set.getInteger("weaponId", 0);
        this._shieldId = set.getInteger("shieldId", 0);
        this._chestId = set.getInteger("chestId", 0);
        this._hairId = set.getInteger("hairId", 0);
        this._faceId = set.getInteger("faceId", 0);
        this._legsId = set.getInteger("legsId", 0);
        this._glovesId = set.getInteger("glovesId", 0);
        this._feetId = set.getInteger("feetId", 0);
        this._priceId = set.getInteger("priceId", 0);
        this._priceCount = set.getInteger("priceCount", 0);
    }

    public int getId() {
        return this._id;
    }

    public String getType() {
        return this._type;
    }

    public String getName() {
        return this._name;
    }

    public int getWeaponId() {
        return this._weaponId;
    }

    public int getShieldId() {
        return this._shieldId;
    }

    public int getChestId() {
        return this._chestId;
    }

    public int getHairId() {
        return this._hairId;
    }

    public int getFaceId() {
        return this._faceId;
    }

    public int getLegsId() {
        return this._legsId;
    }

    public int getGlovesId() {
        return this._glovesId;
    }

    public int getFeetId() {
        return this._feetId;
    }

    public int getPriceId() {
        return this._priceId;
    }

    public int getPriceCount() {
        return this._priceCount;
    }
}
