package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PartySmallWindowDelete extends L2GameServerPacket {
    private final Player _member;

    public PartySmallWindowDelete(Player member) {
        this._member = member;
    }

    protected final void writeImpl() {
        writeC(81);
        writeD(this._member.getObjectId());
        writeS(this._member.getName());
    }
}
