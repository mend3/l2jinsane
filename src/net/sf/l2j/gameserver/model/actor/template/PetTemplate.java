package net.sf.l2j.gameserver.model.actor.template;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.PetDataEntry;

import java.util.Map;

public final class PetTemplate extends NpcTemplate {
    public static final int MAX_LOAD = 54510;

    private final int _food1;

    private final int _food2;

    private final double _autoFeedLimit;

    private final double _hungryLimit;

    private final double _unsummonLimit;

    private final Map<Integer, PetDataEntry> _dataEntries;

    public PetTemplate(StatSet set) {
        super(set);
        this._food1 = set.getInteger("food1");
        this._food2 = set.getInteger("food2");
        this._autoFeedLimit = set.getDouble("autoFeedLimit");
        this._hungryLimit = set.getDouble("hungryLimit");
        this._unsummonLimit = set.getDouble("unsummonLimit");
        this._dataEntries = set.getMap("petData");
    }

    public int getFood1() {
        return this._food1;
    }

    public int getFood2() {
        return this._food2;
    }

    public double getAutoFeedLimit() {
        return this._autoFeedLimit;
    }

    public double getHungryLimit() {
        return this._hungryLimit;
    }

    public double getUnsummonLimit() {
        return this._unsummonLimit;
    }

    public PetDataEntry getPetDataEntry(int level) {
        return this._dataEntries.get(level);
    }

    public boolean canEatFood(int itemId) {
        return (this._food1 == itemId || this._food2 == itemId);
    }
}
