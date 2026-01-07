package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q329_CuriosityOfADwarf extends Quest {
    private static final String qn = "Q329_CuriosityOfADwarf";

    private static final int GOLEM_HEARTSTONE = 1346;

    private static final int BROKEN_HEARTSTONE = 1365;

    public Q329_CuriosityOfADwarf() {
        super(329, "Curiosity of a Dwarf");
        addStartNpc(30437);
        addTalkId(30437);
        addKillId(20083, 20085);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q329_CuriosityOfADwarf");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30437-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30437-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int golem, broken;
        QuestState st = player.getQuestState("Q329_CuriosityOfADwarf");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 33) ? "30437-01.htm" : "30437-02.htm";
                break;
            case 1:
                golem = st.getQuestItemsCount(1346);
                broken = st.getQuestItemsCount(1365);
                if (golem + broken == 0) {
                    htmltext = "30437-04.htm";
                    break;
                }
                htmltext = "30437-05.htm";
                st.takeItems(1346, -1);
                st.takeItems(1365, -1);
                st.rewardItems(57, broken * 50 + golem * 1000 + ((golem + broken > 10) ? 1183 : 0));
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int chance = Rnd.get(100);
        if (chance < 2) {
            st.dropItemsAlways(1346, 1, 0);
        } else if (chance < ((npc.getNpcId() == 20083) ? 44 : 50)) {
            st.dropItemsAlways(1365, 1, 0);
        }
        return null;
    }
}
