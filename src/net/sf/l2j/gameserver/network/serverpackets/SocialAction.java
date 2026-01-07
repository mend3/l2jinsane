package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class SocialAction extends L2GameServerPacket {
    private final int _charObjId;

    private final int _actionId;

    public SocialAction(Creature cha, int actionId) {
        this._charObjId = cha.getObjectId();
        this._actionId = actionId;
    }

    protected final void writeImpl() {
        writeC(45);
        writeD(this._charObjId);
        writeD(this._actionId);
    }
}
