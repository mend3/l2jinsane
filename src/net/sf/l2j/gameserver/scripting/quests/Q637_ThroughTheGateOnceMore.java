package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q637_ThroughTheGateOnceMore extends Quest {
    private static final String qn = "Q637_ThroughTheGateOnceMore";

    private static final int FLAURON = 32010;

    private static final int FADED_VISITOR_MARK = 8065;

    private static final int NECROMANCER_HEART = 8066;

    private static final int PAGAN_MARK = 8067;

    public Q637_ThroughTheGateOnceMore() {
        super(637, "Through the Gate Once More");
        setItemsIds(8066);
        addStartNpc(32010);
        addTalkId(32010);
        addKillId(21565, 21566, 21567);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q637_ThroughTheGateOnceMore");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32010-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32010-10.htm")) {
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q637_ThroughTheGateOnceMore");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 73 || !st.hasQuestItems(8065)) {
                    htmltext = "32010-01a.htm";
                    break;
                }
                if (st.hasQuestItems(8067)) {
                    htmltext = "32010-00.htm";
                    break;
                }
                htmltext = "32010-01.htm";
                break;
            case 1:
                if (st.getInt("cond") == 2) {
                    if (st.getQuestItemsCount(8066) == 10) {
                        htmltext = "32010-06.htm";
                        st.takeItems(8065, 1);
                        st.takeItems(8066, -1);
                        st.giveItems(8067, 1);
                        st.giveItems(8273, 10);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    }
                    st.set("cond", "1");
                    break;
                }
                htmltext = "32010-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(8066, 1, 10, 400000))
            st.set("cond", "2");
        return null;
    }
}
