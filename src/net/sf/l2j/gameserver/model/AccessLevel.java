package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.AdminData;

public class AccessLevel {
    private final int _accessLevel;

    private final String _name;

    private final int _childLevel;
    private final int _nameColor;
    private final int _titleColor;
    private final boolean _isGm;
    private final boolean _allowPeaceAttack;
    private final boolean _allowFixedRes;
    private final boolean _allowTransaction;
    private final boolean _allowAltG;
    private final boolean _giveDamage;
    private final boolean _takeAggro;
    private final boolean _gainExp;
    private AccessLevel _childAccess;

    public AccessLevel(StatSet set) {
        this._accessLevel = set.getInteger("level");
        this._name = set.getString("name");
        this._nameColor = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"));
        this._titleColor = Integer.decode("0x" + set.getString("titleColor", "FFFF77"));
        this._childLevel = set.getInteger("childLevel", 0);
        this._isGm = set.getBool("isGM", false);
        this._allowPeaceAttack = set.getBool("allowPeaceAttack", false);
        this._allowFixedRes = set.getBool("allowFixedRes", false);
        this._allowTransaction = set.getBool("allowTransaction", true);
        this._allowAltG = set.getBool("allowAltg", false);
        this._giveDamage = set.getBool("giveDamage", true);
        this._takeAggro = set.getBool("takeAggro", true);
        this._gainExp = set.getBool("gainExp", true);
    }

    public int getLevel() {
        return this._accessLevel;
    }

    public String getName() {
        return this._name;
    }

    public int getNameColor() {
        return this._nameColor;
    }

    public int getTitleColor() {
        return this._titleColor;
    }

    public boolean isGm() {
        return this._isGm;
    }

    public boolean allowPeaceAttack() {
        return this._allowPeaceAttack;
    }

    public boolean allowFixedRes() {
        return this._allowFixedRes;
    }

    public boolean allowTransaction() {
        return this._allowTransaction;
    }

    public boolean allowAltG() {
        return this._allowAltG;
    }

    public boolean canGiveDamage() {
        return this._giveDamage;
    }

    public boolean canTakeAggro() {
        return this._takeAggro;
    }

    public boolean canGainExp() {
        return this._gainExp;
    }

    public boolean hasChildAccess(AccessLevel access) {
        if (this._childAccess == null && this._childLevel > 0)
            this._childAccess = AdminData.getInstance().getAccessLevel(this._childLevel);
        return (this._childAccess != null && (this._childAccess.getLevel() == access.getLevel() || this._childAccess.hasChildAccess(access)));
    }
}
