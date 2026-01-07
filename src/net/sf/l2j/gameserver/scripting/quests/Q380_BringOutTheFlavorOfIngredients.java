package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q380_BringOutTheFlavorOfIngredients extends Quest {
    private static final String qn = "Q380_BringOutTheFlavorOfIngredients";

    private static final int DIRE_WOLF = 20205;

    private static final int KADIF_WEREWOLF = 20206;

    private static final int GIANT_MIST_LEECH = 20225;

    private static final int RITRON_FRUIT = 5895;

    private static final int MOON_FACE_FLOWER = 5896;

    private static final int LEECH_FLUIDS = 5897;

    private static final int ANTIDOTE = 1831;

    private static final int RITRON_JELLY = 5960;

    private static final int JELLY_RECIPE = 5959;

    public Q380_BringOutTheFlavorOfIngredients() {
        super(380, "Bring Out the Flavor of Ingredients!");
        setItemsIds(5895, 5896, 5897);
        addStartNpc(30069);
        addTalkId(30069);
        addKillId(20205, 20206, 20225);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q380_BringOutTheFlavorOfIngredients");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30069-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30069-12.htm")) {
            st.giveItems(5959, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q380_BringOutTheFlavorOfIngredients");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 24) ? "30069-00.htm" : "30069-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30069-06.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(1831) >= 2) {
                        htmltext = "30069-07.htm";
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(5895, -1);
                        st.takeItems(5896, -1);
                        st.takeItems(5897, -1);
                        st.takeItems(1831, 2);
                        break;
                    }
                    htmltext = "30069-06.htm";
                    break;
                }
                if (cond == 3) {
                    htmltext = "30069-08.htm";
                    st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                if (cond == 4) {
                    htmltext = "30069-09.htm";
                    st.set("cond", "5");
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                if (cond == 5) {
                    htmltext = "30069-10.htm";
                    st.set("cond", "6");
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                if (cond == 6) {
                    st.giveItems(5960, 1);
                    if (Rnd.get(100) < 55) {
                        htmltext = "30069-11.htm";
                        break;
                    }
                    htmltext = "30069-13.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(true);
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20205:
                if (st.dropItems(5895, 1, 4, 100000) &&
                        st.getQuestItemsCount(5896) == 20 && st.getQuestItemsCount(5897) == 10)
                    st.set("cond", "2");
                break;
            case 20206:
                if (st.dropItems(5896, 1, 20, 500000) &&
                        st.getQuestItemsCount(5895) == 4 && st.getQuestItemsCount(5897) == 10)
                    st.set("cond", "2");
                break;
            case 20225:
                if (st.dropItems(5897, 1, 10, 500000) &&
                        st.getQuestItemsCount(5895) == 4 && st.getQuestItemsCount(5896) == 20)
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
