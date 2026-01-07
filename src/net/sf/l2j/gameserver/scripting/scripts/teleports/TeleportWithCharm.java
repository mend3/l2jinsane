package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class TeleportWithCharm extends Quest {
    private static final int WHIRPY = 30540;

    private static final int TAMIL = 30576;

    private static final int ORC_GATEKEEPER_CHARM = 1658;

    private static final int DWARF_GATEKEEPER_TOKEN = 1659;

    public TeleportWithCharm() {
        super(-1, "teleports");
        addStartNpc(30540, 30576);
        addTalkId(30540, 30576);
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        String htmltext = "";
        int npcId = npc.getNpcId();
        if (npcId == 30540) {
            if (st.getQuestItemsCount(1659) >= 1) {
                st.takeItems(1659, 1);
                player.teleportTo(-80826, 149775, -3043, 0);
            } else {
                htmltext = "30540-01.htm";
            }
        } else if (npcId == 30576) {
            if (st.getQuestItemsCount(1658) >= 1) {
                st.takeItems(1658, 1);
                player.teleportTo(-80826, 149775, -3043, 0);
            } else {
                htmltext = "30576-01.htm";
            }
        }
        st.exitQuest(true);
        return htmltext;
    }
}
