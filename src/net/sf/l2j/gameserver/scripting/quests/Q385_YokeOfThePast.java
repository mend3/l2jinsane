package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q385_YokeOfThePast extends Quest {
    private static final String qn = "Q385_YokeOfThePast";

    private static final int[] GATEKEEPER_ZIGGURAT = new int[]{
            31095, 31096, 31097, 31098, 31099, 31100, 31101, 31102, 31103, 31104,
            31105, 31106, 31107, 31108, 31109, 31110, 31114, 31115, 31116, 31117,
            31118, 31119, 31120, 31121, 31122, 31123, 31124, 31125, 31126};

    private static final int ANCIENT_SCROLL = 5902;

    private static final int BLANK_SCROLL = 5965;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q385_YokeOfThePast() {
        super(385, "Yoke of the Past");
        CHANCES.put(Integer.valueOf(21208), Integer.valueOf(70000));
        CHANCES.put(Integer.valueOf(21209), Integer.valueOf(80000));
        CHANCES.put(Integer.valueOf(21210), Integer.valueOf(110000));
        CHANCES.put(Integer.valueOf(21211), Integer.valueOf(110000));
        CHANCES.put(Integer.valueOf(21213), Integer.valueOf(140000));
        CHANCES.put(Integer.valueOf(21214), Integer.valueOf(190000));
        CHANCES.put(Integer.valueOf(21215), Integer.valueOf(190000));
        CHANCES.put(Integer.valueOf(21217), Integer.valueOf(240000));
        CHANCES.put(Integer.valueOf(21218), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(21219), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(21221), Integer.valueOf(370000));
        CHANCES.put(Integer.valueOf(21222), Integer.valueOf(460000));
        CHANCES.put(Integer.valueOf(21223), Integer.valueOf(450000));
        CHANCES.put(Integer.valueOf(21224), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(21225), Integer.valueOf(540000));
        CHANCES.put(Integer.valueOf(21226), Integer.valueOf(660000));
        CHANCES.put(Integer.valueOf(21227), Integer.valueOf(640000));
        CHANCES.put(Integer.valueOf(21228), Integer.valueOf(700000));
        CHANCES.put(Integer.valueOf(21229), Integer.valueOf(750000));
        CHANCES.put(Integer.valueOf(21230), Integer.valueOf(910000));
        CHANCES.put(Integer.valueOf(21231), Integer.valueOf(860000));
        CHANCES.put(Integer.valueOf(21236), Integer.valueOf(120000));
        CHANCES.put(Integer.valueOf(21237), Integer.valueOf(140000));
        CHANCES.put(Integer.valueOf(21238), Integer.valueOf(190000));
        CHANCES.put(Integer.valueOf(21239), Integer.valueOf(190000));
        CHANCES.put(Integer.valueOf(21240), Integer.valueOf(220000));
        CHANCES.put(Integer.valueOf(21241), Integer.valueOf(240000));
        CHANCES.put(Integer.valueOf(21242), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(21243), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(21244), Integer.valueOf(340000));
        CHANCES.put(Integer.valueOf(21245), Integer.valueOf(370000));
        CHANCES.put(Integer.valueOf(21246), Integer.valueOf(460000));
        CHANCES.put(Integer.valueOf(21247), Integer.valueOf(450000));
        CHANCES.put(Integer.valueOf(21248), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(21249), Integer.valueOf(540000));
        CHANCES.put(Integer.valueOf(21250), Integer.valueOf(660000));
        CHANCES.put(Integer.valueOf(21251), Integer.valueOf(640000));
        CHANCES.put(Integer.valueOf(21252), Integer.valueOf(700000));
        CHANCES.put(Integer.valueOf(21253), Integer.valueOf(750000));
        CHANCES.put(Integer.valueOf(21254), Integer.valueOf(910000));
        CHANCES.put(Integer.valueOf(21255), Integer.valueOf(860000));
        setItemsIds(5902);
        addStartNpc(GATEKEEPER_ZIGGURAT);
        addTalkId(GATEKEEPER_ZIGGURAT);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q385_YokeOfThePast");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("10.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q385_YokeOfThePast");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "02.htm" : "01.htm";
                break;
            case 1:
                if (!st.hasQuestItems(5902)) {
                    htmltext = "08.htm";
                    break;
                }
                htmltext = "09.htm";
                count = st.getQuestItemsCount(5902);
                st.takeItems(5902, -1);
                st.rewardItems(5965, count);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5902, 1, 0, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
