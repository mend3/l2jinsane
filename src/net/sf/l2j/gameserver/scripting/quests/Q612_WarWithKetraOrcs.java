package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q612_WarWithKetraOrcs extends Quest {
    private static final String qn = "Q612_WarWithKetraOrcs";

    private static final int NEPENTHES_SEED = 7187;

    private static final int MOLAR_OF_KETRA_ORC = 7234;

    public Q612_WarWithKetraOrcs() {
        super(612, "War with Ketra Orcs");
        setItemsIds(7234);
        addStartNpc(31377);
        addTalkId(31377);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q612_WarWithKetraOrcs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31377-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31377-07.htm")) {
            if (st.getQuestItemsCount(7234) >= 100) {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(7234, 100);
                st.giveItems(7187, 20);
            } else {
                htmltext = "31377-08.htm";
            }
        } else if (event.equalsIgnoreCase("31377-09.htm")) {
            st.takeItems(7234, -1);
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q612_WarWithKetraOrcs");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() >= 74 && player.isAlliedWithVarka()) ? "31377-01.htm" : "31377-02.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7234) ? "31377-04.htm" : "31377-05.htm";
                break;
        }
        return htmltext;
    }
}
