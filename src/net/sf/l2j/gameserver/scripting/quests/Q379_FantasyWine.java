package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q379_FantasyWine extends Quest {
    private static final String qn = "Q379_FantasyWine";

    private static final int HARLAN = 30074;

    private static final int ENKU_CHAMPION = 20291;

    private static final int ENKU_SHAMAN = 20292;

    private static final int LEAF = 5893;

    private static final int STONE = 5894;

    public Q379_FantasyWine() {
        super(379, "Fantasy Wine");
        setItemsIds(5893, 5894);
        addStartNpc(30074);
        addTalkId(30074);
        addKillId(20291, 20292);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q379_FantasyWine");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30074-3.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30074-6.htm")) {
            st.takeItems(5893, 80);
            st.takeItems(5894, 100);
            int rand = Rnd.get(10);
            if (rand < 3) {
                htmltext = "30074-6.htm";
                st.giveItems(5956, 1);
            } else if (rand < 9) {
                htmltext = "30074-7.htm";
                st.giveItems(5957, 1);
            } else {
                htmltext = "30074-8.htm";
                st.giveItems(5958, 1);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30074-2a.htm")) {
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int leaf, stone;
        QuestState st = player.getQuestState("Q379_FantasyWine");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "30074-0a.htm" : "30074-0.htm";
                break;
            case 1:
                leaf = st.getQuestItemsCount(5893);
                stone = st.getQuestItemsCount(5894);
                if (leaf == 80 && stone == 100) {
                    htmltext = "30074-5.htm";
                    break;
                }
                if (leaf == 80) {
                    htmltext = "30074-4a.htm";
                    break;
                }
                if (stone == 100) {
                    htmltext = "30074-4b.htm";
                    break;
                }
                htmltext = "30074-4.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (npc.getNpcId() == 20291) {
            if (st.dropItemsAlways(5893, 1, 80) && st.getQuestItemsCount(5894) >= 100)
                st.set("cond", "2");
        } else if (st.dropItemsAlways(5894, 1, 100) && st.getQuestItemsCount(5893) >= 80) {
            st.set("cond", "2");
        }
        return null;
    }
}
