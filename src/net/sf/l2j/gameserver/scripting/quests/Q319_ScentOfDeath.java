package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q319_ScentOfDeath extends Quest {
    private static final String qn = "Q319_ScentOfDeath";

    private static final int ZOMBIE_SKIN = 1045;

    public Q319_ScentOfDeath() {
        super(319, "Scent of Death");
        setItemsIds(1045);
        addStartNpc(30138);
        addTalkId(30138);
        addKillId(20015, 20020);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q319_ScentOfDeath");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30138-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q319_ScentOfDeath");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 11) ? "30138-02.htm" : "30138-03.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30138-05.htm";
                    break;
                }
                htmltext = "30138-06.htm";
                st.takeItems(1045, -1);
                st.rewardItems(57, 3350);
                st.rewardItems(1060, 1);
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
        if (st.dropItems(1045, 1, 5, 200000))
            st.set("cond", "2");
        return null;
    }
}
