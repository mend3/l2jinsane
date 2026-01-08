package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class MotherTreeZone extends ZoneType {
    private int _enterMsg;

    private int _leaveMsg;

    private int _mpRegen = 1;

    private int _hpRegen = 1;

    private int _race = -1;

    public MotherTreeZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        switch (name) {
            case "enterMsgId" -> this._enterMsg = Integer.parseInt(value);
            case "leaveMsgId" -> this._leaveMsg = Integer.parseInt(value);
            case "MpRegenBonus" -> this._mpRegen = Integer.parseInt(value);
            case "HpRegenBonus" -> this._hpRegen = Integer.parseInt(value);
            case "affectedRace" -> this._race = Integer.parseInt(value);
            default -> super.setParameter(name, value);
        }
    }

    protected boolean isAffected(Creature character) {
        if (character instanceof Player)
            return (this._race == ((Player) character).getRace().ordinal());
        return true;
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.MOTHER_TREE, true);
            if (this._enterMsg != 0)
                character.sendPacket(SystemMessage.getSystemMessage(this._enterMsg));
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.MOTHER_TREE, false);
            if (this._leaveMsg != 0)
                character.sendPacket(SystemMessage.getSystemMessage(this._leaveMsg));
        }
    }

    public int getMpRegenBonus() {
        return this._mpRegen;
    }

    public int getHpRegenBonus() {
        return this._hpRegen;
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
