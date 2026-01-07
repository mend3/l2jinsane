package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q412_PathToADarkWizard extends Quest {
    private static final String qn = "Q412_PathToADarkWizard";

    private static final int SEED_OF_ANGER = 1253;

    private static final int SEED_OF_DESPAIR = 1254;

    private static final int SEED_OF_HORROR = 1255;

    private static final int SEED_OF_LUNACY = 1256;

    private static final int FAMILY_REMAINS = 1257;

    private static final int VARIKA_LIQUOR = 1258;

    private static final int KNEE_BONE = 1259;

    private static final int HEART_OF_LUNACY = 1260;

    private static final int JEWEL_OF_DARKNESS = 1261;

    private static final int LUCKY_KEY = 1277;

    private static final int CANDLE = 1278;

    private static final int HUB_SCENT = 1279;

    private static final int VARIKA = 30421;

    private static final int CHARKEREN = 30415;

    private static final int ANNIKA = 30418;

    private static final int ARKENIA = 30419;

    public Q412_PathToADarkWizard() {
        super(412, "Path to a Dark Wizard");
        setItemsIds(1253, 1254, 1255, 1256, 1257, 1258, 1259, 1260, 1277, 1278,
                1279);
        addStartNpc(30421);
        addTalkId(30421, 30415, 30418, 30419);
        addKillId(20015, 20022, 20045, 20517, 20518);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q412_PathToADarkWizard");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30421-05.htm")) {
            if (player.getClassId() != ClassId.DARK_MYSTIC) {
                htmltext = (player.getClassId() == ClassId.DARK_WIZARD) ? "30421-02a.htm" : "30421-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30421-02.htm";
            } else if (st.hasQuestItems(1261)) {
                htmltext = "30421-04.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1254, 1);
            }
        } else if (event.equalsIgnoreCase("30421-07.htm")) {
            if (st.hasQuestItems(1253)) {
                htmltext = "30421-06.htm";
            } else if (st.hasQuestItems(1277)) {
                htmltext = "30421-08.htm";
            } else if (st.getQuestItemsCount(1257) == 3) {
                htmltext = "30421-18.htm";
            }
        } else if (event.equalsIgnoreCase("30421-10.htm")) {
            if (st.hasQuestItems(1255)) {
                htmltext = "30421-09.htm";
            } else if (st.getQuestItemsCount(1259) == 2) {
                htmltext = "30421-19.htm";
            }
        } else if (event.equalsIgnoreCase("30421-13.htm")) {
            if (st.hasQuestItems(1256))
                htmltext = "30421-12.htm";
        } else if (event.equalsIgnoreCase("30415-03.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1277, 1);
        } else if (event.equalsIgnoreCase("30418-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1278, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q412_PathToADarkWizard");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30421-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30421:
                        if (st.hasQuestItems(1253, 1255, 1256)) {
                            htmltext = "30421-16.htm";
                            st.takeItems(1253, 1);
                            st.takeItems(1254, 1);
                            st.takeItems(1255, 1);
                            st.takeItems(1256, 1);
                            st.giveItems(1261, 1);
                            st.rewardExpAndSp(3200L, 1650);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                            break;
                        }
                        htmltext = "30421-17.htm";
                        break;
                    case 30415:
                        if (st.hasQuestItems(1253)) {
                            htmltext = "30415-06.htm";
                            break;
                        }
                        if (!st.hasQuestItems(1277)) {
                            htmltext = "30415-01.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(1257) == 3) {
                            htmltext = "30415-05.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1257, -1);
                            st.takeItems(1277, 1);
                            st.giveItems(1253, 1);
                            break;
                        }
                        htmltext = "30415-04.htm";
                        break;
                    case 30418:
                        if (st.hasQuestItems(1255)) {
                            htmltext = "30418-04.htm";
                            break;
                        }
                        if (!st.hasQuestItems(1278)) {
                            htmltext = "30418-01.htm";
                            break;
                        }
                        if (st.getQuestItemsCount(1259) == 2) {
                            htmltext = "30418-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1278, 1);
                            st.takeItems(1259, -1);
                            st.giveItems(1255, 1);
                            break;
                        }
                        htmltext = "30418-03.htm";
                        break;
                    case 30419:
                        if (st.hasQuestItems(1256)) {
                            htmltext = "30419-03.htm";
                            break;
                        }
                        if (!st.hasQuestItems(1279)) {
                            htmltext = "30419-01.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1279, 1);
                            break;
                        }
                        if (st.getQuestItemsCount(1260) == 3) {
                            htmltext = "30419-03.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1260, -1);
                            st.takeItems(1279, 1);
                            st.giveItems(1256, 1);
                            break;
                        }
                        htmltext = "30419-02.htm";
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
            case 20015:
                if (st.hasQuestItems(1277))
                    st.dropItems(1257, 1, 3, 500000);
                break;
            case 20022:
            case 20517:
            case 20518:
                if (st.hasQuestItems(1278))
                    st.dropItems(1259, 1, 2, 500000);
                break;
            case 20045:
                if (st.hasQuestItems(1279))
                    st.dropItems(1260, 1, 3, 500000);
                break;
        }
        return null;
    }
}
