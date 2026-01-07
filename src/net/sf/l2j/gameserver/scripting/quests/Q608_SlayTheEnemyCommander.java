package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q608_SlayTheEnemyCommander extends Quest {
    private static final String qn = "Q608_SlayTheEnemyCommander";

    private static final int HEAD_OF_MOS = 7236;

    private static final int TOTEM_OF_WISDOM = 7220;

    private static final int KETRA_ALLIANCE_4 = 7214;

    public Q608_SlayTheEnemyCommander() {
        super(608, "Slay the enemy commander!");
        setItemsIds(7236);
        addStartNpc(31370);
        addTalkId(31370);
        addKillId(25312);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q608_SlayTheEnemyCommander");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31370-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31370-07.htm")) {
            if (st.hasQuestItems(7236)) {
                st.takeItems(7236, -1);
                st.giveItems(7220, 1);
                st.rewardExpAndSp(10000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31370-06.htm";
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q608_SlayTheEnemyCommander");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 75) {
                    if (player.getAllianceWithVarkaKetra() >= 4 && st.hasQuestItems(7214) && !st.hasQuestItems(7220)) {
                        htmltext = "31370-01.htm";
                        break;
                    }
                    htmltext = "31370-02.htm";
                    break;
                }
                htmltext = "31370-03.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7236) ? "31370-05.htm" : "31370-06.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "1")) {
                if (st.getPlayer().getAllianceWithVarkaKetra() >= 4 && st.hasQuestItems(7214)) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(7236, 1);
                }
            }
        return null;
    }
}
