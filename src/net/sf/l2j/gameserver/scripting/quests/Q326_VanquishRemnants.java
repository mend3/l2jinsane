package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q326_VanquishRemnants extends Quest {
    private static final String qn = "Q326_VanquishRemnants";

    private static final int RED_CROSS_BADGE = 1359;

    private static final int BLUE_CROSS_BADGE = 1360;

    private static final int BLACK_CROSS_BADGE = 1361;

    private static final int BLACK_LION_MARK = 1369;

    public Q326_VanquishRemnants() {
        super(326, "Vanquish Remnants");
        setItemsIds(1359, 1360, 1361);
        addStartNpc(30435);
        addTalkId(30435);
        addKillId(20053, 20437, 20058, 20436, 20061, 20439, 20063, 20066, 20438);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q326_VanquishRemnants");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30435-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30435-07.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int redBadges, blueBadges, blackBadges, badgesSum;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q326_VanquishRemnants");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 21) ? "30435-01.htm" : "30435-02.htm";
                break;
            case 1:
                redBadges = st.getQuestItemsCount(1359);
                blueBadges = st.getQuestItemsCount(1360);
                blackBadges = st.getQuestItemsCount(1361);
                badgesSum = redBadges + blueBadges + blackBadges;
                if (badgesSum > 0) {
                    st.takeItems(1359, -1);
                    st.takeItems(1360, -1);
                    st.takeItems(1361, -1);
                    st.rewardItems(57, redBadges * 46 + blueBadges * 52 + blackBadges * 58 + ((badgesSum >= 10) ? 4320 : 0));
                    if (badgesSum >= 100) {
                        if (!st.hasQuestItems(1369)) {
                            htmltext = "30435-06.htm";
                            st.giveItems(1369, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30435-09.htm";
                        break;
                    }
                    htmltext = "30435-05.htm";
                    break;
                }
                htmltext = "30435-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20053:
            case 20058:
            case 20437:
                st.dropItems(1359, 1, 0, 330000);
                break;
            case 20061:
            case 20063:
            case 20436:
            case 20439:
                st.dropItems(1360, 1, 0, 160000);
                break;
            case 20066:
            case 20438:
                st.dropItems(1361, 1, 0, 120000);
                break;
        }
        return null;
    }
}
