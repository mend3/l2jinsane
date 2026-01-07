package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q618_IntoTheFlame extends Quest {
    private static final String qn = "Q618_IntoTheFlame";

    private static final int KLEIN = 31540;

    private static final int HILDA = 31271;

    private static final int VACUALITE_ORE = 7265;

    private static final int VACUALITE = 7266;

    private static final int FLOATING_STONE = 7267;

    public Q618_IntoTheFlame() {
        super(618, "Into The Flame");
        setItemsIds(7265, 7266);
        addStartNpc(31540);
        addTalkId(31540, 31271);
        addKillId(21274, 21275, 21276, 21277, 21282, 21283, 21284, 21285, 21290, 21291,
                21292, 21293);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q618_IntoTheFlame");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31540-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31540-05.htm")) {
            st.takeItems(7266, 1);
            st.giveItems(7267, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("31271-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31271-05.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7265, -1);
            st.giveItems(7266, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q618_IntoTheFlame");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "31540-01.htm" : "31540-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31540:
                        htmltext = (cond == 4) ? "31540-04.htm" : "31540-03.htm";
                        break;
                    case 31271:
                        if (cond == 1) {
                            htmltext = "31271-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31271-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "31271-04.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "31271-06.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "2");
        if (st == null)
            return null;
        if (st.dropItems(7265, 1, 50, 500000))
            st.set("cond", "3");
        return null;
    }
}
