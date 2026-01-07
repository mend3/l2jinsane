package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q328_SenseForBusiness extends Quest {
    private static final String qn = "Q328_SenseForBusiness";

    private static final int MONSTER_EYE_LENS = 1366;

    private static final int MONSTER_EYE_CARCASS = 1347;

    private static final int BASILISK_GIZZARD = 1348;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q328_SenseForBusiness() {
        super(328, "Sense for Business");
        CHANCES.put(20055, 48);
        CHANCES.put(20059, 52);
        CHANCES.put(20067, 68);
        CHANCES.put(20068, 76);
        CHANCES.put(20070, 500000);
        CHANCES.put(20072, 510000);
        setItemsIds(1366, 1347, 1348);
        addStartNpc(30436);
        addTalkId(30436);
        addKillId(20055, 20059, 20067, 20068, 20070, 20072);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q328_SenseForBusiness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30436-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30436-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int carcasses, lenses, gizzards, all;
        QuestState st = player.getQuestState("Q328_SenseForBusiness");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 21) ? "30436-01.htm" : "30436-02.htm";
                break;
            case 1:
                carcasses = st.getQuestItemsCount(1347);
                lenses = st.getQuestItemsCount(1366);
                gizzards = st.getQuestItemsCount(1348);
                all = carcasses + lenses + gizzards;
                if (all == 0) {
                    htmltext = "30436-04.htm";
                    break;
                }
                htmltext = "30436-05.htm";
                st.takeItems(1347, -1);
                st.takeItems(1366, -1);
                st.takeItems(1348, -1);
                st.rewardItems(57, 25 * carcasses + 1000 * lenses + 60 * gizzards + ((all >= 10) ? 618 : 0));
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        int chance = CHANCES.get(npcId);
        if (npcId < 20069) {
            int rnd = Rnd.get(100);
            if (rnd < chance + 1)
                st.dropItemsAlways((rnd < chance) ? 1347 : 1366, 1, 0);
        } else {
            st.dropItems(1348, 1, 0, chance);
        }
        return null;
    }
}
