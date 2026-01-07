package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.ZoneType;

public class FishingZone extends ZoneType {
    public FishingZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
    }

    protected void onExit(Creature character) {
    }

    public int getWaterZ() {
        return getZone().getHighZ();
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
