package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q331_ArrowOfVengeance extends Quest {
    private static final String qn = "Q331_ArrowOfVengeance";

    private static final int HARPY_FEATHER = 1452;

    private static final int MEDUSA_VENOM = 1453;

    private static final int WYRM_TOOTH = 1454;

    public Q331_ArrowOfVengeance() {
        super(331, "Arrow of Vengeance");
        setItemsIds(1452, 1453, 1454);
        addStartNpc(30125);
        addTalkId(30125);
        addKillId(20145, 20158, 20176);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q331_ArrowOfVengeance");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30125-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30125-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int harpyFeather, medusaVenom, wyrmTooth;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q331_ArrowOfVengeance");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 32) ? "30125-01.htm" : "30125-02.htm";
                break;
            case 1:
                harpyFeather = st.getQuestItemsCount(1452);
                medusaVenom = st.getQuestItemsCount(1453);
                wyrmTooth = st.getQuestItemsCount(1454);
                if (harpyFeather + medusaVenom + wyrmTooth > 0) {
                    htmltext = "30125-05.htm";
                    st.takeItems(1452, -1);
                    st.takeItems(1453, -1);
                    st.takeItems(1454, -1);
                    int reward = harpyFeather * 78 + medusaVenom * 88 + wyrmTooth * 92;
                    if (harpyFeather + medusaVenom + wyrmTooth > 10)
                        reward += 3100;
                    st.rewardItems(57, reward);
                    break;
                }
                htmltext = "30125-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20145:
                st.dropItems(1452, 1, 0, 500000);
                break;
            case 20158:
                st.dropItems(1453, 1, 0, 500000);
                break;
            case 20176:
                st.dropItems(1454, 1, 0, 500000);
                break;
        }
        return null;
    }
}
