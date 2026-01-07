package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;

public final class PartySmallWindowAdd extends L2GameServerPacket {
    private final Player _member;

    private final int _leaderId;

    private final int _distribution;

    public PartySmallWindowAdd(Player member, Party party) {
        this._member = member;
        this._leaderId = party.getLeaderObjectId();
        this._distribution = party.getLootRule().ordinal();
    }

    protected void writeImpl() {
        writeC(79);
        writeD(this._leaderId);
        writeD(this._distribution);
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
        writeD(0);
        writeD(0);
    }
}
