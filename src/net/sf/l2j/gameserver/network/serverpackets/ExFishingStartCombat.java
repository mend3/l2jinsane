package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class ExFishingStartCombat extends L2GameServerPacket {
    private final Creature _activeChar;

    private final int _time;

    private final int _hp;

    private final int _lureType;

    private final int _deceptiveMode;

    private final int _mode;

    public ExFishingStartCombat(Creature character, int time, int hp, int mode, int lureType, int deceptiveMode) {
        this._activeChar = character;
        this._time = time;
        this._hp = hp;
        this._mode = mode;
        this._lureType = lureType;
        this._deceptiveMode = deceptiveMode;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(21);
        writeD(this._activeChar.getObjectId());
        writeD(this._time);
        writeD(this._hp);
        writeC(this._mode);
        writeC(this._lureType);
        writeC(this._deceptiveMode);
    }
}
