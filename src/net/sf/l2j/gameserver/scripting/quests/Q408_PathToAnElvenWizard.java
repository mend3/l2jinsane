package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q408_PathToAnElvenWizard extends Quest {
    private static final String qn = "Q408_PathToAnElvenWizard";

    private static final int ROSELLA_LETTER = 1218;

    private static final int RED_DOWN = 1219;

    private static final int MAGICAL_POWERS_RUBY = 1220;

    private static final int PURE_AQUAMARINE = 1221;

    private static final int APPETIZING_APPLE = 1222;

    private static final int GOLD_LEAVES = 1223;

    private static final int IMMORTAL_LOVE = 1224;

    private static final int AMETHYST = 1225;

    private static final int NOBILITY_AMETHYST = 1226;

    private static final int FERTILITY_PERIDOT = 1229;

    private static final int ETERNITY_DIAMOND = 1230;

    private static final int CHARM_OF_GRAIN = 1272;

    private static final int SAP_OF_THE_MOTHER_TREE = 1273;

    private static final int LUCKY_POTPOURRI = 1274;

    private static final int ROSELLA = 30414;

    private static final int GREENIS = 30157;

    private static final int THALIA = 30371;

    private static final int NORTHWIND = 30423;

    public Q408_PathToAnElvenWizard() {
        super(408, "Path to an Elven Wizard");
        setItemsIds(1218, 1219, 1220, 1221, 1222, 1223, 1224, 1225, 1226, 1229,
                1272, 1273, 1274);
        addStartNpc(30414);
        addTalkId(30414, 30157, 30371, 30423);
        addKillId(20047, 20019, 20466);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q408_PathToAnElvenWizard");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30414-06.htm")) {
            if (player.getClassId() != ClassId.ELVEN_MYSTIC) {
                htmltext = (player.getClassId() == ClassId.ELVEN_WIZARD) ? "30414-02a.htm" : "30414-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30414-04.htm";
            } else if (st.hasQuestItems(1230)) {
                htmltext = "30414-05.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1229, 1);
            }
        } else if (event.equalsIgnoreCase("30414-07.htm")) {
            if (!st.hasQuestItems(1220)) {
                st.playSound("ItemSound.quest_middle");
                st.giveItems(1218, 1);
            } else {
                htmltext = "30414-10.htm";
            }
        } else if (event.equalsIgnoreCase("30414-14.htm")) {
            if (!st.hasQuestItems(1221)) {
                st.playSound("ItemSound.quest_middle");
                st.giveItems(1222, 1);
            } else {
                htmltext = "30414-13.htm";
            }
        } else if (event.equalsIgnoreCase("30414-18.htm")) {
            if (!st.hasQuestItems(1226)) {
                st.playSound("ItemSound.quest_middle");
                st.giveItems(1224, 1);
            } else {
                htmltext = "30414-17.htm";
            }
        } else if (event.equalsIgnoreCase("30157-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1218, 1);
            st.giveItems(1272, 1);
        } else if (event.equalsIgnoreCase("30371-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1222, 1);
            st.giveItems(1273, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q408_PathToAnElvenWizard");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30414-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30414:
                        if (st.hasQuestItems(1220, 1226, 1221)) {
                            htmltext = "30414-24.htm";
                            st.takeItems(1229, 1);
                            st.takeItems(1220, 1);
                            st.takeItems(1226, 1);
                            st.takeItems(1221, 1);
                            st.giveItems(1230, 1);
                            st.rewardExpAndSp(3200L, 1890);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                            break;
                        }
                        if (st.hasQuestItems(1218)) {
                            htmltext = "30414-08.htm";
                            break;
                        }
                        if (st.hasQuestItems(1272)) {
                            if (st.getQuestItemsCount(1219) == 5) {
                                htmltext = "30414-25.htm";
                                break;
                            }
                            htmltext = "30414-09.htm";
                            break;
                        }
                        if (st.hasQuestItems(1222)) {
                            htmltext = "30414-15.htm";
                            break;
                        }
                        if (st.hasQuestItems(1273)) {
                            if (st.getQuestItemsCount(1223) == 5) {
                                htmltext = "30414-26.htm";
                                break;
                            }
                            htmltext = "30414-16.htm";
                            break;
                        }
                        if (st.hasQuestItems(1224)) {
                            htmltext = "30414-19.htm";
                            break;
                        }
                        if (st.hasQuestItems(1274)) {
                            if (st.getQuestItemsCount(1225) == 2) {
                                htmltext = "30414-27.htm";
                                break;
                            }
                            htmltext = "30414-20.htm";
                            break;
                        }
                        htmltext = "30414-11.htm";
                        break;
                    case 30157:
                        if (st.hasQuestItems(1218)) {
                            htmltext = "30157-01.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(1219) == 5) {
                            htmltext = "30157-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1272, 1);
                            st.takeItems(1219, -1);
                            st.giveItems(1220, 1);
                            break;
                        }
                        if (st.hasQuestItems(1272))
                            htmltext = "30157-03.htm";
                        break;
                    case 30371:
                        if (st.hasQuestItems(1222)) {
                            htmltext = "30371-01.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(1223) == 5) {
                            htmltext = "30371-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1223, -1);
                            st.takeItems(1273, 1);
                            st.giveItems(1221, 1);
                            break;
                        }
                        if (st.hasQuestItems(1273))
                            htmltext = "30371-03.htm";
                        break;
                    case 30423:
                        if (st.hasQuestItems(1224)) {
                            htmltext = "30423-01.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1224, 1);
                            st.giveItems(1274, 1);
                            break;
                        }
                        if (st.getQuestItemsCount(1225) == 2) {
                            htmltext = "30423-03.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1225, -1);
                            st.takeItems(1274, 1);
                            st.giveItems(1226, 1);
                            break;
                        }
                        if (st.hasQuestItems(1274))
                            htmltext = "30423-02.htm";
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
            case 20019:
                if (st.hasQuestItems(1273))
                    st.dropItems(1223, 1, 5, 400000);
                break;
            case 20047:
                if (st.hasQuestItems(1274))
                    st.dropItems(1225, 1, 2, 400000);
                break;
            case 20466:
                if (st.hasQuestItems(1272))
                    st.dropItems(1219, 1, 5, 700000);
                break;
        }
        return null;
    }
}
