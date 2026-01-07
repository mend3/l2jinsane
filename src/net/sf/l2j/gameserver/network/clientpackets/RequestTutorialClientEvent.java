package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket {
    int eventId;

    protected void readImpl() {
        this.eventId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        QuestState qs = player.getQuestState("Tutorial");
        if (qs != null)
            qs.getQuest().notifyEvent("CE" + this.eventId, null, player);
    }
}
