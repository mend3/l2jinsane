package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q032_AnObviousLie extends Quest {
    private static final String qn = "Q032_AnObviousLie";

    private static final int SUEDE = 1866;

    private static final int THREAD = 1868;

    private static final int SPIRIT_ORE = 3031;

    private static final int MAP = 7165;

    private static final int MEDICINAL_HERB = 7166;

    private static final int CAT_EARS = 6843;

    private static final int RACOON_EARS = 7680;

    private static final int RABBIT_EARS = 7683;

    private static final int GENTLER = 30094;

    private static final int MAXIMILIAN = 30120;

    private static final int MIKI_THE_CAT = 31706;

    public Q032_AnObviousLie() {
        super(32, "An Obvious Lie");
        setItemsIds(7165, 7166);
        addStartNpc(30120);
        addTalkId(30120, 30094, 31706);
        addKillId(20135);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q032_AnObviousLie");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30120-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30094-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7165, 1);
        } else if (event.equalsIgnoreCase("31706-1.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7165, 1);
        } else if (event.equalsIgnoreCase("30094-4.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7166, 20);
        } else if (event.equalsIgnoreCase("30094-7.htm")) {
            if (st.getQuestItemsCount(3031) < 500) {
                htmltext = "30094-5.htm";
            } else {
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(3031, 500);
            }
        } else if (event.equalsIgnoreCase("31706-4.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30094-10.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30094-13.htm")) {
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("cat")) {
            if (st.getQuestItemsCount(1868) < 1000 || st.getQuestItemsCount(1866) < 500) {
                htmltext = "30094-11.htm";
            } else {
                htmltext = "30094-14.htm";
                st.takeItems(1866, 500);
                st.takeItems(1868, 1000);
                st.giveItems(6843, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        } else if (event.equalsIgnoreCase("racoon")) {
            if (st.getQuestItemsCount(1868) < 1000 || st.getQuestItemsCount(1866) < 500) {
                htmltext = "30094-11.htm";
            } else {
                htmltext = "30094-14.htm";
                st.takeItems(1866, 500);
                st.takeItems(1868, 1000);
                st.giveItems(7680, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        } else if (event.equalsIgnoreCase("rabbit")) {
            if (st.getQuestItemsCount(1868) < 1000 || st.getQuestItemsCount(1866) < 500) {
                htmltext = "30094-11.htm";
            } else {
                htmltext = "30094-14.htm";
                st.takeItems(1866, 500);
                st.takeItems(1868, 1000);
                st.giveItems(7683, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q032_AnObviousLie");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 45) ? "30120-0a.htm" : "30120-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30120:
                        htmltext = "30120-2.htm";
                        break;
                    case 30094:
                        if (cond == 1) {
                            htmltext = "30094-0.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30094-2.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30094-3.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = (st.getQuestItemsCount(3031) < 500) ? "30094-5.htm" : "30094-6.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30094-8.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30094-9.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = (st.getQuestItemsCount(1868) < 1000 || st.getQuestItemsCount(1866) < 500) ? "30094-11.htm" : "30094-12.htm";
                        break;
                    case 31706:
                        if (cond == 2) {
                            htmltext = "31706-0.htm";
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "31706-2.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "31706-3.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "31706-5.htm";
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "3");
        if (st == null)
            return null;
        if (st.dropItemsAlways(7166, 1, 20))
            st.set("cond", "4");
        return null;
    }
}
