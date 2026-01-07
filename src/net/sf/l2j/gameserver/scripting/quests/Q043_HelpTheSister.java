package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q043_HelpTheSister extends Quest {
    private static final String qn = "Q043_HelpTheSister";

    private static final int COOPER = 30829;

    private static final int GALLADUCCI = 30097;

    private static final int CRAFTED_DAGGER = 220;

    private static final int MAP_PIECE = 7550;

    private static final int MAP = 7551;

    private static final int PET_TICKET = 7584;

    private static final int SPECTER = 20171;

    private static final int SORROW_MAIDEN = 20197;

    public Q043_HelpTheSister() {
        super(43, "Help the Sister!");
        setItemsIds(7550, 7551);
        addStartNpc(30829);
        addTalkId(30829, 30097);
        addKillId(20171, 20197);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q043_HelpTheSister");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30829-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30829-03.htm") && st.hasQuestItems(220)) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(220, 1);
        } else if (event.equalsIgnoreCase("30829-05.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7550, 30);
            st.giveItems(7551, 1);
        } else if (event.equalsIgnoreCase("30097-06.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7551, 1);
        } else if (event.equalsIgnoreCase("30829-07.htm")) {
            st.giveItems(7584, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q043_HelpTheSister");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 26) ? "30829-00a.htm" : "30829-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30829:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(220) ? "30829-01a.htm" : "30829-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30829-03a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30829-04.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30829-05a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30829-06.htm";
                        break;
                    case 30097:
                        if (cond == 4) {
                            htmltext = "30097-05.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30097-06a.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "2");
        if (st == null)
            return null;
        if (st.dropItemsAlways(7550, 1, 30))
            st.set("cond", "3");
        return null;
    }
}
