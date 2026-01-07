package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ClassMaster;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.feature.TutorialQuest;

public class RequestTutorialLinkHtml extends L2GameClientPacket {
    String _bypass;

    protected void readImpl() {
        this._bypass = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        ClassMaster.onTutorialLink(player, this._bypass);
        QuestState qs = player.getQuestState("Tutorial");
        if (qs != null)
            qs.getQuest().notifyEvent(this._bypass, null, player);
        if (this._bypass != null) {
            switch (this._bypass) {
                case "move":
                    player.teleportTo(Config.TUTORIAL_QUEST_FARM_LOCATION, 20);
                    break;
                case "town":
                    player.teleportTo(Config.TUTORIAL_QUEST_TOWN_LOCATION, 20);
                    break;
                case "close":
                    player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
                    break;
                case "start":
                    TutorialQuest.onStartTutorial(player);
                    break;
                case "exit":
                    TutorialQuest.onExitTutorial(player);
                    break;
                default:
                    return;
            }
            player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
        }
    }
}
