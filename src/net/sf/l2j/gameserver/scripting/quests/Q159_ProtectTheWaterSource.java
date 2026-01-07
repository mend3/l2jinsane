package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q159_ProtectTheWaterSource extends Quest {
    private static final String qn = "Q159_ProtectTheWaterSource";

    private static final int PLAGUE_DUST = 1035;

    private static final int HYACINTH_CHARM_1 = 1071;

    private static final int HYACINTH_CHARM_2 = 1072;

    public Q159_ProtectTheWaterSource() {
        super(159, "Protect the Water Source");
        setItemsIds(1035, 1071, 1072);
        addStartNpc(30154);
        addTalkId(30154);
        addKillId(27017);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q159_ProtectTheWaterSource");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30154-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1071, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q159_ProtectTheWaterSource");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30154-00.htm";
                    break;
                }
                if (player.getLevel() < 12) {
                    htmltext = "30154-02.htm";
                    break;
                }
                htmltext = "30154-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30154-05.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30154-06.htm";
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1035, -1);
                    st.takeItems(1071, 1);
                    st.giveItems(1072, 1);
                    break;
                }
                if (cond == 3) {
                    htmltext = "30154-07.htm";
                    break;
                }
                if (cond == 4) {
                    htmltext = "30154-08.htm";
                    st.takeItems(1072, 1);
                    st.takeItems(1035, -1);
                    st.rewardItems(57, 18250);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (st.getInt("cond") == 1 && st.dropItems(1035, 1, 1, 400000)) {
            st.set("cond", "2");
        } else if (st.getInt("cond") == 3 && st.dropItems(1035, 1, 5, 400000)) {
            st.set("cond", "4");
        }
        return null;
    }
}
