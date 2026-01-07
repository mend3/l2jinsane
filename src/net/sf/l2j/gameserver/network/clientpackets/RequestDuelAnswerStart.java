package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends L2GameClientPacket {
    private boolean _isPartyDuel;

    private boolean _duelAccepted;

    protected void readImpl() {
        this._isPartyDuel = (readD() == 1);
        readD();
        this._duelAccepted = (readD() == 1);
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player requestor = activeChar.getActiveRequester();
        if (requestor == null)
            return;
        activeChar.setActiveRequester(null);
        requestor.onTransactionResponse();
        if (this._duelAccepted) {
            if (!requestor.canDuel()) {
                activeChar.sendPacket(requestor.getNoDuelReason());
                return;
            }
            if (!activeChar.canDuel()) {
                activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
                return;
            }
            if (!requestor.isInsideRadius(activeChar, 2000, false, false)) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addCharName(requestor));
                return;
            }
            if (this._isPartyDuel) {
                Party requestorParty = requestor.getParty();
                if (requestorParty == null || !requestorParty.isLeader(requestor) || requestorParty.containsPlayer(activeChar)) {
                    activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
                    return;
                }
                Party activeCharParty = activeChar.getParty();
                if (activeCharParty == null) {
                    activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
                    return;
                }
                for (Player member : requestorParty.getMembers()) {
                    if (member != requestor && !member.canDuel()) {
                        activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
                        return;
                    }
                }
                for (Player member : activeCharParty.getMembers()) {
                    if (member != activeChar && !member.canDuel()) {
                        activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
                        return;
                    }
                }
                CommandChannel requestorChannel = requestorParty.getCommandChannel();
                if (requestorChannel != null)
                    requestorChannel.removeParty(requestorParty);
                CommandChannel activeCharChannel = activeCharParty.getCommandChannel();
                if (activeCharChannel != null)
                    activeCharChannel.removeParty(activeCharParty);
                for (Player member : requestorParty.getMembers())
                    member.removeMeFromPartyMatch();
                for (Player member : activeCharParty.getMembers())
                    member.removeMeFromPartyMatch();
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(requestor));
                requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(activeChar));
            } else {
                activeChar.removeMeFromPartyMatch();
                requestor.removeMeFromPartyMatch();
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(requestor));
                requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(activeChar));
            }
            DuelManager.getInstance().addDuel(requestor, activeChar, this._isPartyDuel);
        } else if (this._isPartyDuel) {
            requestor.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
        } else {
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL).addCharName(activeChar));
        }
    }
}
