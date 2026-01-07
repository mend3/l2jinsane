package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q004_LongliveThePaagrioLord extends Quest {
    private static final String qn = "Q004_LongliveThePaagrioLord";

    private static final Map<Integer, Integer> NPC_GIFTS = new HashMap<>();

    public Q004_LongliveThePaagrioLord() {
        super(4, "Long live the Pa'agrio Lord!");
        NPC_GIFTS.put(Integer.valueOf(30585), Integer.valueOf(1542));
        NPC_GIFTS.put(Integer.valueOf(30566), Integer.valueOf(1541));
        NPC_GIFTS.put(Integer.valueOf(30562), Integer.valueOf(1543));
        NPC_GIFTS.put(Integer.valueOf(30560), Integer.valueOf(1544));
        NPC_GIFTS.put(Integer.valueOf(30559), Integer.valueOf(1545));
        NPC_GIFTS.put(Integer.valueOf(30587), Integer.valueOf(1546));
        setItemsIds(1541, 1542, 1543, 1544, 1545, 1546);
        addStartNpc(30578);
        addTalkId(30578, 30585, 30566, 30562, 30560, 30559, 30587);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q004_LongliveThePaagrioLord");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30578-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, npcId, i, count;
        Iterator<Integer> iterator;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q004_LongliveThePaagrioLord");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30578-00.htm";
                    break;
                }
                if (player.getLevel() < 2) {
                    htmltext = "30578-01.htm";
                    break;
                }
                htmltext = "30578-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                npcId = npc.getNpcId();
                if (npcId == 30578) {
                    if (cond == 1) {
                        htmltext = "30578-04.htm";
                        break;
                    }
                    if (cond == 2) {
                        htmltext = "30578-06.htm";
                        st.giveItems(4, 1);
                        for (Iterator<Integer> iterator1 = NPC_GIFTS.values().iterator(); iterator1.hasNext(); ) {
                            int item = iterator1.next();
                            st.takeItems(item, -1);
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(false);
                    }
                    break;
                }
                i = NPC_GIFTS.get(Integer.valueOf(npcId));
                if (st.hasQuestItems(i)) {
                    htmltext = npcId + "-02.htm";
                    break;
                }
                st.giveItems(i, 1);
                htmltext = npcId + "-01.htm";
                count = 0;
                for (iterator = NPC_GIFTS.values().iterator(); iterator.hasNext(); ) {
                    int item = iterator.next();
                    count += st.getQuestItemsCount(item);
                }
                if (count == 6) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                st.playSound("ItemSound.quest_itemget");
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
