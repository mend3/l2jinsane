package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q411_PathToAnAssassin extends Quest {
    private static final String qn = "Q411_PathToAnAssassin";

    private static final int SHILEN_CALL = 1245;

    private static final int ARKENIA_LETTER = 1246;

    private static final int LEIKAN_NOTE = 1247;

    private static final int MOONSTONE_BEAST_MOLAR = 1248;

    private static final int SHILEN_TEARS = 1250;

    private static final int ARKENIA_RECOMMENDATION = 1251;

    private static final int IRON_HEART = 1252;

    private static final int TRISKEL = 30416;

    private static final int ARKENIA = 30419;

    private static final int LEIKAN = 30382;

    public Q411_PathToAnAssassin() {
        super(411, "Path to an Assassin");
        setItemsIds(1245, 1246, 1247, 1248, 1250, 1251);
        addStartNpc(30416);
        addTalkId(30416, 30419, 30382);
        addKillId(27036, 20369);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q411_PathToAnAssassin");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30416-05.htm")) {
            if (player.getClassId() != ClassId.DARK_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ASSASSIN) ? "30416-02a.htm" : "30416-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30416-03.htm";
            } else if (st.hasQuestItems(1252)) {
                htmltext = "30416-04.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1245, 1);
            }
        } else if (event.equalsIgnoreCase("30419-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1245, 1);
            st.giveItems(1246, 1);
        } else if (event.equalsIgnoreCase("30382-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1246, 1);
            st.giveItems(1247, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q411_PathToAnAssassin");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30416-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30416:
                        if (cond == 1) {
                            htmltext = "30416-11.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30416-07.htm";
                            break;
                        }
                        if (cond == 3 || cond == 4) {
                            htmltext = "30416-08.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30416-09.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30416-10.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30416-06.htm";
                            st.takeItems(1251, 1);
                            st.giveItems(1252, 1);
                            st.rewardExpAndSp(3200L, 3930);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30419:
                        if (cond == 1) {
                            htmltext = "30419-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30419-07.htm";
                            break;
                        }
                        if (cond == 3 || cond == 4) {
                            htmltext = "30419-10.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30419-11.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30419-08.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1250, -1);
                            st.giveItems(1251, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30419-09.htm";
                        break;
                    case 30382:
                        if (cond == 2) {
                            htmltext = "30382-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = !st.hasQuestItems(1248) ? "30382-05.htm" : "30382-06.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30382-07.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1248, -1);
                            st.takeItems(1247, -1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30382-09.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30382-08.htm";
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
        if (npc.getNpcId() == 20369) {
            if (st.getInt("cond") == 3 && st.dropItemsAlways(1248, 1, 10))
                st.set("cond", "4");
        } else if (st.getInt("cond") == 5) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1250, 1);
        }
        return null;
    }
}
