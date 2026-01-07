package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q117_TheOceanOfDistantStars extends Quest {
    private static final String qn = "Q117_TheOceanOfDistantStars";

    private static final int ABEY = 32053;

    private static final int GHOST = 32054;

    private static final int ANCIENT_GHOST = 32055;

    private static final int OBI = 32052;

    private static final int BOX = 32076;

    private static final int GREY_STAR = 8495;

    private static final int ENGRAVED_HAMMER = 8488;

    private static final int BANDIT_WARRIOR = 22023;

    private static final int BANDIT_INSPECTOR = 22024;

    public Q117_TheOceanOfDistantStars() {
        super(117, "The Ocean of Distant Stars");
        setItemsIds(8495, 8488);
        addStartNpc(32053);
        addTalkId(32053, 32055, 32054, 32052, 32076);
        addKillId(22023, 22024);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q117_TheOceanOfDistantStars");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32053-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32055-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32052-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32053-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32076-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8488, 1);
        } else if (event.equalsIgnoreCase("32053-06.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32052-04.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32052-06.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8495, 1);
        } else if (event.equalsIgnoreCase("32055-04.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8488, 1);
        } else if (event.equalsIgnoreCase("32054-03.htm")) {
            st.rewardExpAndSp(63591L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q117_TheOceanOfDistantStars");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 39) ? "32053-00.htm" : "32053-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 32055:
                        if (cond == 1) {
                            htmltext = "32055-01.htm";
                            break;
                        }
                        if (cond > 1 && cond < 9) {
                            htmltext = "32055-02.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "32055-03.htm";
                            break;
                        }
                        if (cond > 9)
                            htmltext = "32055-05.htm";
                        break;
                    case 32052:
                        if (cond == 2) {
                            htmltext = "32052-01.htm";
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "32052-02.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "32052-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "32052-04.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "32052-05.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "32052-06.htm";
                        break;
                    case 32053:
                        if (cond == 1 || cond == 2) {
                            htmltext = "32053-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32053-03.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "32053-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "32053-05.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "32053-06.htm";
                        break;
                    case 32076:
                        if (cond == 4) {
                            htmltext = "32076-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "32076-03.htm";
                        break;
                    case 32054:
                        if (cond == 10)
                            htmltext = "32054-01.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "7");
        if (st == null)
            return null;
        if (st.dropItems(8495, 1, 1, 200000))
            st.set("cond", "8");
        return null;
    }
}
