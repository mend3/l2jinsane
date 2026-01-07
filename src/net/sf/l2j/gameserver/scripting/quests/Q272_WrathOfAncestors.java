package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q272_WrathOfAncestors extends Quest {
    private static final String qn = "Q272_WrathOfAncestors";

    private static final int GRAVE_ROBBERS_HEAD = 1474;

    public Q272_WrathOfAncestors() {
        super(272, "Wrath of Ancestors");
        setItemsIds(1474);
        addStartNpc(30572);
        addTalkId(30572);
        addKillId(20319, 20320);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q272_WrathOfAncestors");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30572-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q272_WrathOfAncestors");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30572-00.htm";
                    break;
                }
                if (player.getLevel() < 5) {
                    htmltext = "30572-01.htm";
                    break;
                }
                htmltext = "30572-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30572-04.htm";
                    break;
                }
                htmltext = "30572-05.htm";
                st.takeItems(1474, -1);
                st.rewardItems(57, 1500);
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
        if (st.dropItemsAlways(1474, 1, 50))
            st.set("cond", "2");
        return null;
    }
}
