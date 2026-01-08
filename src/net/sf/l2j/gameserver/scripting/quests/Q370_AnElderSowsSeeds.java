package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q370_AnElderSowsSeeds extends Quest {
    private static final String qn = "Q370_AnElderSowsSeeds";

    private static final int CASIAN = 30612;

    private static final int SPELLBOOK_PAGE = 5916;

    private static final int CHAPTER_OF_FIRE = 5917;

    private static final int CHAPTER_OF_WATER = 5918;

    private static final int CHAPTER_OF_WIND = 5919;

    private static final int CHAPTER_OF_EARTH = 5920;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q370_AnElderSowsSeeds() {
        super(370, "An Elder Sows Seeds");
        CHANCES.put(20082, 86000);
        CHANCES.put(20084, 94000);
        CHANCES.put(20086, 90000);
        CHANCES.put(20089, 100000);
        CHANCES.put(20090, 202000);
        setItemsIds(5916, 5917, 5918, 5919, 5920);
        addStartNpc(30612);
        addTalkId(30612);
        addKillId(20082, 20084, 20086, 20089, 20090);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q370_AnElderSowsSeeds");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30612-3.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30612-6.htm")) {
            if (st.hasQuestItems(5917, 5918, 5919, 5920)) {
                htmltext = "30612-8.htm";
                st.takeItems(5917, 1);
                st.takeItems(5918, 1);
                st.takeItems(5919, 1);
                st.takeItems(5920, 1);
                st.rewardItems(57, 3600);
            }
        } else if (event.equalsIgnoreCase("30612-9.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q370_AnElderSowsSeeds");
        if (st == null)
            return htmltext;
        htmltext = switch (st.getState()) {
            case 0 -> (player.getLevel() < 28) ? "30612-0a.htm" : "30612-0.htm";
            case 1 -> "30612-4.htm";
            default -> htmltext;
        };
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5916, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
