package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;

public final class RequestOustPartyMember extends L2GameClientPacket {
    private String _name;

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Party party = player.getParty();
        if (party == null || !party.isLeader(player))
            return;
        party.removePartyMember(this._name, MessageType.EXPELLED);
    }
}
