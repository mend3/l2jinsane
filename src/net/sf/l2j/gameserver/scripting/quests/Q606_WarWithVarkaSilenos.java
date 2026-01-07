package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q606_WarWithVarkaSilenos extends Quest {
    private static final String qn = "Q606_WarWithVarkaSilenos";

    private static final int HORN_OF_BUFFALO = 7186;

    private static final int VARKA_MANE = 7233;

    public Q606_WarWithVarkaSilenos() {
        super(606, "War with Varka Silenos");
        setItemsIds(7233);
        addStartNpc(31370);
        addTalkId(31370);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q606_WarWithVarkaSilenos");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31370-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31370-07.htm")) {
            if (st.getQuestItemsCount(7233) >= 100) {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(7233, 100);
                st.giveItems(7186, 20);
            } else {
                htmltext = "31370-08.htm";
            }
        } else if (event.equalsIgnoreCase("31370-09.htm")) {
            st.takeItems(7233, -1);
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q606_WarWithVarkaSilenos");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() >= 74 && player.isAlliedWithKetra()) ? "31370-01.htm" : "31370-02.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7233) ? "31370-04.htm" : "31370-05.htm";
                break;
        }
        return htmltext;
    }
}
