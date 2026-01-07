package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q042_HelpTheUncle extends Quest {
    private static final String qn = "Q042_HelpTheUncle";

    private static final int WATERS = 30828;

    private static final int SOPHYA = 30735;

    private static final int TRIDENT = 291;

    private static final int MAP_PIECE = 7548;

    private static final int MAP = 7549;

    private static final int PET_TICKET = 7583;

    private static final int MONSTER_EYE_DESTROYER = 20068;

    private static final int MONSTER_EYE_GAZER = 20266;

    public Q042_HelpTheUncle() {
        super(42, "Help the Uncle!");
        setItemsIds(7548, 7549);
        addStartNpc(30828);
        addTalkId(30828, 30735);
        addKillId(20068, 20266);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q042_HelpTheUncle");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30828-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30828-03.htm") && st.hasQuestItems(291)) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(291, 1);
        } else if (event.equalsIgnoreCase("30828-05.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7548, 30);
            st.giveItems(7549, 1);
        } else if (event.equalsIgnoreCase("30735-06.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7549, 1);
        } else if (event.equalsIgnoreCase("30828-07.htm")) {
            st.giveItems(7583, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q042_HelpTheUncle");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 25) ? "30828-00a.htm" : "30828-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30828:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(291) ? "30828-01a.htm" : "30828-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30828-03a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30828-04.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30828-05a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30828-06.htm";
                        break;
                    case 30735:
                        if (cond == 4) {
                            htmltext = "30735-05.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30735-06a.htm";
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
        if (st.dropItemsAlways(7548, 1, 30))
            st.set("cond", "3");
        return null;
    }
}
