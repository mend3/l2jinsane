package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q267_WrathOfVerdure extends Quest {
    private static final String qn = "Q267_WrathOfVerdure";

    private static final int GOBLIN_CLUB = 1335;

    private static final int SILVERY_LEAF = 1340;

    public Q267_WrathOfVerdure() {
        super(267, "Wrath of Verdure");
        setItemsIds(1335);
        addStartNpc(31853);
        addTalkId(31853);
        addKillId(20325);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q267_WrathOfVerdure");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31853-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31853-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q267_WrathOfVerdure");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "31853-00.htm";
                    break;
                }
                if (player.getLevel() < 4) {
                    htmltext = "31853-01.htm";
                    break;
                }
                htmltext = "31853-02.htm";
                break;
            case 1:
                count = st.getQuestItemsCount(1335);
                if (count > 0) {
                    htmltext = "31853-05.htm";
                    st.takeItems(1335, -1);
                    st.rewardItems(1340, count);
                    break;
                }
                htmltext = "31853-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(1335, 1, 0, 500000);
        return null;
    }
}
