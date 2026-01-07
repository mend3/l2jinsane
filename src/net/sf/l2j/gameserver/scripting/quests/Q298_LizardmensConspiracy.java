package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q298_LizardmensConspiracy extends Quest {
    private static final String qn = "Q298_LizardmensConspiracy";

    private static final int PRAGA = 30333;

    private static final int ROHMER = 30344;

    private static final int PATROL_REPORT = 7182;

    private static final int WHITE_GEM = 7183;

    private static final int RED_GEM = 7184;

    public Q298_LizardmensConspiracy() {
        super(298, "Lizardmen's Conspiracy");
        setItemsIds(7182, 7183, 7184);
        addStartNpc(30333);
        addTalkId(30333, 30344);
        addKillId(20926, 20927, 20922, 20923, 20924);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q298_LizardmensConspiracy");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30333-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7182, 1);
        } else if (event.equalsIgnoreCase("30344-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7182, 1);
        } else if (event.equalsIgnoreCase("30344-4.htm")) {
            if (st.getInt("cond") == 3) {
                htmltext = "30344-3.htm";
                st.takeItems(7183, -1);
                st.takeItems(7184, -1);
                st.rewardExpAndSp(0L, 42000);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q298_LizardmensConspiracy");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 25) ? "30333-0b.htm" : "30333-0a.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30333:
                        htmltext = "30333-2.htm";
                        break;
                    case 30344:
                        if (st.getInt("cond") == 1) {
                            htmltext = st.hasQuestItems(7182) ? "30344-0.htm" : "30344-0a.htm";
                            break;
                        }
                        htmltext = "30344-2.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "2");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20922:
                if (st.dropItems(7183, 1, 50, 400000) && st.getQuestItemsCount(7184) >= 50)
                    st.set("cond", "3");
                break;
            case 20923:
                if (st.dropItems(7183, 1, 50, 450000) && st.getQuestItemsCount(7184) >= 50)
                    st.set("cond", "3");
                break;
            case 20924:
                if (st.dropItems(7183, 1, 50, 350000) && st.getQuestItemsCount(7184) >= 50)
                    st.set("cond", "3");
                break;
            case 20926:
            case 20927:
                if (st.dropItems(7184, 1, 50, 400000) && st.getQuestItemsCount(7183) >= 50)
                    st.set("cond", "3");
                break;
        }
        return null;
    }
}
