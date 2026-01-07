package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q007_ATripBegins extends Quest {
    private static final String qn = "Q007_ATripBegins";

    private static final int MIRABEL = 30146;

    private static final int ARIEL = 30148;

    private static final int ASTERIOS = 30154;

    private static final int ARIEL_RECO = 7572;

    private static final int MARK_TRAVELER = 7570;

    private static final int SOE_GIRAN = 7559;

    public Q007_ATripBegins() {
        super(7, "A Trip Begins");
        setItemsIds(7572);
        addStartNpc(30146);
        addTalkId(30146, 30148, 30154);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q007_ATripBegins");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30146-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30148-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7572, 1);
        } else if (event.equalsIgnoreCase("30154-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7572, 1);
        } else if (event.equalsIgnoreCase("30146-06.htm")) {
            st.giveItems(7570, 1);
            st.rewardItems(7559, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q007_ATripBegins");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30146-01.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "30146-01a.htm";
                    break;
                }
                htmltext = "30146-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30146:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30146-04.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30146-05.htm";
                        break;
                    case 30148:
                        if (cond == 1) {
                            htmltext = "30148-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30148-03.htm";
                        break;
                    case 30154:
                        if (cond == 2) {
                            htmltext = "30154-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30154-03.htm";
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
