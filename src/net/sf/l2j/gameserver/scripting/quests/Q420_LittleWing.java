package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q420_LittleWing extends Quest {
    private static final String qn = "Q420_LittleWing";

    private static final int COAL = 1870;

    private static final int CHARCOAL = 1871;

    private static final int SILVER_NUGGET = 1873;

    private static final int STONE_OF_PURITY = 1875;

    private static final int GEMSTONE_D = 2130;

    private static final int GEMSTONE_C = 2131;

    private static final int FAIRY_DUST = 3499;

    private static final int FAIRY_STONE = 3816;

    private static final int DELUXE_FAIRY_STONE = 3817;

    private static final int FAIRY_STONE_LIST = 3818;

    private static final int DELUXE_FAIRY_STONE_LIST = 3819;

    private static final int TOAD_LORD_BACK_SKIN = 3820;

    private static final int JUICE_OF_MONKSHOOD = 3821;

    private static final int SCALE_OF_DRAKE_EXARION = 3822;

    private static final int EGG_OF_DRAKE_EXARION = 3823;

    private static final int SCALE_OF_DRAKE_ZWOV = 3824;

    private static final int EGG_OF_DRAKE_ZWOV = 3825;

    private static final int SCALE_OF_DRAKE_KALIBRAN = 3826;

    private static final int EGG_OF_DRAKE_KALIBRAN = 3827;

    private static final int SCALE_OF_WYVERN_SUZET = 3828;

    private static final int EGG_OF_WYVERN_SUZET = 3829;

    private static final int SCALE_OF_WYVERN_SHAMHAI = 3830;

    private static final int EGG_OF_WYVERN_SHAMHAI = 3831;

    private static final int DRAGONFLUTE_OF_WIND = 3500;

    private static final int DRAGONFLUTE_OF_STAR = 3501;

    private static final int DRAGONFLUTE_OF_TWILIGHT = 3502;

    private static final int HATCHLING_SOFT_LEATHER = 3912;

    private static final int FOOD_FOR_HATCHLING = 4038;

    private static final int MARIA = 30608;

    private static final int CRONOS = 30610;

    private static final int BYRON = 30711;

    private static final int MIMYU = 30747;

    private static final int EXARION = 30748;

    private static final int ZWOV = 30749;

    private static final int KALIBRAN = 30750;

    private static final int SUZET = 30751;

    private static final int SHAMHAI = 30752;

    private static final int COOPER = 30829;

    private static final SpawnLocation[] LOCATIONS = new SpawnLocation[]{new SpawnLocation(109816, 40854, -4640, 0), new SpawnLocation(108940, 41615, -4643, 0), new SpawnLocation(110395, 41625, -4642, 0)};

    private static int _counter = 0;

    public Q420_LittleWing() {
        super(420, "Little Wing");
        setItemsIds(3816, 3817, 3818, 3819, 3820, 3821, 3822, 3823, 3824, 3825,
                3826, 3827, 3828, 3829, 3830, 3831);
        addStartNpc(30829, 30747);
        addTalkId(30608, 30610, 30711, 30747, 30748, 30749, 30750, 30751, 30752, 30829);
        addKillId(20202, 20231, 20233, 20270, 20551, 20580, 20589, 20590, 20591, 20592,
                20593, 20594, 20595, 20596, 20597, 20598, 20599);
    }

    private static boolean checkItems(QuestState st, boolean isDeluxe) {
        if (st.getQuestItemsCount(1870) < 10 || st.getQuestItemsCount(1871) < 10)
            return false;
        if (isDeluxe) {
            return st.getQuestItemsCount(2131) >= 1 && st.getQuestItemsCount(1873) >= 5 && st.getQuestItemsCount(1875) >= 1 && st.getQuestItemsCount(3820) >= 20;
        } else return st.getQuestItemsCount(2130) >= 1 && st.getQuestItemsCount(1873) >= 3 && st.getQuestItemsCount(3820) >= 10;
    }

    private static void giveRandomPet(QuestState st, boolean hasFairyDust) {
        int pet = 3502;
        int chance = Rnd.get(100);
        if (st.hasQuestItems(3823)) {
            st.takeItems(3823, 1);
            if (hasFairyDust) {
                if (chance < 45) {
                    pet = 3500;
                } else if (chance < 75) {
                    pet = 3501;
                }
            } else if (chance < 50) {
                pet = 3500;
            } else if (chance < 85) {
                pet = 3501;
            }
        } else if (st.hasQuestItems(3829)) {
            st.takeItems(3829, 1);
            if (hasFairyDust) {
                if (chance < 55) {
                    pet = 3500;
                } else if (chance < 85) {
                    pet = 3501;
                }
            } else if (chance < 65) {
                pet = 3500;
            } else if (chance < 95) {
                pet = 3501;
            }
        } else if (st.hasQuestItems(3827)) {
            st.takeItems(3827, 1);
            if (hasFairyDust) {
                if (chance < 60) {
                    pet = 3500;
                } else if (chance < 90) {
                    pet = 3501;
                }
            } else if (chance < 70) {
                pet = 3500;
            } else {
                pet = 3501;
            }
        } else if (st.hasQuestItems(3831)) {
            st.takeItems(3831, 1);
            if (hasFairyDust) {
                if (chance < 70) {
                    pet = 3500;
                } else {
                    pet = 3501;
                }
            } else if (chance < 85) {
                pet = 3500;
            } else {
                pet = 3501;
            }
        } else if (st.hasQuestItems(3825)) {
            st.takeItems(3825, 1);
            if (hasFairyDust) {
                if (chance < 90) {
                    pet = 3500;
                } else {
                    pet = 3501;
                }
            } else {
                pet = 3500;
            }
        }
        st.giveItems(pet, 1);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q420_LittleWing");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30829-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30610-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3818, 1);
        } else if (event.equalsIgnoreCase("30610-06.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3819, 1);
        } else if (event.equalsIgnoreCase("30610-12.htm")) {
            st.set("cond", "2");
            st.set("deluxestone", "1");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3818, 1);
        } else if (event.equalsIgnoreCase("30610-13.htm")) {
            st.set("cond", "2");
            st.set("deluxestone", "1");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3819, 1);
        } else if (event.equalsIgnoreCase("30608-03.htm")) {
            if (!checkItems(st, false)) {
                htmltext = "30608-01.htm";
            } else {
                st.takeItems(1870, 10);
                st.takeItems(1871, 10);
                st.takeItems(2130, 1);
                st.takeItems(1873, 3);
                st.takeItems(3820, -1);
                st.takeItems(3818, 1);
                st.giveItems(3816, 1);
            }
        } else if (event.equalsIgnoreCase("30608-05.htm")) {
            if (!checkItems(st, true)) {
                htmltext = "30608-01.htm";
            } else {
                st.takeItems(1870, 10);
                st.takeItems(1871, 10);
                st.takeItems(2131, 1);
                st.takeItems(1875, 1);
                st.takeItems(1873, 5);
                st.takeItems(3820, -1);
                st.takeItems(3819, 1);
                st.giveItems(3817, 1);
            }
        } else if (event.equalsIgnoreCase("30711-03.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            if (st.hasQuestItems(3817))
                htmltext = "30711-04.htm";
        } else if (event.equalsIgnoreCase("30747-02.htm")) {
            st.set("mimyu", "1");
            st.takeItems(3816, 1);
        } else if (event.equalsIgnoreCase("30747-04.htm")) {
            st.set("mimyu", "1");
            st.takeItems(3817, 1);
            st.giveItems(3499, 1);
        } else if (event.equalsIgnoreCase("30747-07.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3821, 1);
        } else if (event.equalsIgnoreCase("30747-12.htm") && !st.hasQuestItems(3499)) {
            htmltext = "30747-15.htm";
            giveRandomPet(st, false);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30747-13.htm")) {
            giveRandomPet(st, st.hasQuestItems(3499));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30747-14.htm")) {
            if (st.hasQuestItems(3499)) {
                st.takeItems(3499, 1);
                giveRandomPet(st, true);
                if (Rnd.get(20) == 1) {
                    st.giveItems(3912, 1);
                } else {
                    htmltext = "30747-14t.htm";
                    st.giveItems(4038, 20);
                }
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "30747-13.htm";
            }
        } else if (event.equalsIgnoreCase("30748-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3821, 1);
            st.giveItems(3822, 1);
        } else if (event.equalsIgnoreCase("30749-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3821, 1);
            st.giveItems(3824, 1);
        } else if (event.equalsIgnoreCase("30750-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3821, 1);
            st.giveItems(3826, 1);
        } else if (event.equalsIgnoreCase("30750-05.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3827, 19);
            st.takeItems(3826, 1);
        } else if (event.equalsIgnoreCase("30751-03.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3821, 1);
            st.giveItems(3828, 1);
        } else if (event.equalsIgnoreCase("30752-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3821, 1);
            st.giveItems(3830, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, deluxestone;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q420_LittleWing");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                switch (npc.getNpcId()) {
                    case 30829:
                        htmltext = (player.getLevel() >= 35) ? "30829-01.htm" : "30829-03.htm";
                        break;
                    case 30747:
                        _counter++;
                        npc.teleportTo(LOCATIONS[_counter % 3], 0);
                        return null;
                }
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30829:
                        htmltext = "30829-04.htm";
                        break;
                    case 30610:
                        if (cond == 1) {
                            htmltext = "30610-01.htm";
                            break;
                        }
                        if (st.getInt("deluxestone") == 2) {
                            htmltext = "30610-10.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (st.hasAtLeastOneQuestItem(3816, 3817)) {
                                if (st.getInt("deluxestone") == 1) {
                                    htmltext = "30610-14.htm";
                                    break;
                                }
                                htmltext = "30610-08.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "30610-07.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30610-09.htm";
                            break;
                        }
                        if (cond == 4 && st.hasAtLeastOneQuestItem(3816, 3817))
                            htmltext = "30610-11.htm";
                        break;
                    case 30608:
                        if (st.hasAtLeastOneQuestItem(3816, 3817)) {
                            htmltext = "30608-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (st.hasQuestItems(3818)) {
                                htmltext = checkItems(st, false) ? "30608-02.htm" : "30608-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(3819))
                                htmltext = checkItems(st, true) ? "30608-04.htm" : "30608-01.htm";
                        }
                        break;
                    case 30711:
                        deluxestone = st.getInt("deluxestone");
                        if (deluxestone == 1) {
                            if (st.hasQuestItems(3816)) {
                                htmltext = "30711-05.htm";
                                st.set("cond", "4");
                                st.unset("deluxestone");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            if (st.hasQuestItems(3817)) {
                                htmltext = "30711-06.htm";
                                st.set("cond", "4");
                                st.unset("deluxestone");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "30711-10.htm";
                            break;
                        }
                        if (deluxestone == 2) {
                            htmltext = "30711-09.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30711-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (st.hasQuestItems(3816)) {
                                htmltext = "30711-07.htm";
                                break;
                            }
                            if (st.hasQuestItems(3817))
                                htmltext = "30711-08.htm";
                        }
                        break;
                    case 30747:
                        if (cond == 4) {
                            if (st.getInt("mimyu") == 1) {
                                htmltext = "30747-06.htm";
                                break;
                            }
                            if (st.hasQuestItems(3816)) {
                                htmltext = "30747-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(3817))
                                htmltext = "30747-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30747-08.htm";
                            break;
                        }
                        if (cond == 6) {
                            int eggs = st.getQuestItemsCount(3823) + st.getQuestItemsCount(3825) + st.getQuestItemsCount(3827) + st.getQuestItemsCount(3829) + st.getQuestItemsCount(3831);
                            if (eggs < 20) {
                                htmltext = "30747-09.htm";
                                break;
                            }
                            htmltext = "30747-10.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30747-11.htm";
                            break;
                        }
                        _counter++;
                        npc.teleportTo(LOCATIONS[_counter % 3], 0);
                        return null;
                    case 30748:
                        if (cond == 5) {
                            htmltext = "30748-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(3823) < 20) {
                                htmltext = "30748-03.htm";
                                break;
                            }
                            htmltext = "30748-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3823, 19);
                            st.takeItems(3822, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30748-05.htm";
                        break;
                    case 30749:
                        if (cond == 5) {
                            htmltext = "30749-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(3825) < 20) {
                                htmltext = "30749-03.htm";
                                break;
                            }
                            htmltext = "30749-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3825, 19);
                            st.takeItems(3824, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30749-05.htm";
                        break;
                    case 30750:
                        if (cond == 5) {
                            htmltext = "30750-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = (st.getQuestItemsCount(3827) < 20) ? "30750-03.htm" : "30750-04.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30750-06.htm";
                        break;
                    case 30751:
                        if (cond == 5) {
                            htmltext = "30751-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(3829) < 20) {
                                htmltext = "30751-04.htm";
                                break;
                            }
                            htmltext = "30751-05.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3829, 19);
                            st.takeItems(3828, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30751-06.htm";
                        break;
                    case 30752:
                        if (cond == 5) {
                            htmltext = "30752-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(3831) < 20) {
                                htmltext = "30752-03.htm";
                                break;
                            }
                            htmltext = "30752-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3831, 19);
                            st.takeItems(3830, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30752-05.htm";
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
            case 20231:
                if (st.hasQuestItems(3818)) {
                    st.dropItems(3820, 1, 10, 300000);
                    break;
                }
                if (st.hasQuestItems(3819))
                    st.dropItems(3820, 1, 20, 300000);
                break;
            case 20580:
                if (st.hasQuestItems(3822) && !st.dropItems(3823, 1, 20, 500000))
                    npc.broadcastNpcSay("If the eggs get taken, we're dead!");
                break;
            case 20233:
                if (st.hasQuestItems(3824))
                    st.dropItems(3825, 1, 20, 500000);
                break;
            case 20551:
                if (st.hasQuestItems(3826) && !st.dropItems(3827, 1, 20, 500000))
                    npc.broadcastNpcSay("Hey! Everybody watch the eggs!");
                break;
            case 20270:
                if (st.hasQuestItems(3828) && !st.dropItems(3829, 1, 20, 500000))
                    npc.broadcastNpcSay("I thought I'd caught one share... Whew!");
                break;
            case 20202:
                if (st.hasQuestItems(3830))
                    st.dropItems(3831, 1, 20, 500000);
                break;
            case 20589:
            case 20590:
            case 20591:
            case 20592:
            case 20593:
            case 20594:
            case 20595:
            case 20596:
            case 20597:
            case 20598:
            case 20599:
                if (st.hasQuestItems(3817) && Rnd.get(100) < 30) {
                    st.set("deluxestone", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(3817, 1);
                    npc.broadcastNpcSay("The stone... the Elven stone... broke...");
                }
                break;
        }
        return null;
    }
}
