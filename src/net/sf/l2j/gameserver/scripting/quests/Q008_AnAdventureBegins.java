package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q008_AnAdventureBegins extends Quest {
    private static final String qn = "Q008_AnAdventureBegins";

    private static final int JASMINE = 30134;

    private static final int ROSELYN = 30355;

    private static final int HARNE = 30144;

    private static final int ROSELYN_NOTE = 7573;

    private static final int SOE_GIRAN = 7559;

    private static final int MARK_TRAVELER = 7570;

    public Q008_AnAdventureBegins() {
        super(8, "An Adventure Begins");
        setItemsIds(7573);
        addStartNpc(30134);
        addTalkId(30134, 30355, 30144);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q008_AnAdventureBegins");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30134-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30355-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7573, 1);
        } else if (event.equalsIgnoreCase("30144-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7573, 1);
        } else if (event.equalsIgnoreCase("30134-06.htm")) {
            st.giveItems(7570, 1);
            st.rewardItems(7559, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q008_AnAdventureBegins");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 3 && player.getRace() == ClassRace.DARK_ELF) {
                    htmltext = "30134-02.htm";
                    break;
                }
                htmltext = "30134-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30134:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30134-04.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30134-05.htm";
                        break;
                    case 30355:
                        if (cond == 1) {
                            htmltext = "30355-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30355-03.htm";
                        break;
                    case 30144:
                        if (cond == 2) {
                            htmltext = "30144-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30144-03.htm";
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
