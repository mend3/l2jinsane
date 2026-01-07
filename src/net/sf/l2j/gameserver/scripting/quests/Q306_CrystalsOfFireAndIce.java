package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q306_CrystalsOfFireAndIce extends Quest {
    private static final String qn = "Q306_CrystalsOfFireAndIce";

    private static final int FLAME_SHARD = 1020;

    private static final int ICE_SHARD = 1021;

    private static final int[][] DROPLIST = new int[][]{{20109, 1020, 300000}, {20110, 1021, 300000}, {20112, 1020, 400000}, {20113, 1021, 400000}, {20114, 1020, 500000}, {20115, 1021, 500000}};

    public Q306_CrystalsOfFireAndIce() {
        super(306, "Crystals of Fire and Ice");
        setItemsIds(1020, 1021);
        addStartNpc(30004);
        addTalkId(30004);
        addKillId(20109, 20110, 20112, 20113, 20114, 20115);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q306_CrystalsOfFireAndIce");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30004-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30004-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int totalItems;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q306_CrystalsOfFireAndIce");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 17) ? "30004-01.htm" : "30004-02.htm";
                break;
            case 1:
                totalItems = st.getQuestItemsCount(1020) + st.getQuestItemsCount(1021);
                if (totalItems == 0) {
                    htmltext = "30004-04.htm";
                    break;
                }
                htmltext = "30004-05.htm";
                st.takeItems(1020, -1);
                st.takeItems(1021, -1);
                st.rewardItems(57, 30 * totalItems + ((totalItems > 10) ? 5000 : 0));
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        for (int[] drop : DROPLIST) {
            if (npc.getNpcId() == drop[0]) {
                st.dropItems(drop[1], 1, 0, drop[2]);
                break;
            }
        }
        return null;
    }
}
