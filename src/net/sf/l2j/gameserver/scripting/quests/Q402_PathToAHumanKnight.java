package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q402_PathToAHumanKnight extends Quest {
    private static final String qn = "Q402_PathToAHumanKnight";

    private static final int SWORD_OF_RITUAL = 1161;

    private static final int COIN_OF_LORDS_1 = 1162;

    private static final int COIN_OF_LORDS_2 = 1163;

    private static final int COIN_OF_LORDS_3 = 1164;

    private static final int COIN_OF_LORDS_4 = 1165;

    private static final int COIN_OF_LORDS_5 = 1166;

    private static final int COIN_OF_LORDS_6 = 1167;

    private static final int GLUDIO_GUARD_MARK_1 = 1168;

    private static final int BUGBEAR_NECKLACE = 1169;

    private static final int EINHASAD_CHURCH_MARK_1 = 1170;

    private static final int EINHASAD_CRUCIFIX = 1171;

    private static final int GLUDIO_GUARD_MARK_2 = 1172;

    private static final int SPIDER_LEG = 1173;

    private static final int EINHASAD_CHURCH_MARK_2 = 1174;

    private static final int LIZARDMAN_TOTEM = 1175;

    private static final int GLUDIO_GUARD_MARK_3 = 1176;

    private static final int GIANT_SPIDER_HUSK = 1177;

    private static final int EINHASAD_CHURCH_MARK_3 = 1178;

    private static final int HORRIBLE_SKULL = 1179;

    private static final int MARK_OF_ESQUIRE = 1271;

    private static final int SIR_KLAUS_VASPER = 30417;

    private static final int BATHIS = 30332;

    private static final int RAYMOND = 30289;

    private static final int BEZIQUE = 30379;

    private static final int LEVIAN = 30037;

    private static final int GILBERT = 30039;

    private static final int BIOTIN = 30031;

    private static final int SIR_AARON_TANFORD = 30653;

    private static final int SIR_COLLIN_WINDAWOOD = 30311;

    public Q402_PathToAHumanKnight() {
        super(402, "Path to a Human Knight");
        setItemsIds(1271, 1162, 1163, 1164, 1165, 1166, 1167, 1168, 1169, 1170,
                1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1175, 1176,
                1177, 1178, 1179);
        addStartNpc(30417);
        addTalkId(30417, 30332, 30289, 30379, 30037, 30039, 30031, 30653, 30311);
        addKillId(20775, 27024, 20038, 20043, 20050, 20030, 20027, 20024, 20103, 20106,
                20108, 20404);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q402_PathToAHumanKnight");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30417-05.htm")) {
            if (player.getClassId() != ClassId.HUMAN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.KNIGHT) ? "30417-02a.htm" : "30417-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30417-02.htm";
            } else if (st.hasQuestItems(1161)) {
                htmltext = "30417-04.htm";
            }
        } else if (event.equalsIgnoreCase("30417-08.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1271, 1);
        } else if (event.equalsIgnoreCase("30332-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1168, 1);
        } else if (event.equalsIgnoreCase("30289-03.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1170, 1);
        } else if (event.equalsIgnoreCase("30379-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1172, 1);
        } else if (event.equalsIgnoreCase("30037-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1174, 1);
        } else if (event.equalsIgnoreCase("30039-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1176, 1);
        } else if (event.equalsIgnoreCase("30031-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1178, 1);
        } else if (event.equalsIgnoreCase("30417-13.htm") || event.equalsIgnoreCase("30417-14.htm")) {
            int coinCount = st.getQuestItemsCount(1162) + st.getQuestItemsCount(1163) + st.getQuestItemsCount(1164) + st.getQuestItemsCount(1165) + st.getQuestItemsCount(1166) + st.getQuestItemsCount(1167);
            st.takeItems(1162, -1);
            st.takeItems(1163, -1);
            st.takeItems(1164, -1);
            st.takeItems(1165, -1);
            st.takeItems(1166, -1);
            st.takeItems(1167, -1);
            st.takeItems(1271, 1);
            st.giveItems(1161, 1);
            st.rewardExpAndSp(3200L, 1500 + 1920 * (coinCount - 3));
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int coins;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q402_PathToAHumanKnight");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30417-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30417:
                        coins = st.getQuestItemsCount(1162) + st.getQuestItemsCount(1163) + st.getQuestItemsCount(1164) + st.getQuestItemsCount(1165) + st.getQuestItemsCount(1166) + st.getQuestItemsCount(1167);
                        if (coins < 3) {
                            htmltext = "30417-09.htm";
                            break;
                        }
                        if (coins == 3) {
                            htmltext = "30417-10.htm";
                            break;
                        }
                        if (coins > 3 && coins < 6) {
                            htmltext = "30417-11.htm";
                            break;
                        }
                        if (coins == 6) {
                            htmltext = "30417-12.htm";
                            st.takeItems(1162, -1);
                            st.takeItems(1163, -1);
                            st.takeItems(1164, -1);
                            st.takeItems(1165, -1);
                            st.takeItems(1166, -1);
                            st.takeItems(1167, -1);
                            st.takeItems(1271, 1);
                            st.giveItems(1161, 1);
                            st.rewardExpAndSp(3200L, 7260);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30332:
                        if (st.hasQuestItems(1162)) {
                            htmltext = "30332-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1168)) {
                            if (st.getQuestItemsCount(1169) < 10) {
                                htmltext = "30332-03.htm";
                                break;
                            }
                            htmltext = "30332-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1169, -1);
                            st.takeItems(1168, 1);
                            st.giveItems(1162, 1);
                            break;
                        }
                        htmltext = "30332-01.htm";
                        break;
                    case 30289:
                        if (st.hasQuestItems(1163)) {
                            htmltext = "30289-06.htm";
                            break;
                        }
                        if (st.hasQuestItems(1170)) {
                            if (st.getQuestItemsCount(1171) < 12) {
                                htmltext = "30289-04.htm";
                                break;
                            }
                            htmltext = "30289-05.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1171, -1);
                            st.takeItems(1170, 1);
                            st.giveItems(1163, 1);
                            break;
                        }
                        htmltext = "30289-01.htm";
                        break;
                    case 30379:
                        if (st.hasQuestItems(1164)) {
                            htmltext = "30379-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1172)) {
                            if (st.getQuestItemsCount(1173) < 20) {
                                htmltext = "30379-03.htm";
                                break;
                            }
                            htmltext = "30379-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1173, -1);
                            st.takeItems(1172, 1);
                            st.giveItems(1164, 1);
                            break;
                        }
                        htmltext = "30379-01.htm";
                        break;
                    case 30037:
                        if (st.hasQuestItems(1165)) {
                            htmltext = "30037-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1174)) {
                            if (st.getQuestItemsCount(1175) < 20) {
                                htmltext = "30037-03.htm";
                                break;
                            }
                            htmltext = "30037-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1175, -1);
                            st.takeItems(1174, 1);
                            st.giveItems(1165, 1);
                            break;
                        }
                        htmltext = "30037-01.htm";
                        break;
                    case 30039:
                        if (st.hasQuestItems(1166)) {
                            htmltext = "30039-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1176)) {
                            if (st.getQuestItemsCount(1177) < 20) {
                                htmltext = "30039-03.htm";
                                break;
                            }
                            htmltext = "30039-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1177, -1);
                            st.takeItems(1176, 1);
                            st.giveItems(1166, 1);
                            break;
                        }
                        htmltext = "30039-01.htm";
                        break;
                    case 30031:
                        if (st.hasQuestItems(1167)) {
                            htmltext = "30031-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1178)) {
                            if (st.getQuestItemsCount(1179) < 10) {
                                htmltext = "30031-03.htm";
                                break;
                            }
                            htmltext = "30031-04.htm";
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1179, -1);
                            st.takeItems(1178, 1);
                            st.giveItems(1167, 1);
                            break;
                        }
                        htmltext = "30031-01.htm";
                        break;
                    case 30653:
                        htmltext = "30653-01.htm";
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
            case 20775:
                if (st.hasQuestItems(1168))
                    st.dropItemsAlways(1169, 1, 10);
                break;
            case 27024:
                if (st.hasQuestItems(1170))
                    st.dropItems(1171, 1, 12, 500000);
                break;
            case 20038:
            case 20043:
            case 20050:
                if (st.hasQuestItems(1172))
                    st.dropItemsAlways(1173, 1, 20);
                break;
            case 20024:
            case 20027:
            case 20030:
                if (st.hasQuestItems(1174))
                    st.dropItems(1175, 1, 20, 500000);
                break;
            case 20103:
            case 20106:
            case 20108:
                if (st.hasQuestItems(1176))
                    st.dropItems(1177, 1, 20, 400000);
                break;
            case 20404:
                if (st.hasQuestItems(1178))
                    st.dropItems(1179, 1, 10, 400000);
                break;
        }
        return null;
    }
}
