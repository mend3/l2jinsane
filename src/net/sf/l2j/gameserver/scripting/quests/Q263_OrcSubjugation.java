package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q263_OrcSubjugation extends Quest {
    private static final String qn = "Q263_OrcSubjugation";

    private static final int ORC_AMULET = 1116;

    private static final int ORC_NECKLACE = 1117;

    public Q263_OrcSubjugation() {
        super(263, "Orc Subjugation");
        setItemsIds(1116, 1117);
        addStartNpc(30346);
        addTalkId(30346);
        addKillId(20385, 20386, 20387, 20388);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q263_OrcSubjugation");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30346-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30346-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int amulet, necklace;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q263_OrcSubjugation");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30346-00.htm";
                    break;
                }
                if (player.getLevel() < 8) {
                    htmltext = "30346-01.htm";
                    break;
                }
                htmltext = "30346-02.htm";
                break;
            case 1:
                amulet = st.getQuestItemsCount(1116);
                necklace = st.getQuestItemsCount(1117);
                if (amulet == 0 && necklace == 0) {
                    htmltext = "30346-04.htm";
                    break;
                }
                htmltext = "30346-05.htm";
                st.takeItems(1116, -1);
                st.takeItems(1117, -1);
                st.rewardItems(57, amulet * 20 + necklace * 30);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems((npc.getNpcId() == 20385) ? 1116 : 1117, 1, 0, 500000);
        return null;
    }
}
