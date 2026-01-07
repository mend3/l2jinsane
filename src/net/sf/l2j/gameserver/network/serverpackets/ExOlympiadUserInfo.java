package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class ExOlympiadUserInfo extends L2GameServerPacket {
    private final int _side;

    private final int _objectId;

    private final String _name;

    private final int _classId;

    private final int _curHp;

    private final int _maxHp;

    private final int _curCp;

    private final int _maxCp;

    public ExOlympiadUserInfo(Player player) {
        this._side = player.getOlympiadSide();
        this._objectId = player.getObjectId();
        this._name = player.getName();
        this._classId = player.getClassId().getId();
        this._curHp = (int) player.getCurrentHp();
        this._maxHp = player.getMaxHp();
        this._curCp = (int) player.getCurrentCp();
        this._maxCp = player.getMaxCp();
    }

    protected final void writeImpl() {
        writeC(254);
        writeH(41);
        writeC(this._side);
        writeD(this._objectId);
        writeS(this._name);
        writeD(this._classId);
        writeD(this._curHp);
        writeD(this._maxHp);
        writeD(this._curCp);
        writeD(this._maxCp);
    }
}
