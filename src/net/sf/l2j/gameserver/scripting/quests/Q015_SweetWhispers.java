package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q015_SweetWhispers extends Quest {
    private static final String qn = "Q015_SweetWhispers";

    private static final int VLADIMIR = 31302;

    private static final int HIERARCH = 31517;

    private static final int MYSTERIOUS_NECRO = 31518;

    public Q015_SweetWhispers() {
        super(15, "Sweet Whispers");
        addStartNpc(31302);
        addTalkId(31302, 31517, 31518);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q015_SweetWhispers");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31302-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31518-01.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31517-01.htm")) {
            st.rewardExpAndSp(60217L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q015_SweetWhispers");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "31302-00a.htm" : "31302-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31302:
                        htmltext = "31302-01a.htm";
                        break;
                    case 31518:
                        if (cond == 1) {
                            htmltext = "31518-00.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31518-01a.htm";
                        break;
                    case 31517:
                        if (cond == 2)
                            htmltext = "31517-00.htm";
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
