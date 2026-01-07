package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q044_HelpTheSon extends Quest {
    private static final String qn = "Q044_HelpTheSon";

    private static final int LUNDY = 30827;

    private static final int DRIKUS = 30505;

    private static final int WORK_HAMMER = 168;

    private static final int GEMSTONE_FRAGMENT = 7552;

    private static final int GEMSTONE = 7553;

    private static final int PET_TICKET = 7585;

    private static final int MAILLE = 20919;

    private static final int MAILLE_SCOUT = 20920;

    private static final int MAILLE_GUARD = 20921;

    public Q044_HelpTheSon() {
        super(44, "Help the Son!");
        setItemsIds(7552, 7553);
        addStartNpc(30827);
        addTalkId(30827, 30505);
        addKillId(20919, 20920, 20921);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q044_HelpTheSon");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30827-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30827-03.htm") && st.hasQuestItems(168)) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(168, 1);
        } else if (event.equalsIgnoreCase("30827-05.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7552, 30);
            st.giveItems(7553, 1);
        } else if (event.equalsIgnoreCase("30505-06.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7553, 1);
        } else if (event.equalsIgnoreCase("30827-07.htm")) {
            st.giveItems(7585, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q044_HelpTheSon");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 24) ? "30827-00a.htm" : "30827-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30827:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(168) ? "30827-01a.htm" : "30827-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30827-03a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30827-04.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30827-05a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30827-06.htm";
                        break;
                    case 30505:
                        if (cond == 4) {
                            htmltext = "30505-05.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30505-06a.htm";
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
        if (st.dropItemsAlways(7552, 1, 30))
            st.set("cond", "3");
        return null;
    }
}
