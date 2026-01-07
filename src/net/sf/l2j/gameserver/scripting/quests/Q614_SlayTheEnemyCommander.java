package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q614_SlayTheEnemyCommander extends Quest {
    private static final String qn = "Q614_SlayTheEnemyCommander";

    private static final int HEAD_OF_TAYR = 7241;

    private static final int FEATHER_OF_WISDOM = 7230;

    private static final int VARKA_ALLIANCE_4 = 7224;

    public Q614_SlayTheEnemyCommander() {
        super(614, "Slay the enemy commander!");
        setItemsIds(7241);
        addStartNpc(31377);
        addTalkId(31377);
        addKillId(25302);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q614_SlayTheEnemyCommander");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31377-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31377-07.htm")) {
            if (st.hasQuestItems(7241)) {
                st.takeItems(7241, -1);
                st.giveItems(7230, 1);
                st.rewardExpAndSp(10000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31377-06.htm";
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q614_SlayTheEnemyCommander");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 75) {
                    if (player.getAllianceWithVarkaKetra() <= -4 && st.hasQuestItems(7224) && !st.hasQuestItems(7230)) {
                        htmltext = "31377-01.htm";
                        break;
                    }
                    htmltext = "31377-02.htm";
                    break;
                }
                htmltext = "31377-03.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7241) ? "31377-05.htm" : "31377-06.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "1")) {
                if (st.getPlayer().getAllianceWithVarkaKetra() <= -4 && st.hasQuestItems(7224)) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(7241, 1);
                }
            }
        return null;
    }
}
