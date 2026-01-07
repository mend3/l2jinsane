package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q224_TestOfSagittarius extends Quest {
    private static final String qn = "Q224_TestOfSagittarius";

    private static final int BERNARD_INTRODUCTION = 3294;

    private static final int HAMIL_LETTER_1 = 3295;

    private static final int HAMIL_LETTER_2 = 3296;

    private static final int HAMIL_LETTER_3 = 3297;

    private static final int HUNTER_RUNE_1 = 3298;

    private static final int HUNTER_RUNE_2 = 3299;

    private static final int TALISMAN_OF_KADESH = 3300;

    private static final int TALISMAN_OF_SNAKE = 3301;

    private static final int MITHRIL_CLIP = 3302;

    private static final int STAKATO_CHITIN = 3303;

    private static final int REINFORCED_BOWSTRING = 3304;

    private static final int MANASHEN_HORN = 3305;

    private static final int BLOOD_OF_LIZARDMAN = 3306;

    private static final int CRESCENT_MOON_BOW = 3028;

    private static final int WOODEN_ARROW = 17;

    private static final int MARK_OF_SAGITTARIUS = 3293;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int BERNARD = 30702;

    private static final int HAMIL = 30626;

    private static final int SIR_ARON_TANFORD = 30653;

    private static final int VOKIAN = 30514;

    private static final int GAUEN = 30717;

    private static final int ANT = 20079;

    private static final int ANT_CAPTAIN = 20080;

    private static final int ANT_OVERSEER = 20081;

    private static final int ANT_RECRUIT = 20082;

    private static final int ANT_PATROL = 20084;

    private static final int ANT_GUARD = 20086;

    private static final int NOBLE_ANT = 20089;

    private static final int NOBLE_ANT_LEADER = 20090;

    private static final int BREKA_ORC_SHAMAN = 20269;

    private static final int BREKA_ORC_OVERLORD = 20270;

    private static final int MARSH_STAKATO_WORKER = 20230;

    private static final int MARSH_STAKATO_SOLDIER = 20232;

    private static final int MARSH_STAKATO_DRONE = 20234;

    private static final int MARSH_SPIDER = 20233;

    private static final int ROAD_SCAVENGER = 20551;

    private static final int MANASHEN_GARGOYLE = 20563;

    private static final int LETO_LIZARDMAN = 20577;

    private static final int LETO_LIZARDMAN_ARCHER = 20578;

    private static final int LETO_LIZARDMAN_SOLDIER = 20579;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    private static final int LETO_LIZARDMAN_SHAMAN = 20581;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int SERPENT_DEMON_KADESH = 27090;

    public Q224_TestOfSagittarius() {
        super(224, "Test Of Sagittarius");
        setItemsIds(3294, 3295, 3296, 3297, 3298, 3299, 3300, 3301, 3302, 3303,
                3304, 3305, 3306, 3028);
        addStartNpc(30702);
        addTalkId(30702, 30626, 30653, 30514, 30717);
        addKillId(20079, 20080, 20081, 20082, 20084, 20086, 20089, 20090, 20269, 20270,
                20230, 20232, 20234, 20233, 20551, 20563, 20577, 20578, 20579, 20580,
                20581, 20582, 27090);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q224_TestOfSagittarius");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30702-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3294, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30702-04a.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30626-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3294, 1);
            st.giveItems(3295, 1);
        } else if (event.equalsIgnoreCase("30626-07.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3298, 10);
            st.giveItems(3296, 1);
        } else if (event.equalsIgnoreCase("30653-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3295, 1);
        } else if (event.equalsIgnoreCase("30514-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3296, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q224_TestOfSagittarius");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.ROGUE && player.getClassId() != ClassId.ELVEN_SCOUT && player.getClassId() != ClassId.ASSASSIN) {
                    htmltext = "30702-02.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30702-01.htm";
                    break;
                }
                htmltext = "30702-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30702:
                        htmltext = "30702-05.htm";
                        break;
                    case 30626:
                        if (cond == 1) {
                            htmltext = "30626-01.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30626-04.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30626-05.htm";
                            break;
                        }
                        if (cond > 4 && cond < 8) {
                            htmltext = "30626-08.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30626-09.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3299, 10);
                            st.giveItems(3297, 1);
                            break;
                        }
                        if (cond > 8 && cond < 12) {
                            htmltext = "30626-10.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30626-11.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30626-12.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30626-13.htm";
                            st.takeItems(3306, -1);
                            st.takeItems(3028, 1);
                            st.takeItems(3300, 1);
                            st.giveItems(3293, 1);
                            st.rewardExpAndSp(54726L, 20250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30653:
                        if (cond == 2) {
                            htmltext = "30653-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30653-03.htm";
                        break;
                    case 30514:
                        if (cond == 5) {
                            htmltext = "30514-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30514-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30514-04.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3301, 1);
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30514-05.htm";
                        break;
                    case 30717:
                        if (cond == 9) {
                            htmltext = "30717-01.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3297, 1);
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30717-03.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30717-02.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3305, 1);
                            st.takeItems(3302, 1);
                            st.takeItems(3304, 1);
                            st.takeItems(3303, 1);
                            st.giveItems(3028, 1);
                            st.giveItems(17, 10);
                            break;
                        }
                        if (cond > 11)
                            htmltext = "30717-04.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
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
            case 20079:
            case 20080:
            case 20081:
            case 20082:
            case 20084:
            case 20086:
            case 20089:
            case 20090:
                if (st.getInt("cond") == 3 && st.dropItems(3298, 1, 10, 500000))
                    st.set("cond", "4");
                break;
            case 20269:
            case 20270:
                if (st.getInt("cond") == 6 && st.dropItems(3299, 1, 10, 500000)) {
                    st.set("cond", "7");
                    st.giveItems(3301, 1);
                }
                break;
            case 20230:
            case 20232:
            case 20234:
                if (st.getInt("cond") == 10 && st.dropItems(3303, 1, 1, 100000) && st.hasQuestItems(3305, 3302, 3304))
                    st.set("cond", "11");
                break;
            case 20233:
                if (st.getInt("cond") == 10 && st.dropItems(3304, 1, 1, 100000) && st.hasQuestItems(3305, 3302, 3303))
                    st.set("cond", "11");
                break;
            case 20551:
                if (st.getInt("cond") == 10 && st.dropItems(3302, 1, 1, 100000) && st.hasQuestItems(3305, 3304, 3303))
                    st.set("cond", "11");
                break;
            case 20563:
                if (st.getInt("cond") == 10 && st.dropItems(3305, 1, 1, 100000) && st.hasQuestItems(3304, 3302, 3303))
                    st.set("cond", "11");
                break;
            case 20577:
            case 20578:
            case 20579:
            case 20580:
            case 20581:
            case 20582:
                if (st.getInt("cond") == 13) {
                    if ((st.getQuestItemsCount(3306) - 120) * 5 > Rnd.get(100)) {
                        st.playSound("Itemsound.quest_before_battle");
                        st.takeItems(3306, -1);
                        addSpawn(27090, player, false, 300000L, true);
                        break;
                    }
                    st.dropItemsAlways(3306, 1, 0);
                }
                break;
            case 27090:
                if (st.getInt("cond") == 13) {
                    if (st.getItemEquipped(7) == 3028) {
                        st.set("cond", "14");
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(3300, 1);
                        break;
                    }
                    addSpawn(27090, player, false, 300000L, true);
                }
                break;
        }
        return null;
    }
}
