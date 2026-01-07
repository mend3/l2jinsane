package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q623_TheFinestFood extends Quest {
    private static final String qn = "Q623_TheFinestFood";

    private static final int LEAF_OF_FLAVA = 7199;

    private static final int BUFFALO_MEAT = 7200;

    private static final int ANTELOPE_HORN = 7201;

    private static final int JEREMY = 31521;

    private static final int FLAVA = 21316;

    private static final int BUFFALO = 21315;

    private static final int ANTELOPE = 21318;

    public Q623_TheFinestFood() {
        super(623, "The Finest Food");
        setItemsIds(7199, 7200, 7201);
        addStartNpc(31521);
        addTalkId(31521);
        addKillId(21316, 21315, 21318);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q623_TheFinestFood");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31521-02.htm")) {
            if (player.getLevel() >= 71) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "31521-03.htm";
            }
        } else if (event.equalsIgnoreCase("31521-05.htm")) {
            st.takeItems(7199, -1);
            st.takeItems(7200, -1);
            st.takeItems(7201, -1);
            int luck = Rnd.get(100);
            if (luck < 11) {
                st.rewardItems(57, 25000);
                st.giveItems(6849, 1);
            } else if (luck < 23) {
                st.rewardItems(57, 65000);
                st.giveItems(6847, 1);
            } else if (luck < 33) {
                st.rewardItems(57, 25000);
                st.giveItems(6851, 1);
            } else {
                st.rewardItems(57, 73000);
                st.rewardExpAndSp(230000L, 18250);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q623_TheFinestFood");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "31521-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31521-06.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7199) >= 100 && st.getQuestItemsCount(7200) >= 100 && st.getQuestItemsCount(7201) >= 100) {
                        htmltext = "31521-04.htm";
                        break;
                    }
                    htmltext = "31521-07.htm";
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 21316:
                if (st.dropItemsAlways(7199, 1, 100) && st.getQuestItemsCount(7200) >= 100 && st.getQuestItemsCount(7201) >= 100)
                    st.set("cond", "2");
                break;
            case 21315:
                if (st.dropItemsAlways(7200, 1, 100) && st.getQuestItemsCount(7199) >= 100 && st.getQuestItemsCount(7201) >= 100)
                    st.set("cond", "2");
                break;
            case 21318:
                if (st.dropItemsAlways(7201, 1, 100) && st.getQuestItemsCount(7199) >= 100 && st.getQuestItemsCount(7200) >= 100)
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
