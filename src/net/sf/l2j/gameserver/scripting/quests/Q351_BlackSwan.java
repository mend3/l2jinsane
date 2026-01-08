package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q351_BlackSwan extends Quest {
    private static final String qn = "Q351_BlackSwan";

    private static final int GOSTA = 30916;

    private static final int IASON_HEINE = 30969;

    private static final int ROMAN = 30897;

    private static final int ORDER_OF_GOSTA = 4296;

    private static final int LIZARD_FANG = 4297;

    private static final int BARREL_OF_LEAGUE = 4298;

    private static final int BILL_OF_IASON_HEINE = 4310;

    public Q351_BlackSwan() {
        super(351, "Black Swan");
        setItemsIds(4296, 4298, 4297);
        addStartNpc(30916);
        addTalkId(30916, 30969, 30897);
        addKillId(20784, 20785, 21639, 21640);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q351_BlackSwan");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30916-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(4296, 1);
        } else if (event.equalsIgnoreCase("30969-02a.htm")) {
            int lizardFangs = st.getQuestItemsCount(4297);
            if (lizardFangs > 0) {
                htmltext = "30969-02.htm";
                st.takeItems(4297, -1);
                st.rewardItems(57, lizardFangs * 20);
            }
        } else if (event.equalsIgnoreCase("30969-03a.htm")) {
            int barrels = st.getQuestItemsCount(4298);
            if (barrels > 0) {
                htmltext = "30969-03.htm";
                st.takeItems(4298, -1);
                st.rewardItems(4310, barrels);
                if (st.getInt("cond") == 1) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                }
            }
        } else if (event.equalsIgnoreCase("30969-06.htm")) {
            if (!st.hasQuestItems(4298, 4297)) {
                htmltext = "30969-07.htm";
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q351_BlackSwan");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 32) ? "30916-00.htm" : "30916-01.htm";
                break;
            case 1:
                htmltext = switch (npc.getNpcId()) {
                    case 30916 -> "30916-04.htm";
                    case 30969 -> "30969-01.htm";
                    case 30897 -> st.hasQuestItems(4310) ? "30897-01.htm" : "30897-02.htm";
                    default -> htmltext;
                };
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int random = Rnd.get(4);
        if (random < 3) {
            st.dropItemsAlways(4297, (random < 2) ? 1 : 2, 0);
            st.dropItems(4298, 1, 0, 50000);
        } else {
            st.dropItems(4298, 1, 0, (npc.getNpcId() > 20785) ? 30000 : 40000);
        }
        return null;
    }
}
