package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q170_DangerousSeduction extends Quest {
    private static final String qn = "Q170_DangerousSeduction";

    private static final int NIGHTMARE_CRYSTAL = 1046;

    public Q170_DangerousSeduction() {
        super(170, "Dangerous Seduction");
        setItemsIds(1046);
        addStartNpc(30305);
        addTalkId(30305);
        addKillId(27022);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q170_DangerousSeduction");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30305-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q170_DangerousSeduction");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30305-00.htm";
                    break;
                }
                if (player.getLevel() < 21) {
                    htmltext = "30305-02.htm";
                    break;
                }
                htmltext = "30305-03.htm";
                break;
            case 1:
                if (st.hasQuestItems(1046)) {
                    htmltext = "30305-06.htm";
                    st.takeItems(1046, -1);
                    st.rewardItems(57, 102680);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                    break;
                }
                htmltext = "30305-05.htm";
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        st.set("cond", "2");
        st.playSound("ItemSound.quest_middle");
        st.giveItems(1046, 1);
        return null;
    }
}
