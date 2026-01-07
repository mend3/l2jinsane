package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q410_PathToAPalusKnight extends Quest {
    private static final String qn = "Q410_PathToAPalusKnight";

    private static final int PALUS_TALISMAN = 1237;

    private static final int LYCANTHROPE_SKULL = 1238;

    private static final int VIRGIL_LETTER = 1239;

    private static final int MORTE_TALISMAN = 1240;

    private static final int PREDATOR_CARAPACE = 1241;

    private static final int ARACHNID_TRACKER_SILK = 1242;

    private static final int COFFIN_OF_ETERNAL_REST = 1243;

    private static final int GAZE_OF_ABYSS = 1244;

    private static final int KALINTA = 30422;

    private static final int VIRGIL = 30329;

    private static final int POISON_SPIDER = 20038;

    private static final int ARACHNID_TRACKER = 20043;

    private static final int LYCANTHROPE = 20049;

    public Q410_PathToAPalusKnight() {
        super(410, "Path to a Palus Knight");
        setItemsIds(1237, 1238, 1239, 1240, 1241, 1242, 1243);
        addStartNpc(30329);
        addTalkId(30329, 30422);
        addKillId(20038, 20043, 20049);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q410_PathToAPalusKnight");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30329-05.htm")) {
            if (player.getClassId() != ClassId.DARK_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.PALUS_KNIGHT) ? "30329-02a.htm" : "30329-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30329-02.htm";
            } else if (st.hasQuestItems(1244)) {
                htmltext = "30329-04.htm";
            }
        } else if (event.equalsIgnoreCase("30329-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1237, 1);
        } else if (event.equalsIgnoreCase("30329-10.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1238, -1);
            st.takeItems(1237, 1);
            st.giveItems(1239, 1);
        } else if (event.equalsIgnoreCase("30422-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1239, 1);
            st.giveItems(1240, 1);
        } else if (event.equalsIgnoreCase("30422-06.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1242, -1);
            st.takeItems(1240, 1);
            st.takeItems(1241, -1);
            st.giveItems(1243, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q410_PathToAPalusKnight");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30329-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30329:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(1238) ? "30329-07.htm" : "30329-08.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30329-09.htm";
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "30329-12.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30329-11.htm";
                            st.takeItems(1243, 1);
                            st.giveItems(1244, 1);
                            st.rewardExpAndSp(3200L, 1500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30422:
                        if (cond == 3) {
                            htmltext = "30422-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (!st.hasQuestItems(1242) || !st.hasQuestItems(1241)) {
                                htmltext = "30422-03.htm";
                                break;
                            }
                            htmltext = "30422-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30422-05.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30422-06.htm";
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
        switch (npc.getNpcId()) {
            case 20049:
                if (st.getInt("cond") == 1 && st.dropItemsAlways(1238, 1, 13))
                    st.set("cond", "2");
                break;
            case 20043:
                if (st.getInt("cond") == 4 && st.dropItemsAlways(1242, 1, 5) && st.hasQuestItems(1241))
                    st.set("cond", "5");
                break;
            case 20038:
                if (st.getInt("cond") == 4 && st.dropItemsAlways(1241, 1, 1) && st.getQuestItemsCount(1242) == 5)
                    st.set("cond", "5");
                break;
        }
        return null;
    }
}
