package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q258_BringWolfPelts extends Quest {
    private static final String qn = "Q258_BringWolfPelts";

    private static final int WOLF_PELT = 702;

    private static final int COTTON_SHIRT = 390;

    private static final int LEATHER_PANTS = 29;

    private static final int LEATHER_SHIRT = 22;

    private static final int SHORT_LEATHER_GLOVES = 1119;

    private static final int TUNIC = 426;

    public Q258_BringWolfPelts() {
        super(258, "Bring Wolf Pelts");
        setItemsIds(702);
        addStartNpc(30001);
        addTalkId(30001);
        addKillId(20120, 20442);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q258_BringWolfPelts");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30001-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int randomNumber;
        QuestState st = player.getQuestState("Q258_BringWolfPelts");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 3) ? "30001-01.htm" : "30001-02.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(702) < 40) {
                    htmltext = "30001-05.htm";
                    break;
                }
                st.takeItems(702, -1);
                randomNumber = Rnd.get(16);
                if (randomNumber == 0) {
                    st.giveItems(390, 1);
                } else if (randomNumber < 6) {
                    st.giveItems(29, 1);
                } else if (randomNumber < 9) {
                    st.giveItems(22, 1);
                } else if (randomNumber < 13) {
                    st.giveItems(1119, 1);
                } else {
                    st.giveItems(426, 1);
                }
                htmltext = "30001-06.htm";
                if (randomNumber == 0) {
                    st.playSound("ItemSound.quest_jackpot");
                } else {
                    st.playSound("ItemSound.quest_finish");
                }
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
        if (st.dropItemsAlways(702, 1, 40))
            st.set("cond", "2");
        return null;
    }
}
