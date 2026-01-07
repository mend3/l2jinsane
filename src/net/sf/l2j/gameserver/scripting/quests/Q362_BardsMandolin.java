package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q362_BardsMandolin extends Quest {
    private static final String qn = "Q362_BardsMandolin";

    private static final int SWAN_FLUTE = 4316;

    private static final int SWAN_LETTER = 4317;

    private static final int SWAN = 30957;

    private static final int NANARIN = 30956;

    private static final int GALION = 30958;

    private static final int WOODROW = 30837;

    public Q362_BardsMandolin() {
        super(362, "Bard's Mandolin");
        setItemsIds(4316, 4317);
        addStartNpc(30957);
        addTalkId(30957, 30956, 30958, 30837);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q362_BardsMandolin");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30957-3.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30957-7.htm") || event.equalsIgnoreCase("30957-8.htm")) {
            st.rewardItems(57, 10000);
            st.giveItems(4410, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q362_BardsMandolin");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30957-2.htm" : "30957-1.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30957:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30957-4.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30957-5.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(4317, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30957-5a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30957-6.htm";
                        break;
                    case 30837:
                        if (cond == 1) {
                            htmltext = "30837-1.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30837-2.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30837-3.htm";
                        break;
                    case 30958:
                        if (cond == 2) {
                            htmltext = "30958-1.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(4316, 1);
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30958-2.htm";
                        break;
                    case 30956:
                        if (cond == 4) {
                            htmltext = "30956-1.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4316, 1);
                            st.takeItems(4317, 1);
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30956-2.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
