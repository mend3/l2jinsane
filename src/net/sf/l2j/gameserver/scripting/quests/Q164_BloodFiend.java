package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q164_BloodFiend extends Quest {
    private static final String qn = "Q164_BloodFiend";

    private static final int KIRUNAK_SKULL = 1044;

    public Q164_BloodFiend() {
        super(164, "Blood Fiend");
        setItemsIds(1044);
        addStartNpc(30149);
        addTalkId(30149);
        addKillId(27021);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q164_BloodFiend");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30149-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q164_BloodFiend");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() == ClassRace.DARK_ELF) {
                    htmltext = "30149-00.htm";
                    break;
                }
                if (player.getLevel() < 21) {
                    htmltext = "30149-02.htm";
                    break;
                }
                htmltext = "30149-03.htm";
                break;
            case 1:
                if (st.hasQuestItems(1044)) {
                    htmltext = "30149-06.htm";
                    st.takeItems(1044, 1);
                    st.rewardItems(57, 42130);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                    break;
                }
                htmltext = "30149-05.htm";
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
        st.giveItems(1044, 1);
        return null;
    }
}
