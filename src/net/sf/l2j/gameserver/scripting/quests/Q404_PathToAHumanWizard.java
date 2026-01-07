package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q404_PathToAHumanWizard extends Quest {
    private static final String qn = "Q404_PathToAHumanWizard";

    private static final int MAP_OF_LUSTER = 1280;

    private static final int KEY_OF_FLAME = 1281;

    private static final int FLAME_EARING = 1282;

    private static final int BROKEN_BRONZE_MIRROR = 1283;

    private static final int WIND_FEATHER = 1284;

    private static final int WIND_BANGEL = 1285;

    private static final int RAMA_DIARY = 1286;

    private static final int SPARKLE_PEBBLE = 1287;

    private static final int WATER_NECKLACE = 1288;

    private static final int RUST_GOLD_COIN = 1289;

    private static final int RED_SOIL = 1290;

    private static final int EARTH_RING = 1291;

    private static final int BEAD_OF_SEASON = 1292;

    private static final int PARINA = 30391;

    private static final int EARTH_SNAKE = 30409;

    private static final int WASTELAND_LIZARDMAN = 30410;

    private static final int FLAME_SALAMANDER = 30411;

    private static final int WIND_SYLPH = 30412;

    private static final int WATER_UNDINE = 30413;

    public Q404_PathToAHumanWizard() {
        super(404, "Path to a Human Wizard");
        setItemsIds(1280, 1281, 1282, 1283, 1284, 1285, 1286, 1287, 1288, 1289,
                1290, 1291);
        addStartNpc(30391);
        addTalkId(30391, 30409, 30410, 30411, 30412, 30413);
        addKillId(20021, 20359, 27030);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q404_PathToAHumanWizard");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30391-08.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30410-03.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1283, 1);
            st.giveItems(1284, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q404_PathToAHumanWizard");
        if (st == null)
            return htmltext;
        int cond = st.getInt("cond");
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.HUMAN_MYSTIC) {
                    htmltext = (player.getClassId() == ClassId.HUMAN_WIZARD) ? "30391-02a.htm" : "30391-01.htm";
                    break;
                }
                if (player.getLevel() < 19) {
                    htmltext = "30391-02.htm";
                    break;
                }
                if (st.hasQuestItems(1292)) {
                    htmltext = "30391-03.htm";
                    break;
                }
                htmltext = "30391-04.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30391:
                        if (cond < 13) {
                            htmltext = "30391-05.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30391-06.htm";
                            st.takeItems(1291, 1);
                            st.takeItems(1282, 1);
                            st.takeItems(1288, 1);
                            st.takeItems(1285, 1);
                            st.giveItems(1292, 1);
                            st.rewardExpAndSp(3200L, 2020);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30411:
                        if (cond == 1) {
                            htmltext = "30411-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1280, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30411-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30411-03.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1281, 1);
                            st.takeItems(1280, 1);
                            st.giveItems(1282, 1);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30411-04.htm";
                        break;
                    case 30412:
                        if (cond == 4) {
                            htmltext = "30412-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1283, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30412-02.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30412-03.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1284, 1);
                            st.giveItems(1285, 1);
                            break;
                        }
                        if (cond > 6)
                            htmltext = "30412-04.htm";
                        break;
                    case 30410:
                        if (cond == 5) {
                            htmltext = "30410-01.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30410-04.htm";
                        break;
                    case 30413:
                        if (cond == 7) {
                            htmltext = "30413-01.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1286, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30413-02.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30413-03.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1286, 1);
                            st.takeItems(1287, -1);
                            st.giveItems(1288, 1);
                            break;
                        }
                        if (cond > 9)
                            htmltext = "30413-04.htm";
                        break;
                    case 30409:
                        if (cond == 10) {
                            htmltext = "30409-01.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1289, 1);
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30409-02.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30409-03.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1290, 1);
                            st.takeItems(1289, 1);
                            st.giveItems(1291, 1);
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30409-04.htm";
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
            case 20359:
                if (st.getInt("cond") == 2 && st.dropItems(1281, 1, 1, 800000))
                    st.set("cond", "3");
                break;
            case 27030:
                if (st.getInt("cond") == 8 && st.dropItems(1287, 1, 2, 800000))
                    st.set("cond", "9");
                break;
            case 20021:
                if (st.getInt("cond") == 11 && st.dropItems(1290, 1, 1, 200000))
                    st.set("cond", "12");
                break;
        }
        return null;
    }
}
