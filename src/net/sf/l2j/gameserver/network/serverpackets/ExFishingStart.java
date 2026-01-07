package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.location.Location;

public class ExFishingStart extends L2GameServerPacket {
    private final Creature _activeChar;

    private final Location _loc;

    private final int _fishType;

    private final boolean _isNightLure;

    public ExFishingStart(Creature character, int fishType, Location loc, boolean isNightLure) {
        this._activeChar = character;
        this._fishType = fishType;
        this._loc = loc;
        this._isNightLure = isNightLure;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(19);
        writeD(this._activeChar.getObjectId());
        writeD(this._fishType);
        writeD(this._loc.getX());
        writeD(this._loc.getY());
        writeD(this._loc.getZ());
        writeC(this._isNightLure ? 1 : 0);
        writeC(Config.ALT_FISH_CHAMPIONSHIP_ENABLED ? 1 : 0);
    }
}
