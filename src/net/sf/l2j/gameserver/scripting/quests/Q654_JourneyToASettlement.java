package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q654_JourneyToASettlement extends Quest {
    private static final String qn = "Q654_JourneyToASettlement";

    private static final int ANTELOPE_SKIN = 8072;

    private static final int FORCE_FIELD_REMOVAL_SCROLL = 8073;

    public Q654_JourneyToASettlement() {
        super(654, "Journey to a Settlement");
        setItemsIds(8072);
        addStartNpc(31453);
        addTalkId(31453);
        addKillId(21294, 21295);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q654_JourneyToASettlement");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31453-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31453-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31453-06.htm")) {
            st.takeItems(8072, -1);
            st.giveItems(8073, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState prevSt;
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q654_JourneyToASettlement");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                prevSt = player.getQuestState("Q119_LastImperialPrince");
                htmltext = (prevSt == null || !prevSt.isCompleted() || player.getLevel() < 74) ? "31453-00.htm" : "31453-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31453-02.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "31453-04.htm";
                    break;
                }
                if (cond == 3)
                    htmltext = "31453-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "2");
        if (st == null)
            return null;
        if (st.dropItems(8072, 1, 1, 50000))
            st.set("cond", "3");
        return null;
    }
}
