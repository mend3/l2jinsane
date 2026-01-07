package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q009_IntoTheCityOfHumans extends Quest {
    private static final String qn = "Q009_IntoTheCityOfHumans";

    public final int PETUKAI = 30583;

    public final int TANAPI = 30571;

    public final int TAMIL = 30576;

    public final int MARK_OF_TRAVELER = 7570;

    public final int SOE_GIRAN = 7126;

    public Q009_IntoTheCityOfHumans() {
        super(9, "Into the City of Humans");
        addStartNpc(30583);
        addTalkId(30583, 30571, 30576);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q009_IntoTheCityOfHumans");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30583-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30571-01.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30576-01.htm")) {
            st.giveItems(7570, 1);
            st.rewardItems(7126, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q009_IntoTheCityOfHumans");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 3 && player.getRace() == ClassRace.ORC) {
                    htmltext = "30583-00.htm";
                    break;
                }
                htmltext = "30583-00a.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30583:
                        if (cond == 1)
                            htmltext = "30583-01a.htm";
                        break;
                    case 30571:
                        if (cond == 1) {
                            htmltext = "30571-00.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30571-01a.htm";
                        break;
                    case 30576:
                        if (cond == 2)
                            htmltext = "30576-00.htm";
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
