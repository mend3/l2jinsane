package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q622_SpecialtyLiquorDelivery extends Quest {
    private static final String qn = "Q622_SpecialtyLiquorDelivery";

    private static final int SPECIAL_DRINK = 7197;

    private static final int FEE_OF_SPECIAL_DRINK = 7198;

    private static final int JEREMY = 31521;

    private static final int PULIN = 31543;

    private static final int NAFF = 31544;

    private static final int CROCUS = 31545;

    private static final int KUBER = 31546;

    private static final int BEOLIN = 31547;

    private static final int LIETTA = 31267;

    private static final int ADENA = 57;

    private static final int HASTE_POTION = 1062;

    private static final int[] REWARDS = new int[]{6847, 6849, 6851};

    public Q622_SpecialtyLiquorDelivery() {
        super(622, "Specialty Liquor Delivery");
        setItemsIds(7197, 7198);
        addStartNpc(31521);
        addTalkId(31521, 31543, 31544, 31545, 31546, 31547, 31267);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q622_SpecialtyLiquorDelivery");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31521-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7197, 5);
        } else if (event.equalsIgnoreCase("31547-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1);
            st.giveItems(7198, 1);
        } else if (event.equalsIgnoreCase("31546-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1);
            st.giveItems(7198, 1);
        } else if (event.equalsIgnoreCase("31545-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1);
            st.giveItems(7198, 1);
        } else if (event.equalsIgnoreCase("31544-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1);
            st.giveItems(7198, 1);
        } else if (event.equalsIgnoreCase("31543-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1);
            st.giveItems(7198, 1);
        } else if (event.equalsIgnoreCase("31521-06.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7198, 5);
        } else if (event.equalsIgnoreCase("31267-02.htm")) {
            if (Rnd.get(5) < 1) {
                st.giveItems(Rnd.get(REWARDS), 1);
            } else {
                st.rewardItems(57, 18800);
                st.rewardItems(1062, 1);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q622_SpecialtyLiquorDelivery");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 68) ? "31521-03.htm" : "31521-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31521:
                        if (cond < 6) {
                            htmltext = "31521-04.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "31521-05.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "31521-06.htm";
                        break;
                    case 31547:
                        if (cond == 1 && st.getQuestItemsCount(7197) == 5) {
                            htmltext = "31547-01.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "31547-03.htm";
                        break;
                    case 31546:
                        if (cond == 2 && st.getQuestItemsCount(7197) == 4) {
                            htmltext = "31546-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "31546-03.htm";
                        break;
                    case 31545:
                        if (cond == 3 && st.getQuestItemsCount(7197) == 3) {
                            htmltext = "31545-01.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31545-03.htm";
                        break;
                    case 31544:
                        if (cond == 4 && st.getQuestItemsCount(7197) == 2) {
                            htmltext = "31544-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "31544-03.htm";
                        break;
                    case 31543:
                        if (cond == 5 && st.getQuestItemsCount(7197) == 1) {
                            htmltext = "31543-01.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "31543-03.htm";
                        break;
                    case 31267:
                        if (cond == 7)
                            htmltext = "31267-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
