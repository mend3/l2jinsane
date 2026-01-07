package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q264_KeenClaws extends Quest {
    private static final String qn = "Q264_KeenClaws";

    private static final int WOLF_CLAW = 1367;

    private static final int LEATHER_SANDALS = 36;

    private static final int WOODEN_HELMET = 43;

    private static final int STOCKINGS = 462;

    private static final int HEALING_POTION = 1061;

    private static final int SHORT_GLOVES = 48;

    private static final int CLOTH_SHOES = 35;

    public Q264_KeenClaws() {
        super(264, "Keen Claws");
        setItemsIds(1367);
        addStartNpc(30136);
        addTalkId(30136);
        addKillId(20003, 20456);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q264_KeenClaws");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30136-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count, n;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q264_KeenClaws");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 3) ? "30136-01.htm" : "30136-02.htm";
                break;
            case 1:
                count = st.getQuestItemsCount(1367);
                if (count < 50) {
                    htmltext = "30136-04.htm";
                    break;
                }
                htmltext = "30136-05.htm";
                st.takeItems(1367, -1);
                n = Rnd.get(17);
                if (n == 0) {
                    st.giveItems(43, 1);
                    st.playSound("ItemSound.quest_jackpot");
                } else if (n < 2) {
                    st.giveItems(57, 1000);
                } else if (n < 5) {
                    st.giveItems(36, 1);
                } else if (n < 8) {
                    st.giveItems(462, 1);
                    st.giveItems(57, 50);
                } else if (n < 11) {
                    st.giveItems(1061, 1);
                } else if (n < 14) {
                    st.giveItems(48, 1);
                } else {
                    st.giveItems(35, 1);
                }
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (npc.getNpcId() == 20003) {
            if (st.dropItems(1367, Rnd.nextBoolean() ? 2 : 4, 50, 500000))
                st.set("cond", "2");
        } else if (st.dropItemsAlways(1367, (Rnd.get(5) < 4) ? 1 : 2, 50)) {
            st.set("cond", "2");
        }
        return null;
    }
}
