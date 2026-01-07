package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PledgeShowMemberListAdd extends L2GameServerPacket {
    private final String _name;

    private final int _lvl;

    private final int _classId;

    private final int _isOnline;

    private final int _pledgeType;

    private final int _race;

    private final int _sex;

    public PledgeShowMemberListAdd(Player player) {
        this._name = player.getName();
        this._lvl = player.getLevel();
        this._classId = player.getClassId().getId();
        this._isOnline = player.isOnline() ? player.getObjectId() : 0;
        this._pledgeType = player.getPledgeType();
        this._race = player.getRace().ordinal();
        this._sex = player.getAppearance().getSex().ordinal();
    }

    protected final void writeImpl() {
        writeC(85);
        writeS(this._name);
        writeD(this._lvl);
        writeD(this._classId);
        writeD(this._sex);
        writeD(this._race);
        writeD(this._isOnline);
        writeD(this._pledgeType);
    }
}
