package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q650_ABrokenDream extends Quest {
    private static final String qn = "Q650_ABrokenDream";

    private static final int GHOST = 32054;

    private static final int DREAM_FRAGMENT = 8514;

    private static final int CREWMAN = 22027;

    private static final int VAGABOND = 22028;

    public Q650_ABrokenDream() {
        super(650, "A Broken Dream");
        setItemsIds(8514);
        addStartNpc(32054);
        addTalkId(32054);
        addKillId(22027, 22028);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q650_ABrokenDream");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32054-01a.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32054-03.htm")) {
            if (!st.hasQuestItems(8514))
                htmltext = "32054-04.htm";
        } else if (event.equalsIgnoreCase("32054-05.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2, st = player.getQuestState("Q650_ABrokenDream");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                st2 = player.getQuestState("Q117_TheOceanOfDistantStars");
                if (st2 != null && st2.isCompleted() && player.getLevel() >= 39) {
                    htmltext = "32054-01.htm";
                    break;
                }
                htmltext = "32054-00.htm";
                st.exitQuest(true);
                break;
            case 1:
                htmltext = "32054-02.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8514, 1, 0, 250000);
        return null;
    }
}
