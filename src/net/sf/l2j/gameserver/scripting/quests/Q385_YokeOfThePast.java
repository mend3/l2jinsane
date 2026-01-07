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
        CHANCES.put(21208, 70000);
        CHANCES.put(21209, 80000);
        CHANCES.put(21210, 110000);
        CHANCES.put(21211, 110000);
        CHANCES.put(21213, 140000);
        CHANCES.put(21214, 190000);
        CHANCES.put(21215, 190000);
        CHANCES.put(21217, 240000);
        CHANCES.put(21218, 300000);
        CHANCES.put(21219, 300000);
        CHANCES.put(21221, 370000);
        CHANCES.put(21222, 460000);
        CHANCES.put(21223, 450000);
        CHANCES.put(21224, 500000);
        CHANCES.put(21225, 540000);
        CHANCES.put(21226, 660000);
        CHANCES.put(21227, 640000);
        CHANCES.put(21228, 700000);
        CHANCES.put(21229, 750000);
        CHANCES.put(21230, 910000);
        CHANCES.put(21231, 860000);
        CHANCES.put(21236, 120000);
        CHANCES.put(21237, 140000);
        CHANCES.put(21238, 190000);
        CHANCES.put(21239, 190000);
        CHANCES.put(21240, 220000);
        CHANCES.put(21241, 240000);
        CHANCES.put(21242, 300000);
        CHANCES.put(21243, 300000);
        CHANCES.put(21244, 340000);
        CHANCES.put(21245, 370000);
        CHANCES.put(21246, 460000);
        CHANCES.put(21247, 450000);
        CHANCES.put(21248, 500000);
        CHANCES.put(21249, 540000);
        CHANCES.put(21250, 660000);
        CHANCES.put(21251, 640000);
        CHANCES.put(21252, 700000);
        CHANCES.put(21253, 750000);
        CHANCES.put(21254, 910000);
        CHANCES.put(21255, 860000);
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
        st.dropItems(5902, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
