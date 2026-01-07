package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q629_CleanUpTheSwampOfScreams extends Quest {
    private static final String qn = "Q629_CleanUpTheSwampOfScreams";

    private static final int PIERCE = 31553;

    private static final int TALON_OF_STAKATO = 7250;

    private static final int GOLDEN_RAM_COIN = 7251;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q629_CleanUpTheSwampOfScreams() {
        super(629, "Clean up the Swamp of Screams");
        CHANCES.put(21508, 500000);
        CHANCES.put(21509, 431000);
        CHANCES.put(21510, 521000);
        CHANCES.put(21511, 576000);
        CHANCES.put(21512, 746000);
        CHANCES.put(21513, 530000);
        CHANCES.put(21514, 538000);
        CHANCES.put(21515, 545000);
        CHANCES.put(21516, 553000);
        CHANCES.put(21517, 560000);
        setItemsIds(7250, 7251);
        addStartNpc(31553);
        addTalkId(31553);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q629_CleanUpTheSwampOfScreams");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31553-1.htm")) {
            if (player.getLevel() >= 66) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "31553-0a.htm";
                st.exitQuest(true);
            }
        } else if (event.equalsIgnoreCase("31553-3.htm")) {
            if (st.getQuestItemsCount(7250) >= 100) {
                st.takeItems(7250, 100);
                st.giveItems(7251, 20);
            } else {
                htmltext = "31553-3a.htm";
            }
        } else if (event.equalsIgnoreCase("31553-5.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q629_CleanUpTheSwampOfScreams");
        if (st == null)
            return htmltext;
        if (!st.hasAtLeastOneQuestItem(7246, 7247))
            return "31553-6.htm";
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 66) ? "31553-0a.htm" : "31553-0.htm";
                break;
            case 1:
                htmltext = (st.getQuestItemsCount(7250) >= 100) ? "31553-2.htm" : "31553-1a.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(7250, 1, 100, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
