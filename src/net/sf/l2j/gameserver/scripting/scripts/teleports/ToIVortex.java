package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class ToIVortex extends Quest {
    private static final int GREEN_STONE = 4401;

    private static final int BLUE_STONE = 4402;

    private static final int RED_STONE = 4403;

    public ToIVortex() {
        super(-1, "teleports");
        addStartNpc(30952, 30953, 30954);
        addTalkId(30952, 30953, 30954);
        addFirstTalkId(30952, 30953, 30954);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        if (event.equalsIgnoreCase("blue")) {
            if (st.hasQuestItems(4402)) {
                st.takeItems(4402, 1);
                player.teleportTo(114097, 19935, 935, 0);
            } else {
                htmltext = "no-items.htm";
            }
        } else if (event.equalsIgnoreCase("green")) {
            if (st.hasQuestItems(4401)) {
                st.takeItems(4401, 1);
                player.teleportTo(110930, 15963, -4378, 0);
            } else {
                htmltext = "no-items.htm";
            }
        } else if (event.equalsIgnoreCase("red")) {
            if (st.hasQuestItems(4403)) {
                st.takeItems(4403, 1);
                player.teleportTo(118558, 16659, 5987, 0);
            } else {
                htmltext = "no-items.htm";
            }
        }
        st.exitQuest(true);
        return htmltext;
    }

    public String onFirstTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        if (st == null)
            st = newQuestState(player);
        return npc.getNpcId() + ".htm";
    }
}
