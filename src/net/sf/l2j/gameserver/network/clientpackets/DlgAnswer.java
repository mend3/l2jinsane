package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class DlgAnswer extends L2GameClientPacket {
    private int _messageId;

    private int _answer;

    private int _requesterId;

    protected void readImpl() {
        this._messageId = readD();
        this._answer = readD();
        this._requesterId = readD();
    }

    public void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (this._messageId == SystemMessageId.RESSURECTION_REQUEST_BY_S1.getId() || this._messageId == SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED.getId()) {
            activeChar.reviveAnswer(this._answer);
        } else if (this._messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId()) {
            if (Announcements.isSummoning && this._answer == 1)
                activeChar.teleportTo(ArenaTask.loc1x(), ArenaTask.loc1y(), ArenaTask.loc1z(), 125);
            if (this._answer == 1) {
                for (Player allsplayer : World.getInstance().getPlayers())
                    activeChar.teleportTo(allsplayer.getX(), allsplayer.getY(), allsplayer.getZ(), 100);
            } else {
                activeChar.teleportAnswer(this._answer, this._requesterId);
            }
        } else if (this._messageId == 1983 && Config.ALLOW_WEDDING) {
            activeChar.engageAnswer(this._answer);
        } else if (this._messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId()) {
            activeChar.activateGate(this._answer, 1);
        } else if (this._messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId()) {
            activeChar.activateGate(this._answer, 0);
        }
    }
}
