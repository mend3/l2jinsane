package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket {
    private final Party _party;

    public ExMPCCShowPartyMemberInfo(Party party) {
        this._party = party;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(74);
        writeD(this._party.getMembersCount());
        for (Player member : this._party.getMembers()) {
            writeS(member.getName());
            writeD(member.getObjectId());
            writeD(member.getClassId().getId());
        }
    }
}
