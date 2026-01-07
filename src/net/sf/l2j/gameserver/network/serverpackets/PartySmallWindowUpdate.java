package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PartySmallWindowUpdate extends L2GameServerPacket {
    private final Player _member;

    public PartySmallWindowUpdate(Player member) {
        this._member = member;
    }

    protected final void writeImpl() {
        writeC(82);
        writeD(this._member.getObjectId());
        writeS(this._member.getName());
        writeD((int) this._member.getCurrentCp());
        writeD(this._member.getMaxCp());
        writeD((int) this._member.getCurrentHp());
        writeD(this._member.getMaxHp());
        writeD((int) this._member.getCurrentMp());
        writeD(this._member.getMaxMp());
        writeD(this._member.getLevel());
        writeD(this._member.getClassId().getId());
    }
}
