package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q006_StepIntoTheFuture extends Quest {
    private static final String qn = "Q006_StepIntoTheFuture";

    private static final int ROXXY = 30006;

    private static final int BAULRO = 30033;

    private static final int SIR_COLLIN = 30311;

    private static final int BAULRO_LETTER = 7571;

    private static final int MARK_TRAVELER = 7570;

    private static final int SOE_GIRAN = 7559;

    public Q006_StepIntoTheFuture() {
        super(6, "Step into the Future");
        setItemsIds(7571);
        addStartNpc(30006);
        addTalkId(30006, 30033, 30311);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q006_StepIntoTheFuture");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30006-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30033-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7571, 1);
        } else if (event.equalsIgnoreCase("30311-02.htm")) {
            if (st.hasQuestItems(7571)) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7571, 1);
            } else {
                htmltext = "30311-03.htm";
            }
        } else if (event.equalsIgnoreCase("30006-06.htm")) {
            st.giveItems(7570, 1);
            st.rewardItems(7559, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q006_StepIntoTheFuture");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.HUMAN || player.getLevel() < 3) {
                    htmltext = "30006-01.htm";
                    break;
                }
                htmltext = "30006-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30006:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30006-04.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30006-05.htm";
                        break;
                    case 30033:
                        if (cond == 1) {
                            htmltext = "30033-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30033-03.htm";
                            break;
                        }
                        htmltext = "30033-04.htm";
                        break;
                    case 30311:
                        if (cond == 2) {
                            htmltext = "30311-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30311-03a.htm";
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
