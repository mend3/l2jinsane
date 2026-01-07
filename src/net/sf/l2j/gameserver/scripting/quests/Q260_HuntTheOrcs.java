package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q260_HuntTheOrcs extends Quest {
    private static final String qn = "Q260_HuntTheOrcs";

    private static final int RAYEN = 30221;

    private static final int ORC_AMULET = 1114;

    private static final int ORC_NECKLACE = 1115;

    private static final int KABOO_ORC = 20468;

    private static final int KABOO_ORC_ARCHER = 20469;

    private static final int KABOO_ORC_GRUNT = 20470;

    private static final int KABOO_ORC_FIGHTER = 20471;

    private static final int KABOO_ORC_FIGHTER_LEADER = 20472;

    private static final int KABOO_ORC_FIGHTER_LIEUTENANT = 20473;

    public Q260_HuntTheOrcs() {
        super(260, "Hunt the Orcs");
        setItemsIds(1114, 1115);
        addStartNpc(30221);
        addTalkId(30221);
        addKillId(20468, 20469, 20470, 20471, 20472, 20473);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q260_HuntTheOrcs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30221-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30221-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int amulet, necklace;
        QuestState st = player.getQuestState("Q260_HuntTheOrcs");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30221-00.htm";
                    break;
                }
                if (player.getLevel() < 6) {
                    htmltext = "30221-01.htm";
                    break;
                }
                htmltext = "30221-02.htm";
                break;
            case 1:
                amulet = st.getQuestItemsCount(1114);
                necklace = st.getQuestItemsCount(1115);
                if (amulet == 0 && necklace == 0) {
                    htmltext = "30221-04.htm";
                    break;
                }
                htmltext = "30221-05.htm";
                st.takeItems(1114, -1);
                st.takeItems(1115, -1);
                st.rewardItems(57, amulet * 5 + necklace * 15);
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
            case 20468:
            case 20469:
            case 20470:
                st.dropItems(1114, 1, 0, 500000);
                break;
            case 20471:
            case 20472:
            case 20473:
                st.dropItems(1115, 1, 0, 500000);
                break;
        }
        return null;
    }
}
