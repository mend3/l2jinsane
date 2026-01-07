package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q211_TrialOfTheChallenger extends Quest {
    private static final String qn = "Q211_TrialOfTheChallenger";

    private static final int LETTER_OF_KASH = 2628;

    private static final int WATCHER_EYE_1 = 2629;

    private static final int WATCHER_EYE_2 = 2630;

    private static final int SCROLL_OF_SHYSLASSYS = 2631;

    private static final int BROKEN_KEY = 2632;

    private static final int ADENA = 57;

    private static final int ELVEN_NECKLACE_BEADS = 1904;

    private static final int WHITE_TUNIC_PATTERN = 1936;

    private static final int IRON_BOOTS_DESIGN = 1940;

    private static final int MANTICOR_SKIN_GAITERS_PATTERN = 1943;

    private static final int RIP_GAUNTLETS_PATTERN = 1946;

    private static final int TOME_OF_BLOOD_PAGE = 2030;

    private static final int MITHRIL_SCALE_GAITERS_MATERIAL = 2918;

    private static final int BRIGANDINE_GAUNTLETS_PATTERN = 2927;

    private static final int MARK_OF_CHALLENGER = 2627;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int FILAUR = 30535;

    private static final int KASH = 30644;

    private static final int MARTIEN = 30645;

    private static final int RALDO = 30646;

    private static final int CHEST_OF_SHYSLASSYS = 30647;

    private static final int SHYSLASSYS = 27110;

    private static final int GORR = 27112;

    private static final int BARAHAM = 27113;

    private static final int SUCCUBUS_QUEEN = 27114;

    public Q211_TrialOfTheChallenger() {
        super(211, "Trial of the Challenger");
        setItemsIds(2628, 2629, 2630, 2631, 2632);
        addStartNpc(30644);
        addTalkId(30535, 30644, 30645, 30646, 30647);
        addKillId(27110, 27112, 27113, 27114);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q211_TrialOfTheChallenger");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30644-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            if (!player.getMemos().getBool("secondClassChange35", false)) {
                htmltext = "30644-05a.htm";
                st.giveItems(7562, DF_REWARD_35.get(player.getClassId().getId()));
                player.getMemos().set("secondClassChange35", true);
            }
        } else if (event.equalsIgnoreCase("30645-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2628, 1);
        } else if (event.equalsIgnoreCase("30646-04.htm") || event.equalsIgnoreCase("30646-06.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2630, 1);
        } else if (event.equalsIgnoreCase("30647-04.htm")) {
            if (st.hasQuestItems(2632))
                if (Rnd.get(10) < 2) {
                    htmltext = "30647-03.htm";
                    st.playSound("ItemSound.quest_jackpot");
                    st.takeItems(2632, 1);
                    int chance = Rnd.get(100);
                    if (chance > 90) {
                        st.rewardItems(2927, 1);
                        st.rewardItems(1940, 1);
                        st.rewardItems(1943, 1);
                        st.rewardItems(2918, 1);
                        st.rewardItems(1946, 1);
                    } else if (chance > 70) {
                        st.rewardItems(1904, 1);
                        st.rewardItems(2030, 1);
                    } else if (chance > 40) {
                        st.rewardItems(1936, 1);
                    } else {
                        st.rewardItems(1940, 1);
                    }
                } else {
                    htmltext = "30647-02.htm";
                    st.takeItems(2632, 1);
                    st.rewardItems(57, Rnd.get(1, 1000));
                }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q211_TrialOfTheChallenger");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.WARRIOR && player.getClassId() != ClassId.ELVEN_KNIGHT && player.getClassId() != ClassId.PALUS_KNIGHT && player.getClassId() != ClassId.ORC_RAIDER && player.getClassId() != ClassId.MONK) {
                    htmltext = "30644-02.htm";
                    break;
                }
                if (player.getLevel() < 35) {
                    htmltext = "30644-01.htm";
                    break;
                }
                htmltext = "30644-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30644:
                        if (cond == 1) {
                            htmltext = "30644-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30644-07.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2631, 1);
                            st.giveItems(2628, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30644-08.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30644-09.htm";
                        break;
                    case 30647:
                        htmltext = "30647-01.htm";
                        break;
                    case 30645:
                        if (cond == 3) {
                            htmltext = "30645-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30645-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30645-04.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2629, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30645-05.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30645-07.htm";
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30645-06.htm";
                        break;
                    case 30646:
                        if (cond == 7) {
                            htmltext = "30646-01.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30646-06a.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30646-07.htm";
                            st.takeItems(2632, 1);
                            st.giveItems(2627, 1);
                            st.rewardExpAndSp(72394L, 11250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30535:
                        if (cond == 8) {
                            if (player.getLevel() >= 36) {
                                htmltext = "30535-01.htm";
                                st.set("cond", "9");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "30535-03.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30535-02.htm";
                            st.addRadar(176560, -184969, -3729);
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30535-04.htm";
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 27110:
                if (st.getInt("cond") == 1) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2632, 1);
                    st.giveItems(2631, 1);
                    addSpawn(30647, npc, false, 200000L, true);
                }
                break;
            case 27112:
                if (st.getInt("cond") == 4 && st.dropItemsAlways(2629, 1, 1))
                    st.set("cond", "5");
                break;
            case 27113:
                if (st.getInt("cond") == 6 && st.dropItemsAlways(2630, 1, 1))
                    st.set("cond", "7");
                addSpawn(30646, npc, false, 100000L, true);
                break;
            case 27114:
                if (st.getInt("cond") == 9) {
                    st.set("cond", "10");
                    st.playSound("ItemSound.quest_middle");
                }
                addSpawn(30646, npc, false, 100000L, true);
                break;
        }
        return null;
    }
}
