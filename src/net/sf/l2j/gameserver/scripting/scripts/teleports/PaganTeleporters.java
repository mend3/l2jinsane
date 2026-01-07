package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class PaganTeleporters extends Quest {
    private static final int VISITOR_MARK = 8064;

    private static final int PAGAN_MARK = 8067;

    public PaganTeleporters() {
        super(-1, "teleports");
        addStartNpc(32034, 32035, 32036, 32037, 32039, 32040);
        addTalkId(32034, 32035, 32036, 32037, 32039, 32040);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("Close_Door1")) {
            DoorData.getInstance().getDoor(19160001).closeMe();
        } else if (event.equalsIgnoreCase("Close_Door2")) {
            DoorData.getInstance().getDoor(19160010).closeMe();
            DoorData.getInstance().getDoor(19160011).closeMe();
        }
        return null;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        if (st == null)
            return htmltext;
        switch (npc.getNpcId()) {
            case 32034:
                if (st.hasQuestItems(8064) || st.hasQuestItems(8067)) {
                    DoorData.getInstance().getDoor(19160001).openMe();
                    startQuestTimer("Close_Door1", 10000L, npc, player, false);
                    htmltext = "FadedMark.htm";
                    break;
                }
                htmltext = "32034-1.htm";
                st.exitQuest(true);
                break;
            case 32035:
                DoorData.getInstance().getDoor(19160001).openMe();
                startQuestTimer("Close_Door1", 10000L, npc, player, false);
                htmltext = "FadedMark.htm";
                break;
            case 32036:
                if (!st.hasQuestItems(8067)) {
                    htmltext = "32036-1.htm";
                    break;
                }
                DoorData.getInstance().getDoor(19160010).openMe();
                DoorData.getInstance().getDoor(19160011).openMe();
                startQuestTimer("Close_Door2", 10000L, npc, player, false);
                htmltext = "32036-2.htm";
                break;
            case 32037:
                DoorData.getInstance().getDoor(19160010).openMe();
                DoorData.getInstance().getDoor(19160011).openMe();
                startQuestTimer("Close_Door2", 10000L, npc, player, false);
                htmltext = "FadedMark.htm";
                break;
            case 32039:
                player.teleportTo(-12766, -35840, -10856, 0);
                break;
            case 32040:
                player.teleportTo(34962, -49758, -763, 0);
                break;
        }
        return htmltext;
    }
}
