package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q053_LinnaeusSpecialBait extends Quest {
    private static final String qn = "Q053_LinnaeusSpecialBait";

    private static final int CRIMSON_DRAKE_HEART = 7624;

    private static final int FLAMING_FISHING_LURE = 7613;

    public Q053_LinnaeusSpecialBait() {
        super(53, "Linnaues' Special Bait");
        setItemsIds(7624);
        addStartNpc(31577);
        addTalkId(31577);
        addKillId(20670);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q053_LinnaeusSpecialBait");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31577-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31577-07.htm")) {
            htmltext = "31577-06.htm";
            st.takeItems(7624, -1);
            st.rewardItems(7613, 4);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q053_LinnaeusSpecialBait");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        htmltext = switch (st.getState()) {
            case 0 -> (player.getLevel() < 60) ? "31577-02.htm" : "31577-01.htm";
            case 1 -> (st.getQuestItemsCount(7624) == 100) ? "31577-04.htm" : "31577-05.htm";
            case 2 -> getAlreadyCompletedMsg();
            default -> htmltext;
        };
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(7624, 1, 100, 500000))
            st.set("cond", "2");
        return null;
    }
}
