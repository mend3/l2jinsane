package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;

public final class PartySmallWindowAll extends L2GameServerPacket {
    private final Party _party;

    private final Player _exclude;

    private final int _dist;

    private final int _leaderObjectId;

    public PartySmallWindowAll(Player exclude, Party party) {
        this._exclude = exclude;
        this._party = party;
        this._leaderObjectId = this._party.getLeaderObjectId();
        this._dist = this._party.getLootRule().ordinal();
    }

    protected void writeImpl() {
        writeC(78);
        writeD(this._leaderObjectId);
        writeD(this._dist);
        writeD(this._party.getMembersCount() - 1);
        for (Player member : this._party.getMembers()) {
            if (member == this._exclude)
                continue;
            writeD(member.getObjectId());
            writeS(member.getName());
            writeD((int) member.getCurrentCp());
            writeD(member.getMaxCp());
            writeD((int) member.getCurrentHp());
            writeD(member.getMaxHp());
            writeD((int) member.getCurrentMp());
            writeD(member.getMaxMp());
            writeD(member.getLevel());
            writeD(member.getClassId().getId());
            writeD(0);
            writeD(member.getRace().ordinal());
        }
    }
}
