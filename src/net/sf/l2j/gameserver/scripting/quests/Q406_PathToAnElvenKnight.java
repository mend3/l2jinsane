package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q406_PathToAnElvenKnight extends Quest {
    private static final String qn = "Q406_PathToAnElvenKnight";

    private static final int SORIUS_LETTER = 1202;

    private static final int KLUTO_BOX = 1203;

    private static final int ELVEN_KNIGHT_BROOCH = 1204;

    private static final int TOPAZ_PIECE = 1205;

    private static final int EMERALD_PIECE = 1206;

    private static final int KLUTO_MEMO = 1276;

    private static final int SORIUS = 30327;

    private static final int KLUTO = 30317;

    public Q406_PathToAnElvenKnight() {
        super(406, "Path to an Elven Knight");
        setItemsIds(1202, 1203, 1205, 1206, 1276);
        addStartNpc(30327);
        addTalkId(30327, 30317);
        addKillId(20035, 20042, 20045, 20051, 20054, 20060, 20782);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q406_PathToAnElvenKnight");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30327-05.htm")) {
            if (player.getClassId() != ClassId.ELVEN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ELVEN_KNIGHT) ? "30327-02a.htm" : "30327-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30327-03.htm";
            } else if (st.hasQuestItems(1204)) {
                htmltext = "30327-04.htm";
            }
        } else if (event.equalsIgnoreCase("30327-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30317-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1202, 1);
            st.giveItems(1276, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q406_PathToAnElvenKnight");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30327-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30327:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(1205) ? "30327-07.htm" : "30327-08.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30327-09.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1202, 1);
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "30327-11.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30327-10.htm";
                            st.takeItems(1203, 1);
                            st.takeItems(1276, 1);
                            st.giveItems(1204, 1);
                            st.rewardExpAndSp(3200L, 2280);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30317:
                        if (cond == 3) {
                            htmltext = "30317-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = !st.hasQuestItems(1206) ? "30317-03.htm" : "30317-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30317-05.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1206, -1);
                            st.takeItems(1205, -1);
                            st.giveItems(1203, 1);
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30317-06.htm";
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
            case 20035:
            case 20042:
            case 20045:
            case 20051:
            case 20054:
            case 20060:
                if (st.getInt("cond") == 1 && st.dropItems(1205, 1, 20, 700000))
                    st.set("cond", "2");
                break;
            case 20782:
                if (st.getInt("cond") == 4 && st.dropItems(1206, 1, 20, 500000))
                    st.set("cond", "5");
                break;
        }
        return null;
    }
}
