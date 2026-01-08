package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q014_WhereaboutsOfTheArchaeologist extends Quest {
    private static final String qn = "Q014_WhereaboutsOfTheArchaeologist";

    private static final int LIESEL = 31263;

    private static final int GHOST_OF_ADVENTURER = 31538;

    private static final int LETTER = 7253;

    public Q014_WhereaboutsOfTheArchaeologist() {
        super(14, "Whereabouts of the Archaeologist");
        setItemsIds(7253);
        addStartNpc(31263);
        addTalkId(31263, 31538);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q014_WhereaboutsOfTheArchaeologist");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31263-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7253, 1);
        } else if (event.equalsIgnoreCase("31538-1.htm")) {
            st.takeItems(7253, 1);
            st.rewardItems(57, 113228);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q014_WhereaboutsOfTheArchaeologist");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 74) ? "31263-1.htm" : "31263-0.htm";
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 31263 -> "31263-2.htm";
                    case 31538 -> "31538-0.htm";
                    default -> htmltext;
                };
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
