package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q409_PathToAnElvenOracle extends Quest {
    private static final String qn = "Q409_PathToAnElvenOracle";

    private static final int CRYSTAL_MEDALLION = 1231;

    private static final int SWINDLER_MONEY = 1232;

    private static final int ALLANA_DIARY = 1233;

    private static final int LIZARD_CAPTAIN_ORDER = 1234;

    private static final int LEAF_OF_ORACLE = 1235;

    private static final int HALF_OF_DIARY = 1236;

    private static final int TAMIL_NECKLACE = 1275;

    private static final int MANUEL = 30293;

    private static final int ALLANA = 30424;

    private static final int PERRIN = 30428;

    public Q409_PathToAnElvenOracle() {
        super(409, "Path to an Elven Oracle");
        setItemsIds(1231, 1232, 1233, 1234, 1236, 1275);
        addStartNpc(30293);
        addTalkId(30293, 30424, 30428);
        addKillId(27032, 27033, 27034, 27035);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q409_PathToAnElvenOracle");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30293-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1231, 1);
        } else {
            if (event.equalsIgnoreCase("spawn_lizards")) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                addSpawn(27032, -92319, 154235, -3284, 2000, false, 0L, false);
                addSpawn(27033, -92361, 154190, -3284, 2000, false, 0L, false);
                addSpawn(27034, -92375, 154278, -3278, 2000, false, 0L, false);
                return null;
            }
            if (event.equalsIgnoreCase("30428-06.htm"))
                addSpawn(27035, -93194, 147587, -2672, 2000, false, 0L, true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q409_PathToAnElvenOracle");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.ELVEN_MYSTIC) {
                    htmltext = (player.getClassId() == ClassId.ELVEN_ORACLE) ? "30293-02a.htm" : "30293-02.htm";
                    break;
                }
                if (player.getLevel() < 19) {
                    htmltext = "30293-03.htm";
                    break;
                }
                if (st.hasQuestItems(1235)) {
                    htmltext = "30293-04.htm";
                    break;
                }
                htmltext = "30293-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30293:
                        if (cond == 1) {
                            htmltext = "30293-06.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30293-09.htm";
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30293-07.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30293-08.htm";
                            st.takeItems(1233, 1);
                            st.takeItems(1231, 1);
                            st.takeItems(1234, 1);
                            st.takeItems(1232, 1);
                            st.giveItems(1235, 1);
                            st.rewardExpAndSp(3200L, 1130);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30424:
                        if (cond == 1) {
                            htmltext = "30424-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30424-02.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1236, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30424-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30424-06.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30424-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1236, -1);
                            st.giveItems(1233, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30424-05.htm";
                        break;
                    case 30428:
                        if (cond == 4) {
                            htmltext = "30428-01.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30428-04.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1275, -1);
                            st.giveItems(1232, 1);
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30428-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (npc.getNpcId() == 27035) {
            if (st.getInt("cond") == 4) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(1275, 1);
            }
        } else if (st.getInt("cond") == 2) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1234, 1);
        }
        return null;
    }
}
