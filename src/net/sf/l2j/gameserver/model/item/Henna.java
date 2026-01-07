package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public final class Henna {
    public static final int DRAW_AMOUNT = 10;

    public static final int REMOVE_AMOUNT = 5;

    private final int _symbolId;

    private final int _dyeId;

    private final int _drawPrice;

    private final int _INT;

    private final int _STR;

    private final int _CON;

    private final int _MEN;

    private final int _DEX;

    private final int _WIT;

    private final int[] _classes;

    public Henna(StatSet set) {
        this._symbolId = set.getInteger("symbolId");
        this._dyeId = set.getInteger("dyeId");
        this._drawPrice = set.getInteger("price", 0);
        this._INT = set.getInteger("INT", 0);
        this._STR = set.getInteger("STR", 0);
        this._CON = set.getInteger("CON", 0);
        this._MEN = set.getInteger("MEN", 0);
        this._DEX = set.getInteger("DEX", 0);
        this._WIT = set.getInteger("WIT", 0);
        this._classes = set.getIntegerArray("classes");
    }

    public int getSymbolId() {
        return this._symbolId;
    }

    public int getDyeId() {
        return this._dyeId;
    }

    public int getDrawPrice() {
        return this._drawPrice;
    }

    public int getRemovePrice() {
        return this._drawPrice / 5;
    }

    public int getINT() {
        return this._INT;
    }

    public int getSTR() {
        return this._STR;
    }

    public int getCON() {
        return this._CON;
    }

    public int getMEN() {
        return this._MEN;
    }

    public int getDEX() {
        return this._DEX;
    }

    public int getWIT() {
        return this._WIT;
    }

    public boolean canBeUsedBy(Player player) {
        return ArraysUtil.contains(this._classes, player.getClassId().getId());
    }
}
