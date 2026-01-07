package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q123_TheLeaderAndTheFollower extends Quest {
    private static final String qn = "Q123_TheLeaderAndTheFollower";

    private static final String qn2 = "Q118_ToLeadAndBeLed";

    private static final int NEWYEAR = 31961;

    private static final int BRUIN_LIZARDMAN = 27321;

    private static final int PICOT_ARENEID = 27322;

    private static final int BRUIN_LIZARDMAN_BLOOD = 8549;

    private static final int PICOT_ARANEID_LEG = 8550;

    private static final int CRYSTAL_D = 1458;

    private static final int CLAN_OATH_HELM = 7850;

    private static final int CLAN_OATH_ARMOR = 7851;

    private static final int CLAN_OATH_GAUNTLETS = 7852;

    private static final int CLAN_OATH_SABATON = 7853;

    private static final int CLAN_OATH_BRIGANDINE = 7854;

    private static final int CLAN_OATH_LEATHER_GLOVES = 7855;

    private static final int CLAN_OATH_BOOTS = 7856;

    private static final int CLAN_OATH_AKETON = 7857;

    private static final int CLAN_OATH_PADDED_GLOVES = 7858;

    private static final int CLAN_OATH_SANDALS = 7859;

    public Q123_TheLeaderAndTheFollower() {
        super(123, "The Leader and the Follower");
        setItemsIds(8549, 8550);
        addStartNpc(31961);
        addTalkId(31961);
        addKillId(27321, 27322);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q123_TheLeaderAndTheFollower");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31961-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("state", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31961-05d.htm")) {
            if (st.getQuestItemsCount(8549) > 9) {
                st.set("cond", "3");
                st.set("state", "2");
                st.set("stateEx", "1");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(8549, -1);
            }
        } else if (event.equalsIgnoreCase("31961-05e.htm")) {
            if (st.getQuestItemsCount(8549) > 9) {
                st.set("cond", "4");
                st.set("state", "2");
                st.set("stateEx", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(8549, -1);
            }
        } else if (event.equalsIgnoreCase("31961-05f.htm")) {
            if (st.getQuestItemsCount(8549) > 9) {
                st.set("cond", "5");
                st.set("state", "2");
                st.set("stateEx", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(8549, -1);
            }
        } else if (event.equalsIgnoreCase("31961-10.htm")) {
            Player academic = getApprentice(player);
            if (academic != null) {
                QuestState st2 = academic.getQuestState("Q123_TheLeaderAndTheFollower");
                if (st2 != null && st2.getInt("state") == 2) {
                    int stateEx = st2.getInt("stateEx");
                    if (stateEx == 1) {
                        if (st.getQuestItemsCount(1458) > 921) {
                            st.takeItems(1458, 922);
                            st2.set("cond", "6");
                            st2.set("state", "3");
                            st2.playSound("ItemSound.quest_middle");
                        } else {
                            htmltext = "31961-11.htm";
                        }
                    } else if (st.getQuestItemsCount(1458) > 770) {
                        st.takeItems(1458, 771);
                        st2.set("cond", "6");
                        st2.set("state", "3");
                        st2.playSound("ItemSound.quest_middle");
                    } else {
                        htmltext = "31961-11a.htm";
                    }
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int state;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q123_TheLeaderAndTheFollower");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getSponsor() > 0) {
                    QuestState st2 = player.getQuestState("Q118_ToLeadAndBeLed");
                    if (st2 != null) {
                        htmltext = st2.isCompleted() ? "31961-02a.htm" : "31961-02b.htm";
                        break;
                    }
                    htmltext = (player.getLevel() > 18) ? "31961-01.htm" : "31961-02.htm";
                    break;
                }
                if (player.getApprentice() > 0) {
                    Player academic = getApprentice(player);
                    if (academic != null) {
                        QuestState st3 = academic.getQuestState("Q123_TheLeaderAndTheFollower");
                        if (st3 != null) {
                            int i = st3.getInt("state");
                            if (i == 2) {
                                htmltext = "31961-08.htm";
                                break;
                            }
                            if (i == 3) {
                                htmltext = "31961-12.htm";
                                break;
                            }
                            htmltext = "31961-14.htm";
                        }
                        break;
                    }
                    htmltext = "31961-09.htm";
                }
                break;
            case 1:
                state = st.getInt("state");
                if (state == 1) {
                    htmltext = (st.getQuestItemsCount(8549) < 10) ? "31961-04.htm" : "31961-05.htm";
                    break;
                }
                if (state == 2) {
                    int stateEx = st.getInt("stateEx");
                    if (player.getSponsor() == 0) {
                        if (stateEx == 1) {
                            htmltext = "31961-06a.htm";
                            break;
                        }
                        if (stateEx == 2) {
                            htmltext = "31961-06b.htm";
                            break;
                        }
                        if (stateEx == 3)
                            htmltext = "31961-06c.htm";
                        break;
                    }
                    if (getSponsor(player)) {
                        if (stateEx == 1) {
                            htmltext = "31961-06.htm";
                            break;
                        }
                        if (stateEx == 2) {
                            htmltext = "31961-06d.htm";
                            break;
                        }
                        if (stateEx == 3)
                            htmltext = "31961-06e.htm";
                        break;
                    }
                    htmltext = "31961-07.htm";
                    break;
                }
                if (state == 3) {
                    st.set("cond", "7");
                    st.set("state", "4");
                    st.playSound("ItemSound.quest_middle");
                    htmltext = "31961-15.htm";
                    break;
                }
                if (state == 4) {
                    if (st.getQuestItemsCount(8550) > 7) {
                        htmltext = "31961-17.htm";
                        st.takeItems(8550, -1);
                        st.giveItems(7850, 1);
                        switch (st.getInt("stateEx")) {
                            case 1:
                                st.giveItems(7851, 1);
                                st.giveItems(7852, 1);
                                st.giveItems(7853, 1);
                                break;
                            case 2:
                                st.giveItems(7854, 1);
                                st.giveItems(7855, 1);
                                st.giveItems(7856, 1);
                                break;
                            case 3:
                                st.giveItems(7857, 1);
                                st.giveItems(7858, 1);
                                st.giveItems(7859, 1);
                                break;
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(false);
                        break;
                    }
                    htmltext = "31961-16.htm";
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (player.getSponsor() == 0) {
            st.exitQuest(true);
            return null;
        }
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 27321:
                if (cond == 1 && st.dropItems(8549, 1, 10, 700000))
                    st.set("cond", "2");
                break;
            case 27322:
                if (cond == 7 && getSponsor(player) && st.dropItems(8550, 1, 8, 700000))
                    st.set("cond", "8");
                break;
        }
        return null;
    }
}
