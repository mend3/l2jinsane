package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q021_HiddenTruth extends Quest {
    private static final String qn = "Q021_HiddenTruth";

    private static final int MYSTERIOUS_WIZARD = 31522;

    private static final int TOMBSTONE = 31523;

    private static final int VON_HELLMAN_DUKE = 31524;

    private static final int VON_HELLMAN_PAGE = 31525;

    private static final int BROKEN_BOOKSHELF = 31526;

    private static final int AGRIPEL = 31348;

    private static final int DOMINIC = 31350;

    private static final int BENEDICT = 31349;

    private static final int INNOCENTIN = 31328;

    private static final int CROSS_OF_EINHASAD = 7140;

    private static final int CROSS_OF_EINHASAD_NEXT_QUEST = 7141;

    private static final Location[] PAGE_LOCS = new Location[]{new Location(51992, -54424, -3160), new Location(52328, -53400, -3160), new Location(51928, -51656, -3096)};

    private Npc _duke;

    private Npc _page;

    public Q021_HiddenTruth() {
        super(21, "Hidden Truth");
        setItemsIds(7140);
        addStartNpc(31522);
        addTalkId(31522, 31523, 31524, 31525, 31526, 31348, 31350, 31349, 31328);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q021_HiddenTruth");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31522-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31523-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            spawnTheDuke(player);
        } else if (event.equalsIgnoreCase("31524-06.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            spawnThePage(player);
        } else if (event.equalsIgnoreCase("31526-08.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31526-14.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7140, 1);
        } else {
            if (event.equalsIgnoreCase("1")) {
                this._page.getAI().setIntention(IntentionType.MOVE_TO, PAGE_LOCS[0]);
                this._page.broadcastNpcSay("Follow me...");
                startQuestTimer("2", 5000L, this._page, player, false);
                return null;
            }
            if (event.equalsIgnoreCase("2")) {
                this._page.getAI().setIntention(IntentionType.MOVE_TO, PAGE_LOCS[1]);
                startQuestTimer("3", 12000L, this._page, player, false);
                return null;
            }
            if (event.equalsIgnoreCase("3")) {
                this._page.getAI().setIntention(IntentionType.MOVE_TO, PAGE_LOCS[2]);
                startQuestTimer("4", 18000L, this._page, player, false);
                return null;
            }
            if (event.equalsIgnoreCase("4")) {
                st.set("end_walk", "1");
                this._page.broadcastNpcSay("Please check this bookcase, " + player.getName() + ".");
                startQuestTimer("5", 47000L, this._page, player, false);
                return null;
            }
            if (event.equalsIgnoreCase("5")) {
                this._page.broadcastNpcSay("I'm confused! Maybe it's time to go back.");
                return null;
            }
            if (event.equalsIgnoreCase("31328-05.htm")) {
                if (st.hasQuestItems(7140)) {
                    st.takeItems(7140, 1);
                    st.giveItems(7141, 1);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                }
            } else {
                if (event.equalsIgnoreCase("dukeDespawn")) {
                    this._duke.deleteMe();
                    this._duke = null;
                    return null;
                }
                if (event.equalsIgnoreCase("pageDespawn")) {
                    this._page.deleteMe();
                    this._page = null;
                    return null;
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q021_HiddenTruth");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 63) ? "31522-03.htm" : "31522-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31522:
                        htmltext = "31522-05.htm";
                        break;
                    case 31523:
                        if (cond == 1) {
                            htmltext = "31523-01.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "31523-04.htm";
                            spawnTheDuke(player);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31523-04.htm";
                        break;
                    case 31524:
                        if (cond == 2) {
                            htmltext = "31524-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "31524-07.htm";
                            spawnThePage(player);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "31524-07a.htm";
                        break;
                    case 31525:
                        if (cond == 3) {
                            if (st.getInt("end_walk") == 1) {
                                htmltext = "31525-02.htm";
                                st.set("cond", "4");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "31525-01.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "31525-02.htm";
                        break;
                    case 31526:
                        if ((cond == 3 && st.getInt("end_walk") == 1) || cond == 4) {
                            htmltext = "31526-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            if (this._page != null) {
                                cancelQuestTimer("5", this._page, player);
                                cancelQuestTimer("pageDespawn", this._page, player);
                                this._page.deleteMe();
                                this._page = null;
                            }
                            if (this._duke != null) {
                                cancelQuestTimer("dukeDespawn", this._duke, player);
                                this._duke.deleteMe();
                                this._duke = null;
                            }
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "31526-10.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "31526-15.htm";
                        break;
                    case 31348:
                    case 31349:
                    case 31350:
                        if ((cond == 6 || cond == 7) && st.hasQuestItems(7140)) {
                            int npcId = npc.getNpcId();
                            if (cond == 6) {
                                int npcId1 = 0, npcId2 = 0;
                                if (npcId == 31348) {
                                    npcId1 = 31349;
                                    npcId2 = 31350;
                                } else if (npcId == 31349) {
                                    npcId1 = 31348;
                                    npcId2 = 31350;
                                } else if (npcId == 31350) {
                                    npcId1 = 31348;
                                    npcId2 = 31349;
                                }
                                if (st.getInt(String.valueOf(npcId1)) == 1 && st.getInt(String.valueOf(npcId2)) == 1) {
                                    st.set("cond", "7");
                                    st.playSound("ItemSound.quest_middle");
                                } else {
                                    st.set(String.valueOf(npcId), "1");
                                }
                            }
                            htmltext = npcId + "-01.htm";
                        }
                        break;
                    case 31328:
                        if (cond == 7 && st.hasQuestItems(7140))
                            htmltext = "31328-01.htm";
                        break;
                }
                break;
            case 2:
                if (npc.getNpcId() == 31328) {
                    htmltext = "31328-06.htm";
                    break;
                }
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    private void spawnTheDuke(Player player) {
        if (this._duke == null) {
            this._duke = addSpawn(31524, 51432, -54570, -3136, 0, false, 0L, true);
            this._duke.broadcastNpcSay("Who awoke me?");
            startQuestTimer("dukeDespawn", 300000L, this._duke, player, false);
        }
    }

    private void spawnThePage(Player player) {
        if (this._page == null) {
            this._page = addSpawn(31525, 51608, -54520, -3168, 0, false, 0L, true);
            this._page.broadcastNpcSay("My master has instructed me to be your guide, " + player.getName() + ".");
            startQuestTimer("1", 4000L, this._page, player, false);
            startQuestTimer("pageDespawn", 90000L, this._page, player, false);
        }
    }
}
