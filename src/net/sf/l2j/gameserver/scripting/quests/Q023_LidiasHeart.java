package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q023_LidiasHeart extends Quest {
    private static final String qn = "Q023_LidiasHeart";

    private static final int INNOCENTIN = 31328;

    private static final int BROKEN_BOOKSHELF = 31526;

    private static final int GHOST_OF_VON_HELLMANN = 31524;

    private static final int TOMBSTONE = 31523;

    private static final int VIOLET = 31386;

    private static final int BOX = 31530;
    private static final int FOREST_OF_DEADMAN_MAP = 7063;
    private static final int SILVER_KEY = 7149;
    private static final int LIDIA_HAIRPIN = 7148;
    private static final int LIDIA_DIARY = 7064;
    private static final int SILVER_SPEAR = 7150;
    private Npc _ghost = null;

    public Q023_LidiasHeart() {
        super(23, "Lidia's Heart");
        setItemsIds(7149, 7064, 7150);
        addStartNpc(31328);
        addTalkId(31328, 31526, 31524, 31386, 31530, 31523);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q023_LidiasHeart");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31328-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7063, 1);
            st.giveItems(7149, 1);
        } else if (event.equalsIgnoreCase("31328-06.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31526-05.htm")) {
            if (!st.hasQuestItems(7148)) {
                st.giveItems(7148, 1);
                if (st.hasQuestItems(7064)) {
                    st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (event.equalsIgnoreCase("31526-11.htm")) {
            if (!st.hasQuestItems(7064)) {
                st.giveItems(7064, 1);
                if (st.hasQuestItems(7148)) {
                    st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (event.equalsIgnoreCase("31328-11.htm")) {
            if (st.getInt("cond") < 5) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("31328-19.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31524-04.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7064, 1);
        } else if (event.equalsIgnoreCase("31523-02.htm")) {
            if (this._ghost == null) {
                this._ghost = addSpawn(31524, 51432, -54570, -3136, 0, false, 60000L, true);
                this._ghost.broadcastNpcSay("Who awoke me?");
                startQuestTimer("ghost_cleanup", 58000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("31523-05.htm")) {
            if (getQuestTimer("tomb_digger", null, player) == null)
                startQuestTimer("tomb_digger", 10000L, null, player, false);
        } else if (event.equalsIgnoreCase("tomb_digger")) {
            htmltext = "31523-06.htm";
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7149, 1);
        } else if (event.equalsIgnoreCase("31530-02.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7149, 1);
            st.giveItems(7150, 1);
        } else if (event.equalsIgnoreCase("ghost_cleanup")) {
            this._ghost = null;
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q023_LidiasHeart");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                st2 = player.getQuestState("Q022_TragedyInVonHellmannForest");
                if (st2 != null && st2.isCompleted()) {
                    if (player.getLevel() >= 64) {
                        htmltext = "31328-01.htm";
                        break;
                    }
                    htmltext = "31328-00a.htm";
                    break;
                }
                htmltext = "31328-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31328:
                        if (cond == 1) {
                            htmltext = "31328-03.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31328-07.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31328-08.htm";
                            break;
                        }
                        if (cond == 5) {
                            if (st.getInt("diary") == 1) {
                                htmltext = "31328-14.htm";
                                break;
                            }
                            htmltext = "31328-11.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "31328-21.htm";
                        break;
                    case 31526:
                        if (cond == 2) {
                            htmltext = "31526-00.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 3) {
                            if (!st.hasQuestItems(7064)) {
                                htmltext = !st.hasQuestItems(7148) ? "31526-02.htm" : "31526-06.htm";
                                break;
                            }
                            if (!st.hasQuestItems(7148))
                                htmltext = "31526-12.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31526-13.htm";
                        break;
                    case 31524:
                        if (cond == 6) {
                            htmltext = "31524-01.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "31524-05.htm";
                        break;
                    case 31523:
                        if (cond == 6) {
                            htmltext = (this._ghost == null) ? "31523-01.htm" : "31523-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "31523-04.htm";
                            break;
                        }
                        if (cond > 7)
                            htmltext = "31523-06.htm";
                        break;
                    case 31386:
                        if (cond == 8) {
                            htmltext = "31386-01.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "31386-02.htm";
                            break;
                        }
                        if (cond == 10) {
                            if (st.hasQuestItems(7150)) {
                                htmltext = "31386-03.htm";
                                st.takeItems(7150, 1);
                                st.rewardItems(57, 100000);
                                st.playSound("ItemSound.quest_finish");
                                st.exitQuest(false);
                                break;
                            }
                            htmltext = "31386-02.htm";
                            st.set("cond", "9");
                        }
                        break;
                    case 31530:
                        if (cond == 9) {
                            htmltext = "31530-01.htm";
                            break;
                        }
                        if (cond == 10)
                            htmltext = "31530-03.htm";
                        break;
                }
                break;
            case 2:
                if (npc.getNpcId() == 31386) {
                    htmltext = "31386-04.htm";
                    break;
                }
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
