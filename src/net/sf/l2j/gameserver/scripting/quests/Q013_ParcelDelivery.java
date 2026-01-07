package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q013_ParcelDelivery extends Quest {
    private static final String qn = "Q013_ParcelDelivery";

    private static final int FUNDIN = 31274;

    private static final int VULCAN = 31539;

    private static final int PACKAGE = 7263;

    public Q013_ParcelDelivery() {
        super(13, "Parcel Delivery");
        setItemsIds(7263);
        addStartNpc(31274);
        addTalkId(31274, 31539);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q013_ParcelDelivery");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31274-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7263, 1);
        } else if (event.equalsIgnoreCase("31539-1.htm")) {
            st.takeItems(7263, 1);
            st.rewardItems(57, 82656);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q013_ParcelDelivery");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 74) ? "31274-1.htm" : "31274-0.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31274:
                        htmltext = "31274-2.htm";
                        break;
                    case 31539:
                        htmltext = "31539-0.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
