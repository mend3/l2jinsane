package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;

public class ChangeMoveType extends L2GameServerPacket {
    private final int _objectId;

    private final int _running;

    private final int _swimming;

    public ChangeMoveType(Creature creature) {
        this._objectId = creature.getObjectId();
        this._running = creature.isRunning() ? 1 : 0;
        this._swimming = creature.isInsideZone(ZoneId.WATER) ? 1 : 0;
    }

    protected final void writeImpl() {
        writeC(46);
        writeD(this._objectId);
        writeD(this._running);
        writeD(this._swimming);
    }
}
