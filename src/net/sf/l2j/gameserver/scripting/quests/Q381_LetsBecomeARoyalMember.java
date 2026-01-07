package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q381_LetsBecomeARoyalMember extends Quest {
    private static final String qn = "Q381_LetsBecomeARoyalMember";

    private static final int SORINT = 30232;

    private static final int SANDRA = 30090;

    private static final int KAIL_COIN = 5899;

    private static final int COIN_ALBUM = 5900;

    private static final int GOLDEN_CLOVER_COIN = 7569;

    private static final int COIN_COLLECTOR_MEMBERSHIP = 3813;

    private static final int ROYAL_MEMBERSHIP = 5898;

    public Q381_LetsBecomeARoyalMember() {
        super(381, "Lets Become a Royal Member!");
        setItemsIds(5899, 7569);
        addStartNpc(30232);
        addTalkId(30232, 30090);
        addKillId(21018, 27316);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q381_LetsBecomeARoyalMember");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30090-02.htm")) {
            st.set("aCond", "1");
        } else if (event.equalsIgnoreCase("30232-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q381_LetsBecomeARoyalMember");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 55 || !st.hasQuestItems(3813)) ? "30232-02.htm" : "30232-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30232:
                        if (!st.hasQuestItems(5899)) {
                            htmltext = "30232-04.htm";
                            break;
                        }
                        if (!st.hasQuestItems(5900)) {
                            htmltext = "30232-05.htm";
                            break;
                        }
                        htmltext = "30232-06.htm";
                        st.takeItems(5899, -1);
                        st.takeItems(5900, -1);
                        st.giveItems(5898, 1);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    case 30090:
                        if (!st.hasQuestItems(5900)) {
                            if (st.getInt("aCond") == 0) {
                                htmltext = "30090-01.htm";
                                break;
                            }
                            if (!st.hasQuestItems(7569)) {
                                htmltext = "30090-03.htm";
                                break;
                            }
                            htmltext = "30090-04.htm";
                            st.takeItems(7569, -1);
                            st.giveItems(5900, 1);
                            break;
                        }
                        htmltext = "30090-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (npc.getNpcId() == 21018) {
            st.dropItems(5899, 1, 1, 50000);
        } else if (st.getInt("aCond") == 1) {
            st.dropItemsAlways(7569, 1, 1);
        }
        return null;
    }
}
