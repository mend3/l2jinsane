package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q619_RelicsOfTheOldEmpire extends Quest {
    private static final String qn = "Q619_RelicsOfTheOldEmpire";

    private static final int GHOST_OF_ADVENTURER = 31538;

    private static final int RELICS = 7254;

    private static final int ENTRANCE = 7075;

    private static final int[] REWARDS = new int[]{6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

    public Q619_RelicsOfTheOldEmpire() {
        super(619, "Relics of the Old Empire");
        setItemsIds(7254);
        addStartNpc(31538);
        addTalkId(31538);
        int id;
        for (id = 21396; id <= 21434; id++) {
            addKillId(id);
        }
        addKillId(21798, 21799, 21800);
        for (id = 18120; id <= 18256; id++) {
            addKillId(id);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q619_RelicsOfTheOldEmpire");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31538-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31538-09.htm")) {
            if (st.getQuestItemsCount(7254) >= 1000) {
                htmltext = "31538-09.htm";
                st.takeItems(7254, 1000);
                st.giveItems(Rnd.get(REWARDS), 1);
            } else {
                htmltext = "31538-06.htm";
            }
        } else if (event.equalsIgnoreCase("31538-10.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q619_RelicsOfTheOldEmpire");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 74) ? "31538-02.htm" : "31538-01.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(7254) >= 1000) {
                    htmltext = "31538-04.htm";
                    break;
                }
                if (st.hasQuestItems(7075)) {
                    htmltext = "31538-06.htm";
                    break;
                }
                htmltext = "31538-07.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItemsAlways(7254, 1, 0);
        st.dropItems(7075, 1, 0, 50000);
        return null;
    }
}
