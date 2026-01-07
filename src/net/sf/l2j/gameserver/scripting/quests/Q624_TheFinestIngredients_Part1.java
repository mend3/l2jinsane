package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q624_TheFinestIngredients_Part1 extends Quest {
    private static final String qn = "Q624_TheFinestIngredients_Part1";

    private static final int NEPENTHES = 21319;

    private static final int ATROX = 21321;

    private static final int ATROXSPAWN = 21317;

    private static final int BANDERSNATCH = 21314;

    private static final int TRUNK_OF_NEPENTHES = 7202;

    private static final int FOOT_OF_BANDERSNATCHLING = 7203;

    private static final int SECRET_SPICE = 7204;

    private static final int ICE_CRYSTAL = 7080;

    private static final int SOY_SAUCE_JAR = 7205;

    public Q624_TheFinestIngredients_Part1() {
        super(624, "The Finest Ingredients - Part 1");
        setItemsIds(7202, 7203, 7204);
        addStartNpc(31521);
        addTalkId(31521);
        addKillId(21319, 21321, 21317, 21314);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q624_TheFinestIngredients_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31521-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31521-05.htm")) {
            if (st.getQuestItemsCount(7202) >= 50 && st.getQuestItemsCount(7203) >= 50 && st.getQuestItemsCount(7204) >= 50) {
                st.takeItems(7202, -1);
                st.takeItems(7203, -1);
                st.takeItems(7204, -1);
                st.giveItems(7080, 1);
                st.giveItems(7205, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                st.set("cond", "1");
                htmltext = "31521-07.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q624_TheFinestIngredients_Part1");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31521-03.htm" : "31521-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31521-06.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7202) >= 50 && st.getQuestItemsCount(7203) >= 50 && st.getQuestItemsCount(7204) >= 50) {
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
            case 21319:
                if (st.dropItemsAlways(7202, 1, 50) && st.getQuestItemsCount(7203) >= 50 && st.getQuestItemsCount(7204) >= 50)
                    st.set("cond", "2");
                break;
            case 21317:
            case 21321:
                if (st.dropItemsAlways(7204, 1, 50) && st.getQuestItemsCount(7202) >= 50 && st.getQuestItemsCount(7203) >= 50)
                    st.set("cond", "2");
                break;
            case 21314:
                if (st.dropItemsAlways(7203, 1, 50) && st.getQuestItemsCount(7202) >= 50 && st.getQuestItemsCount(7204) >= 50)
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
