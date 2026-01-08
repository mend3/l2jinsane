package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q617_GatherTheFlames extends Quest {
    private static final String qn = "Q617_GatherTheFlames";

    private static final int HILDA = 31271;

    private static final int VULCAN = 31539;

    private static final int ROONEY = 32049;

    private static final int TORCH = 7264;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final int[] REWARDS = new int[]{6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

    public Q617_GatherTheFlames() {
        super(617, "Gather the Flames");
        CHANCES.put(21381, 510000);
        CHANCES.put(21653, 510000);
        CHANCES.put(21387, 530000);
        CHANCES.put(21655, 530000);
        CHANCES.put(21390, 560000);
        CHANCES.put(21656, 690000);
        CHANCES.put(21389, 550000);
        CHANCES.put(21388, 530000);
        CHANCES.put(21383, 510000);
        CHANCES.put(21392, 560000);
        CHANCES.put(21382, 600000);
        CHANCES.put(21654, 520000);
        CHANCES.put(21384, 640000);
        CHANCES.put(21394, 510000);
        CHANCES.put(21395, 560000);
        CHANCES.put(21385, 520000);
        CHANCES.put(21391, 550000);
        CHANCES.put(21393, 580000);
        CHANCES.put(21657, 570000);
        CHANCES.put(21386, 520000);
        CHANCES.put(21652, 490000);
        CHANCES.put(21378, 490000);
        CHANCES.put(21376, 480000);
        CHANCES.put(21377, 480000);
        CHANCES.put(21379, 590000);
        CHANCES.put(21380, 490000);
        setItemsIds(7264);
        addStartNpc(31539, 31271);
        addTalkId(31539, 31271, 32049);
        for (int mobs : CHANCES.keySet()) {
            addKillId(mobs);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q617_GatherTheFlames");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31539-03.htm") || event.equalsIgnoreCase("31271-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31539-05.htm")) {
            if (st.getQuestItemsCount(7264) >= 1000) {
                htmltext = "31539-07.htm";
                st.takeItems(7264, 1000);
                st.giveItems(Rnd.get(REWARDS), 1);
            }
        } else if (event.equalsIgnoreCase("31539-08.htm")) {
            st.takeItems(7264, -1);
            st.exitQuest(true);
        } else if (StringUtil.isDigit(event)) {
            if (st.getQuestItemsCount(7264) >= 1200) {
                htmltext = "32049-03.htm";
                st.takeItems(7264, 1200);
                st.giveItems(Integer.parseInt(event), 1);
            } else {
                htmltext = "32049-02.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q617_GatherTheFlames");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "" + npc.getNpcId() + npc.getNpcId();
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 31539 -> (st.getQuestItemsCount(7264) >= 1000) ? "31539-04.htm" : "31539-05.htm";
                    case 31271 -> "31271-04.htm";
                    case 32049 -> (st.getQuestItemsCount(7264) >= 1200) ? "32049-01.htm" : "32049-02.htm";
                    default -> htmltext;
                };
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(7264, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
