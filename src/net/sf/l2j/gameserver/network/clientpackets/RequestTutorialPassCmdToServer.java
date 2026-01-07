package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.QuestState;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket {
    String _bypass;

    protected void readImpl() {
        this._bypass = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        QuestState qs = player.getQuestState("Tutorial");
        if (qs != null)
            qs.getQuest().notifyEvent(this._bypass, null, player);
    }
}
