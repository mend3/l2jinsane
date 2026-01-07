package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q167_DwarvenKinship extends Quest {
    private static final String qn = "Q167_DwarvenKinship";

    private static final int CARLON_LETTER = 1076;

    private static final int NORMAN_LETTER = 1106;

    private static final int CARLON = 30350;

    private static final int NORMAN = 30210;

    private static final int HAPROCK = 30255;

    public Q167_DwarvenKinship() {
        super(167, "Dwarven Kinship");
        setItemsIds(1076, 1106);
        addStartNpc(30350);
        addTalkId(30350, 30255, 30210);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q167_DwarvenKinship");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30350-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1076, 1);
        } else if (event.equalsIgnoreCase("30255-03.htm")) {
            st.set("cond", "2");
            st.takeItems(1076, 1);
            st.giveItems(1106, 1);
            st.rewardItems(57, 2000);
        } else if (event.equalsIgnoreCase("30255-04.htm")) {
            st.takeItems(1076, 1);
            st.rewardItems(57, 3000);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30210-02.htm")) {
            st.takeItems(1106, 1);
            st.rewardItems(57, 20000);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q167_DwarvenKinship");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30350-02.htm" : "30350-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30350:
                        if (cond == 1)
                            htmltext = "30350-05.htm";
                        break;
                    case 30255:
                        if (cond == 1) {
                            htmltext = "30255-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30255-05.htm";
                        break;
                    case 30210:
                        if (cond == 2)
                            htmltext = "30210-01.htm";
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
