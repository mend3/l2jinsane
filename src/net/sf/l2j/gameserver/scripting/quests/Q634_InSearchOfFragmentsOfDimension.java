package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q634_InSearchOfFragmentsOfDimension extends Quest {
    private static final String qn = "Q634_InSearchOfFragmentsOfDimension";

    private static final int DIMENSION_FRAGMENT = 7079;

    public Q634_InSearchOfFragmentsOfDimension() {
        super(634, "In Search of Fragments of Dimension");
        int i;
        for (i = 31494; i < 31508; i++) {
            addStartNpc(i);
            addTalkId(i);
        }
        for (i = 21208; i < 21256; i++) {
            addKillId(i);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q634_InSearchOfFragmentsOfDimension");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("05.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q634_InSearchOfFragmentsOfDimension");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "01a.htm" : "01.htm";
                break;
            case 1:
                htmltext = "03.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(7079, (int) (npc.getLevel() * 0.15D + 1.6D), -1, 900000);
        return null;
    }
}
