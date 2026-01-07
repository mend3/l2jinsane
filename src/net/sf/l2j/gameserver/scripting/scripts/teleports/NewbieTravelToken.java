package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class NewbieTravelToken extends Quest {
    private static final Map<String, int[]> data = new HashMap<>();

    private static final int TOKEN = 8542;

    public NewbieTravelToken() {
        super(-1, "teleports");
        data.put("30600", new int[]{12160, 16554, -4583});
        data.put("30601", new int[]{115594, -177993, -912});
        data.put("30599", new int[]{45470, 48328, -3059});
        data.put("30602", new int[]{-45067, -113563, -199});
        data.put("30598", new int[]{-84053, 243343, -3729});
        addStartNpc(30598, 30599, 30600, 30601, 30602);
        addTalkId(30598, 30599, 30600, 30601, 30602);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        if (st == null)
            st = newQuestState(player);
        if (data.containsKey(event)) {
            int x = ((int[]) data.get(event))[0];
            int y = ((int[]) data.get(event))[1];
            int z = ((int[]) data.get(event))[2];
            if (st.getQuestItemsCount(8542) != 0) {
                st.takeItems(8542, 1);
                st.getPlayer().teleportTo(x, y, z, 0);
            } else {
                return "notoken.htm";
            }
        }
        st.exitQuest(true);
        return super.onAdvEvent(event, npc, player);
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = "";
        QuestState st = player.getQuestState(getName());
        int npcId = npc.getNpcId();
        if (player.getLevel() >= 20) {
            htmltext = "wronglevel.htm";
            st.exitQuest(true);
        } else {
            htmltext = npcId + ".htm";
        }
        return htmltext;
    }
}
