package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q366_SilverHairedShaman extends Quest {
    private static final String qn = "Q366_SilverHairedShaman";

    private static final int DIETER = 30111;

    private static final int HAIR = 5874;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q366_SilverHairedShaman() {
        super(366, "Silver Haired Shaman");
        CHANCES.put(Integer.valueOf(20986), Integer.valueOf(560000));
        CHANCES.put(Integer.valueOf(20987), Integer.valueOf(660000));
        CHANCES.put(Integer.valueOf(20988), Integer.valueOf(620000));
        setItemsIds(5874);
        addStartNpc(30111);
        addTalkId(30111);
        addKillId(20986, 20987, 20988);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q366_SilverHairedShaman");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30111-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30111-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q366_SilverHairedShaman");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 48) ? "30111-0.htm" : "30111-1.htm";
                break;
            case 1:
                count = st.getQuestItemsCount(5874);
                if (count == 0) {
                    htmltext = "30111-3.htm";
                    break;
                }
                htmltext = "30111-4.htm";
                st.takeItems(5874, -1);
                st.rewardItems(57, 12070 + 500 * count);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5874, 1, 0, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
