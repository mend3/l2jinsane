package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
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
        CHANCES.put(Integer.valueOf(21381), Integer.valueOf(510000));
        CHANCES.put(Integer.valueOf(21653), Integer.valueOf(510000));
        CHANCES.put(Integer.valueOf(21387), Integer.valueOf(530000));
        CHANCES.put(Integer.valueOf(21655), Integer.valueOf(530000));
        CHANCES.put(Integer.valueOf(21390), Integer.valueOf(560000));
        CHANCES.put(Integer.valueOf(21656), Integer.valueOf(690000));
        CHANCES.put(Integer.valueOf(21389), Integer.valueOf(550000));
        CHANCES.put(Integer.valueOf(21388), Integer.valueOf(530000));
        CHANCES.put(Integer.valueOf(21383), Integer.valueOf(510000));
        CHANCES.put(Integer.valueOf(21392), Integer.valueOf(560000));
        CHANCES.put(Integer.valueOf(21382), Integer.valueOf(600000));
        CHANCES.put(Integer.valueOf(21654), Integer.valueOf(520000));
        CHANCES.put(Integer.valueOf(21384), Integer.valueOf(640000));
        CHANCES.put(Integer.valueOf(21394), Integer.valueOf(510000));
        CHANCES.put(Integer.valueOf(21395), Integer.valueOf(560000));
        CHANCES.put(Integer.valueOf(21385), Integer.valueOf(520000));
        CHANCES.put(Integer.valueOf(21391), Integer.valueOf(550000));
        CHANCES.put(Integer.valueOf(21393), Integer.valueOf(580000));
        CHANCES.put(Integer.valueOf(21657), Integer.valueOf(570000));
        CHANCES.put(Integer.valueOf(21386), Integer.valueOf(520000));
        CHANCES.put(Integer.valueOf(21652), Integer.valueOf(490000));
        CHANCES.put(Integer.valueOf(21378), Integer.valueOf(490000));
        CHANCES.put(Integer.valueOf(21376), Integer.valueOf(480000));
        CHANCES.put(Integer.valueOf(21377), Integer.valueOf(480000));
        CHANCES.put(Integer.valueOf(21379), Integer.valueOf(590000));
        CHANCES.put(Integer.valueOf(21380), Integer.valueOf(490000));
        setItemsIds(7264);
        addStartNpc(31539, 31271);
        addTalkId(31539, 31271, 32049);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int mobs = iterator.next();
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
                st.giveItems(Integer.valueOf(event), 1);
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
                switch (npc.getNpcId()) {
                    case 31539:
                        htmltext = (st.getQuestItemsCount(7264) >= 1000) ? "31539-04.htm" : "31539-05.htm";
                        break;
                    case 31271:
                        htmltext = "31271-04.htm";
                        break;
                    case 32049:
                        htmltext = (st.getQuestItemsCount(7264) >= 1200) ? "32049-01.htm" : "32049-02.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(7264, 1, 0, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
