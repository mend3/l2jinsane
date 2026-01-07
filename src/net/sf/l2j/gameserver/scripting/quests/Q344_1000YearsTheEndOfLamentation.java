package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q344_1000YearsTheEndOfLamentation extends Quest {
    private static final String qn = "Q344_1000YearsTheEndOfLamentation";

    private static final int GILMORE = 30754;

    private static final int RODEMAI = 30756;

    private static final int ORVEN = 30857;

    private static final int KAIEN = 30623;

    private static final int GARVARENTZ = 30704;

    private static final int ARTICLE_DEAD_HERO = 4269;

    private static final int OLD_KEY = 4270;

    private static final int OLD_HILT = 4271;

    private static final int OLD_TOTEM = 4272;

    private static final int CRUCIFIX = 4273;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q344_1000YearsTheEndOfLamentation() {
        super(344, "1000 Years, the End of Lamentation");
        CHANCES.put(20236, 380000);
        CHANCES.put(20237, 490000);
        CHANCES.put(20238, 460000);
        CHANCES.put(20239, 490000);
        CHANCES.put(20240, 530000);
        CHANCES.put(20272, 380000);
        CHANCES.put(20273, 490000);
        CHANCES.put(20274, 460000);
        CHANCES.put(20275, 490000);
        CHANCES.put(20276, 530000);
        setItemsIds(4269, 4270, 4271, 4272, 4273);
        addStartNpc(30754);
        addTalkId(30754, 30756, 30857, 30704, 30623);
        addKillId(20236, 20237, 20238, 20239, 20240, 20272, 20273, 20274, 20275, 20276);
    }

    private static void rewards(QuestState st, int npcId) {
        switch (npcId) {
            case 30857:
                if (st.hasQuestItems(4273)) {
                    st.set("success", "1");
                    st.takeItems(4273, -1);
                    int chance = Rnd.get(100);
                    if (chance < 80) {
                        st.giveItems(1875, 19);
                        break;
                    }
                    if (chance < 95) {
                        st.giveItems(952, 5);
                        break;
                    }
                    st.giveItems(2437, 1);
                }
                break;
            case 30704:
                if (st.hasQuestItems(4272)) {
                    st.set("success", "1");
                    st.takeItems(4272, -1);
                    int chance = Rnd.get(100);
                    if (chance < 55) {
                        st.giveItems(1882, 70);
                        break;
                    }
                    if (chance < 99) {
                        st.giveItems(1881, 50);
                        break;
                    }
                    st.giveItems(191, 1);
                }
                break;
            case 30623:
                if (st.hasQuestItems(4271)) {
                    st.set("success", "1");
                    st.takeItems(4271, -1);
                    int chance = Rnd.get(100);
                    if (chance < 60) {
                        st.giveItems(1874, 25);
                        break;
                    }
                    if (chance < 85) {
                        st.giveItems(1887, 10);
                        break;
                    }
                    if (chance < 99) {
                        st.giveItems(951, 1);
                        break;
                    }
                    st.giveItems(133, 1);
                }
                break;
            case 30756:
                if (st.hasQuestItems(4270)) {
                    st.set("success", "1");
                    st.takeItems(4270, -1);
                    int chance = Rnd.get(100);
                    if (chance < 80) {
                        st.giveItems(1879, 55);
                        break;
                    }
                    if (chance < 95) {
                        st.giveItems(951, 1);
                        break;
                    }
                    st.giveItems(885, 1);
                }
                break;
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q344_1000YearsTheEndOfLamentation");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30754-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30754-07.htm")) {
            if (st.get("success") != null) {
                st.set("cond", "1");
                st.unset("success");
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("30754-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30754-06.htm")) {
            if (!st.hasQuestItems(4269)) {
                htmltext = "30754-06a.htm";
            } else {
                int amount = st.getQuestItemsCount(4269);
                st.takeItems(4269, -1);
                st.giveItems(57, amount * 60);
                if (Rnd.get(1000) < Math.min(10, Math.max(1, amount / 10)))
                    htmltext = "30754-10.htm";
            }
        } else if (event.equalsIgnoreCase("30754-11.htm")) {
            int random = Rnd.get(4);
            if (random < 1) {
                htmltext = "30754-12.htm";
                st.giveItems(4270, 1);
            } else if (random < 2) {
                htmltext = "30754-13.htm";
                st.giveItems(4271, 1);
            } else if (random < 3) {
                htmltext = "30754-14.htm";
                st.giveItems(4272, 1);
            } else {
                st.giveItems(4273, 1);
            }
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q344_1000YearsTheEndOfLamentation");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 48) ? "30754-01.htm" : "30754-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30754:
                        if (cond == 1) {
                            htmltext = st.hasQuestItems(4269) ? "30754-05.htm" : "30754-09.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = (st.get("success") != null) ? "30754-16.htm" : "30754-15.htm";
                        break;
                }
                if (cond == 2) {
                    if (st.get("success") != null) {
                        htmltext = npc.getNpcId() + "-02.htm";
                        break;
                    }
                    rewards(st, npc.getNpcId());
                    htmltext = npc.getNpcId() + "-01.htm";
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
        st.dropItems(4269, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
