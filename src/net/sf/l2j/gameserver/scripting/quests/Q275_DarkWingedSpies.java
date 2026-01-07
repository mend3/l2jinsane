package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q275_DarkWingedSpies extends Quest {
    private static final String qn = "Q275_DarkWingedSpies";

    private static final int DARKWING_BAT = 20316;

    private static final int VARANGKA_TRACKER = 27043;

    private static final int DARKWING_BAT_FANG = 1478;

    private static final int VARANGKA_PARASITE = 1479;

    public Q275_DarkWingedSpies() {
        super(275, "Dark Winged Spies");
        setItemsIds(1478, 1479);
        addStartNpc(30567);
        addTalkId(30567);
        addKillId(20316, 27043);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q275_DarkWingedSpies");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30567-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q275_DarkWingedSpies");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30567-00.htm";
                    break;
                }
                if (player.getLevel() < 11) {
                    htmltext = "30567-01.htm";
                    break;
                }
                htmltext = "30567-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30567-04.htm";
                    break;
                }
                htmltext = "30567-05.htm";
                st.takeItems(1478, -1);
                st.takeItems(1479, -1);
                st.rewardItems(57, 4200);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20316:
                if (st.dropItemsAlways(1478, 1, 70)) {
                    st.set("cond", "2");
                    break;
                }
                if (Rnd.get(100) < 10 && st.getQuestItemsCount(1478) > 10 && st.getQuestItemsCount(1478) < 66) {
                    addSpawn(27043, npc, true, 0L, true);
                    st.giveItems(1479, 1);
                }
                break;
            case 27043:
                if (st.hasQuestItems(1479)) {
                    st.takeItems(1479, -1);
                    if (st.dropItemsAlways(1478, 5, 70))
                        st.set("cond", "2");
                }
                break;
        }
        return null;
    }
}
