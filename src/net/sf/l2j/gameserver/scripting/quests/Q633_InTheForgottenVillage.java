package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q633_InTheForgottenVillage extends Quest {
    private static final String qn = "Q633_InTheForgottenVillage";

    private static final int MINA = 31388;

    private static final int RIB_BONE = 7544;

    private static final int ZOMBIE_LIVER = 7545;

    private static final Map<Integer, Integer> MOBS = new HashMap<>();

    private static final Map<Integer, Integer> UNDEADS = new HashMap<>();

    public Q633_InTheForgottenVillage() {
        super(633, "In the Forgotten Village");
        MOBS.put(21557, 328000);
        MOBS.put(21558, 328000);
        MOBS.put(21559, 337000);
        MOBS.put(21560, 337000);
        MOBS.put(21563, 342000);
        MOBS.put(21564, 348000);
        MOBS.put(21565, 351000);
        MOBS.put(21566, 359000);
        MOBS.put(21567, 359000);
        MOBS.put(21572, 365000);
        MOBS.put(21574, 383000);
        MOBS.put(21575, 383000);
        MOBS.put(21580, 385000);
        MOBS.put(21581, 395000);
        MOBS.put(21583, 397000);
        MOBS.put(21584, 401000);
        UNDEADS.put(21553, 347000);
        UNDEADS.put(21554, 347000);
        UNDEADS.put(21561, 450000);
        UNDEADS.put(21578, 501000);
        UNDEADS.put(21596, 359000);
        UNDEADS.put(21597, 370000);
        UNDEADS.put(21598, 441000);
        UNDEADS.put(21599, 395000);
        UNDEADS.put(21600, 408000);
        UNDEADS.put(21601, 411000);
        setItemsIds(7544, 7545);
        addStartNpc(31388);
        addTalkId(31388);
        Iterator<Integer> iterator;
        for (iterator = MOBS.keySet().iterator(); iterator.hasNext(); ) {
            int i = iterator.next();
            addKillId(i);
        }
        for (iterator = UNDEADS.keySet().iterator(); iterator.hasNext(); ) {
            int i = iterator.next();
            addKillId(i);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q633_InTheForgottenVillage");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31388-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31388-10.htm")) {
            st.takeItems(7544, -1);
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("31388-09.htm")) {
            if (st.getQuestItemsCount(7544) >= 200) {
                htmltext = "31388-08.htm";
                st.takeItems(7544, 200);
                st.rewardItems(57, 25000);
                st.rewardExpAndSp(305235L, 0);
                st.playSound("ItemSound.quest_finish");
            }
            st.set("cond", "1");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q633_InTheForgottenVillage");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 65) ? "31388-03.htm" : "31388-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31388-06.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "31388-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        int npcId = npc.getNpcId();
        if (UNDEADS.containsKey(npcId)) {
            QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
            if (st == null)
                return null;
            st.dropItems(7545, 1, 0, UNDEADS.get(npcId));
        } else if (MOBS.containsKey(npcId)) {
            QuestState st = getRandomPartyMember(player, npc, "1");
            if (st == null)
                return null;
            if (st.dropItems(7544, 1, 200, MOBS.get(npcId)))
                st.set("cond", "2");
        }
        return null;
    }
}
