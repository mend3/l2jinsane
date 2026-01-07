package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends L2GameClientPacket {
    private String _name;

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player requestor = getClient().getPlayer();
        if (requestor == null)
            return;
        Player target = World.getInstance().getPlayer(this._name);
        if (target == null) {
            requestor.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
            return;
        }
        if (requestor.equals(target)) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        Party requestorParty = requestor.getParty();
        Party targetParty = target.getParty();
        if (requestorParty == null || targetParty == null) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        CommandChannel requestorChannel = requestorParty.getCommandChannel();
        if (requestorChannel == null || !requestorChannel.isLeader(requestor)) {
            requestor.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (!requestorChannel.removeParty(targetParty)) {
            requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        targetParty.broadcastMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);
        if (requestorParty.isInCommandChannel())
            requestorParty.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL).addCharName(targetParty.getLeader()));
    }
}
