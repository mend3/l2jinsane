package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinAlly;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinAlly extends L2GameClientPacket {
    private int _id;

    protected void readImpl() {
        this._id = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
            return;
        }
        Player target = World.getInstance().getPlayer(this._id);
        if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            return;
        }
        if (target.isInsideZone(ZoneId.AUTOFARMZONE)) {
            activeChar.sendMessage("Your target is inside no party farm zone.");
            return;
        }
        if (!Clan.checkAllyJoinCondition(activeChar, target))
            return;
        if (!activeChar.getRequest().setRequest(target, this))
            return;
        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE).addString(clan.getAllyName()).addCharName(activeChar));
        target.sendPacket(new AskJoinAlly(activeChar.getObjectId(), clan.getAllyName()));
    }
}
