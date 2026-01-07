package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q415_PathToAMonk extends Quest {
    private static final String qn = "Q415_PathToAMonk";

    private static final int POMEGRANATE = 1593;

    private static final int LEATHER_POUCH_1 = 1594;

    private static final int LEATHER_POUCH_2 = 1595;

    private static final int LEATHER_POUCH_3 = 1596;

    private static final int LEATHER_POUCH_FULL_1 = 1597;

    private static final int LEATHER_POUCH_FULL_2 = 1598;

    private static final int LEATHER_POUCH_FULL_3 = 1599;

    private static final int KASHA_BEAR_CLAW = 1600;

    private static final int KASHA_BLADE_SPIDER_TALON = 1601;

    private static final int SCARLET_SALAMANDER_SCALE = 1602;

    private static final int FIERY_SPIRIT_SCROLL = 1603;

    private static final int ROSHEEK_LETTER = 1604;

    private static final int GANTAKI_LETTER_OF_RECOMMENDATION = 1605;

    private static final int FIG = 1606;

    private static final int LEATHER_POUCH_4 = 1607;

    private static final int LEATHER_POUCH_FULL_4 = 1608;

    private static final int VUKU_ORC_TUSK = 1609;

    private static final int RATMAN_FANG = 1610;

    private static final int LANG_KLIZARDMAN_TOOTH = 1611;

    private static final int FELIM_LIZARDMAN_TOOTH = 1612;

    private static final int IRON_WILL_SCROLL = 1613;

    private static final int TORUKU_LETTER = 1614;

    private static final int KHAVATARI_TOTEM = 1615;

    private static final int KASHA_SPIDER_TOOTH = 8545;

    private static final int HORN_OF_BAAR_DRE_VANUL = 8546;

    private static final int GANTAKI = 30587;

    private static final int ROSHEEK = 30590;

    private static final int KASMAN = 30501;

    private static final int TORUKU = 30591;

    private static final int AREN = 32056;

    private static final int MOIRA = 31979;

    public Q415_PathToAMonk() {
        super(415, "Path to a Monk");
        setItemsIds(1593, 1594, 1595, 1596, 1597, 1598, 1599, 1600, 1601, 1602,
                1603, 1604, 1605, 1606, 1607, 1608, 1609, 1610, 1611, 1612,
                1613, 1614, 8545, 8546);
        addStartNpc(30587);
        addTalkId(30587, 30590, 30501, 30591, 32056, 31979);
        addKillId(20014, 20017, 20024, 20359, 20415, 20476, 20478, 20479, 21118);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q415_PathToAMonk");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30587-05.htm")) {
            if (player.getClassId() != ClassId.ORC_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.MONK) ? "30587-02a.htm" : "30587-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30587-03.htm";
            } else if (st.hasQuestItems(1615)) {
                htmltext = "30587-04.htm";
            }
        } else if (event.equalsIgnoreCase("30587-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1593, 1);
        } else if (event.equalsIgnoreCase("30587-09a.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1604, 1);
            st.giveItems(1605, 1);
        } else if (event.equalsIgnoreCase("30587-09b.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1604, 1);
        } else if (event.equalsIgnoreCase("32056-03.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32056-08.htm")) {
            st.set("cond", "20");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31979-03.htm")) {
            st.takeItems(1603, 1);
            st.giveItems(1615, 1);
            st.rewardExpAndSp(3200L, 4230);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q415_PathToAMonk");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30587-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30587:
                        if (cond == 1) {
                            htmltext = "30587-07.htm";
                            break;
                        }
                        if (cond > 1 && cond < 8) {
                            htmltext = "30587-08.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30587-09.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30587-10.htm";
                            break;
                        }
                        if (cond > 9)
                            htmltext = "30587-11.htm";
                        break;
                    case 30590:
                        if (cond == 1) {
                            htmltext = "30590-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1593, 1);
                            st.giveItems(1594, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30590-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30590-03.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1597, 1);
                            st.giveItems(1595, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30590-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30590-05.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1598, 1);
                            st.giveItems(1596, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30590-06.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30590-07.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1599, 1);
                            st.giveItems(1603, 1);
                            st.giveItems(1604, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30590-08.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30590-09.htm";
                        break;
                    case 30501:
                        if (cond == 9) {
                            htmltext = "30501-01.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1605, 1);
                            st.giveItems(1606, 1);
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30501-02.htm";
                            break;
                        }
                        if (cond == 11 || cond == 12) {
                            htmltext = "30501-03.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30501-04.htm";
                            st.takeItems(1603, 1);
                            st.takeItems(1613, 1);
                            st.takeItems(1614, 1);
                            st.giveItems(1615, 1);
                            st.rewardExpAndSp(3200L, 1500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30591:
                        if (cond == 10) {
                            htmltext = "30591-01.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1606, 1);
                            st.giveItems(1607, 1);
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30591-02.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30591-03.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1608, 1);
                            st.giveItems(1613, 1);
                            st.giveItems(1614, 1);
                            break;
                        }
                        if (cond == 13)
                            htmltext = "30591-04.htm";
                        break;
                    case 32056:
                        if (cond == 14) {
                            htmltext = "32056-01.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "32056-04.htm";
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "32056-05.htm";
                            st.set("cond", "17");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(8545, -1);
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "32056-06.htm";
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "32056-07.htm";
                            st.set("cond", "19");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(8546, -1);
                            break;
                        }
                        if (cond == 20)
                            htmltext = "32056-09.htm";
                        break;
                    case 31979:
                        if (cond == 20)
                            htmltext = "31979-01.htm";
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
        WeaponType weapon = player.getAttackType();
        if (weapon != WeaponType.DUALFIST && weapon != WeaponType.FIST) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
            return null;
        }
        switch (npc.getNpcId()) {
            case 20479:
                if (st.getInt("cond") == 2 && st.dropItemsAlways(1600, 1, 5)) {
                    st.set("cond", "3");
                    st.takeItems(1600, -1);
                    st.takeItems(1594, 1);
                    st.giveItems(1597, 1);
                }
                break;
            case 20478:
                if (st.getInt("cond") == 4 && st.dropItemsAlways(1601, 1, 5)) {
                    st.set("cond", "5");
                    st.takeItems(1601, -1);
                    st.takeItems(1595, 1);
                    st.giveItems(1598, 1);
                    break;
                }
                if (st.getInt("cond") == 15 && st.dropItems(8545, 1, 6, 500000))
                    st.set("cond", "16");
                break;
            case 20476:
                if (st.getInt("cond") == 15 && st.dropItems(8545, 1, 6, 500000))
                    st.set("cond", "16");
                break;
            case 20415:
                if (st.getInt("cond") == 6 && st.dropItemsAlways(1602, 1, 5)) {
                    st.set("cond", "7");
                    st.takeItems(1602, -1);
                    st.takeItems(1596, 1);
                    st.giveItems(1599, 1);
                }
                break;
            case 20014:
                if (st.getInt("cond") == 11 && st.dropItemsAlways(1612, 1, 3))
                    if (st.getQuestItemsCount(1610) == 3 && st.getQuestItemsCount(1611) == 3 && st.getQuestItemsCount(1609) == 3) {
                        st.set("cond", "12");
                        st.takeItems(1609, -1);
                        st.takeItems(1610, -1);
                        st.takeItems(1611, -1);
                        st.takeItems(1612, -1);
                        st.takeItems(1607, 1);
                        st.giveItems(1608, 1);
                    }
                break;
            case 20017:
                if (st.getInt("cond") == 11 && st.dropItemsAlways(1609, 1, 3))
                    if (st.getQuestItemsCount(1610) == 3 && st.getQuestItemsCount(1611) == 3 && st.getQuestItemsCount(1612) == 3) {
                        st.set("cond", "12");
                        st.takeItems(1609, -1);
                        st.takeItems(1610, -1);
                        st.takeItems(1611, -1);
                        st.takeItems(1612, -1);
                        st.takeItems(1607, 1);
                        st.giveItems(1608, 1);
                    }
                break;
            case 20024:
                if (st.getInt("cond") == 11 && st.dropItemsAlways(1611, 1, 3))
                    if (st.getQuestItemsCount(1610) == 3 && st.getQuestItemsCount(1612) == 3 && st.getQuestItemsCount(1609) == 3) {
                        st.set("cond", "12");
                        st.takeItems(1609, -1);
                        st.takeItems(1610, -1);
                        st.takeItems(1611, -1);
                        st.takeItems(1612, -1);
                        st.takeItems(1607, 1);
                        st.giveItems(1608, 1);
                    }
                break;
            case 20359:
                if (st.getInt("cond") == 11 && st.dropItemsAlways(1610, 1, 3))
                    if (st.getQuestItemsCount(1611) == 3 && st.getQuestItemsCount(1612) == 3 && st.getQuestItemsCount(1609) == 3) {
                        st.set("cond", "12");
                        st.takeItems(1609, -1);
                        st.takeItems(1610, -1);
                        st.takeItems(1611, -1);
                        st.takeItems(1612, -1);
                        st.takeItems(1607, 1);
                        st.giveItems(1608, 1);
                    }
                break;
            case 21118:
                if (st.getInt("cond") == 17) {
                    st.set("cond", "18");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(8546, 1);
                }
                break;
        }
        return null;
    }
}
