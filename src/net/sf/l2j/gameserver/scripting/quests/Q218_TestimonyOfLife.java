package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q218_TestimonyOfLife extends Quest {
    private static final String qn = "Q218_TestimonyOfLife";

    private static final int ASTERIOS = 30154;

    private static final int PUSHKIN = 30300;

    private static final int THALIA = 30371;

    private static final int ADONIUS = 30375;

    private static final int ARKENIA = 30419;

    private static final int CARDIEN = 30460;

    private static final int ISAEL = 30655;

    private static final int TALINS_SPEAR = 3026;

    private static final int CARDIEN_LETTER = 3141;

    private static final int CAMOMILE_CHARM = 3142;

    private static final int HIERARCH_LETTER = 3143;

    private static final int MOONFLOWER_CHARM = 3144;

    private static final int GRAIL_DIAGRAM = 3145;

    private static final int THALIA_LETTER_1 = 3146;

    private static final int THALIA_LETTER_2 = 3147;

    private static final int THALIA_INSTRUCTIONS = 3148;

    private static final int PUSHKIN_LIST = 3149;

    private static final int PURE_MITHRIL_CUP = 3150;

    private static final int ARKENIA_CONTRACT = 3151;

    private static final int ARKENIA_INSTRUCTIONS = 3152;

    private static final int ADONIUS_LIST = 3153;

    private static final int ANDARIEL_SCRIPTURE_COPY = 3154;

    private static final int STARDUST = 3155;

    private static final int ISAEL_INSTRUCTIONS = 3156;

    private static final int ISAEL_LETTER = 3157;

    private static final int GRAIL_OF_PURITY = 3158;

    private static final int TEARS_OF_UNICORN = 3159;

    private static final int WATER_OF_LIFE = 3160;

    private static final int PURE_MITHRIL_ORE = 3161;

    private static final int ANT_SOLDIER_ACID = 3162;

    private static final int WYRM_TALON = 3163;

    private static final int SPIDER_ICHOR = 3164;

    private static final int HARPY_DOWN = 3165;

    private static final int[] TALINS_PIECES = new int[]{3166, 3167, 3168, 3169, 3170, 3171};

    private static final int MARK_OF_LIFE = 3140;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    public Q218_TestimonyOfLife() {
        super(218, "Testimony of Life");
        setItemsIds(3026, 3141, 3142, 3143, 3144, 3145, 3146, 3147, 3148, 3149,
                3150, 3151, 3152, 3153, 3154, 3155, 3156, 3157, 3158, 3159,
                3160, 3161, 3162, 3163, 3164, 3165, 3166, 3167, 3168, 3169,
                3170, 3171);
        addStartNpc(30460);
        addTalkId(30154, 30300, 30371, 30375, 30419, 30460, 30655);
        addKillId(20145, 20176, 20233, 27077, 20550, 20581, 20582, 20082, 20084, 20086,
                20087, 20088);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q218_TestimonyOfLife");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30460-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3141, 1);
            if (!player.getMemos().getBool("secondClassChange37", false)) {
                htmltext = "30460-04a.htm";
                st.giveItems(7562, DF_REWARD_37.get(player.getRace().ordinal()));
                player.getMemos().set("secondClassChange37", true);
            }
        } else if (event.equalsIgnoreCase("30154-07.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3141, 1);
            st.giveItems(3143, 1);
            st.giveItems(3144, 1);
        } else if (event.equalsIgnoreCase("30371-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3143, 1);
            st.giveItems(3145, 1);
        } else if (event.equalsIgnoreCase("30371-11.htm")) {
            st.takeItems(3155, 1);
            st.playSound("ItemSound.quest_middle");
            if (player.getLevel() < 38) {
                htmltext = "30371-10.htm";
                st.set("cond", "13");
                st.giveItems(3148, 1);
            } else {
                st.set("cond", "14");
                st.giveItems(3147, 1);
            }
        } else if (event.equalsIgnoreCase("30300-06.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3145, 1);
            st.giveItems(3149, 1);
        } else if (event.equalsIgnoreCase("30300-10.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3149, 1);
            st.takeItems(3162, -1);
            st.takeItems(3161, -1);
            st.takeItems(3163, -1);
            st.giveItems(3150, 1);
        } else if (event.equalsIgnoreCase("30419-04.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3146, 1);
            st.giveItems(3151, 1);
            st.giveItems(3152, 1);
        } else if (event.equalsIgnoreCase("30375-02.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3152, 1);
            st.giveItems(3153, 1);
        } else if (event.equalsIgnoreCase("30655-02.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3147, 1);
            st.giveItems(3156, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q218_TestimonyOfLife");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30460-01.htm";
                    break;
                }
                if (player.getLevel() < 37 || player.getClassId().level() != 1) {
                    htmltext = "30460-02.htm";
                    break;
                }
                htmltext = "30460-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30154:
                        if (cond == 1) {
                            htmltext = "30154-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30154-08.htm";
                            break;
                        }
                        if (cond == 20) {
                            htmltext = "30154-09.htm";
                            st.set("cond", "21");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3144, 1);
                            st.takeItems(3160, 1);
                            st.giveItems(3142, 1);
                            break;
                        }
                        if (cond == 21)
                            htmltext = "30154-10.htm";
                        break;
                    case 30300:
                        if (cond == 3) {
                            htmltext = "30300-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30300-07.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30300-08.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30300-11.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "30300-12.htm";
                        break;
                    case 30371:
                        if (cond == 2) {
                            htmltext = "30371-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30371-04.htm";
                            break;
                        }
                        if (cond > 3 && cond < 6) {
                            htmltext = "30371-05.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30371-06.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3150, 1);
                            st.giveItems(3146, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30371-07.htm";
                            break;
                        }
                        if (cond > 7 && cond < 12) {
                            htmltext = "30371-08.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30371-09.htm";
                            break;
                        }
                        if (cond == 13) {
                            if (player.getLevel() < 38) {
                                htmltext = "30371-12.htm";
                                break;
                            }
                            htmltext = "30371-13.htm";
                            st.set("cond", "14");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3148, 1);
                            st.giveItems(3147, 1);
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30371-14.htm";
                            break;
                        }
                        if (cond > 14 && cond < 17) {
                            htmltext = "30371-15.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30371-16.htm";
                            st.set("cond", "18");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3157, 1);
                            st.giveItems(3158, 1);
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30371-17.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "30371-18.htm";
                            st.set("cond", "20");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3159, 1);
                            st.giveItems(3160, 1);
                            break;
                        }
                        if (cond > 19)
                            htmltext = "30371-19.htm";
                        break;
                    case 30375:
                        if (cond == 8) {
                            htmltext = "30375-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30375-03.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30375-04.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3153, 1);
                            st.takeItems(3165, -1);
                            st.takeItems(3164, -1);
                            st.giveItems(3154, 1);
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30375-05.htm";
                            break;
                        }
                        if (cond > 11)
                            htmltext = "30375-06.htm";
                        break;
                    case 30419:
                        if (cond == 7) {
                            htmltext = "30419-01.htm";
                            break;
                        }
                        if (cond > 7 && cond < 11) {
                            htmltext = "30419-05.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30419-06.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3154, 1);
                            st.takeItems(3151, 1);
                            st.giveItems(3155, 1);
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30419-07.htm";
                            break;
                        }
                        if (cond > 12)
                            htmltext = "30419-08.htm";
                        break;
                    case 30460:
                        if (cond == 1) {
                            htmltext = "30460-05.htm";
                            break;
                        }
                        if (cond > 1 && cond < 21) {
                            htmltext = "30460-06.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "30460-07.htm";
                            st.takeItems(3142, 1);
                            st.giveItems(3140, 1);
                            st.rewardExpAndSp(104591L, 11250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30655:
                        if (cond == 14) {
                            htmltext = "30655-01.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30655-03.htm";
                            break;
                        }
                        if (cond == 16) {
                            if (st.hasQuestItems(TALINS_PIECES)) {
                                htmltext = "30655-04.htm";
                                st.set("cond", "17");
                                st.playSound("ItemSound.quest_middle");
                                for (int itemId : TALINS_PIECES)
                                    st.takeItems(itemId, 1);
                                st.takeItems(3156, 1);
                                st.giveItems(3157, 1);
                                st.giveItems(3026, 1);
                                break;
                            }
                            htmltext = "30655-03.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "30655-05.htm";
                            break;
                        }
                        if (cond > 17)
                            htmltext = "30655-06.htm";
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
            case 20550:
                if (st.getInt("cond") == 4 && st.dropItems(3161, 1, 10, 500000) &&
                        st.getQuestItemsCount(3163) >= 20 && st.getQuestItemsCount(3162) >= 20)
                    st.set("cond", "5");
                break;
            case 20176:
                if (st.getInt("cond") == 4 && st.dropItems(3163, 1, 20, 500000) &&
                        st.getQuestItemsCount(3161) >= 10 && st.getQuestItemsCount(3162) >= 20)
                    st.set("cond", "5");
                break;
            case 20082:
            case 20084:
            case 20086:
            case 20087:
            case 20088:
                if (st.getInt("cond") == 4 && st.dropItems(3162, 1, 20, 800000) &&
                        st.getQuestItemsCount(3161) >= 10 && st.getQuestItemsCount(3163) >= 20)
                    st.set("cond", "5");
                break;
            case 20233:
                if (st.getInt("cond") == 9 && st.dropItems(3164, 1, 20, 500000) && st.getQuestItemsCount(3165) >= 20)
                    st.set("cond", "10");
                break;
            case 20145:
                if (st.getInt("cond") == 9 && st.dropItems(3165, 1, 20, 500000) && st.getQuestItemsCount(3164) >= 20)
                    st.set("cond", "10");
                break;
            case 27077:
                if (st.getInt("cond") == 18 && st.getItemEquipped(7) == 3026) {
                    st.set("cond", "19");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(3158, 1);
                    st.takeItems(3026, 1);
                    st.giveItems(3159, 1);
                }
                break;
            case 20581:
            case 20582:
                if (st.getInt("cond") == 15 && Rnd.nextBoolean())
                    for (int itemId : TALINS_PIECES) {
                        if (!st.hasQuestItems(itemId)) {
                            st.giveItems(itemId, 1);
                            if (st.hasQuestItems(TALINS_PIECES)) {
                                st.set("cond", "16");
                                st.playSound("ItemSound.quest_middle");
                            } else {
                                st.playSound("ItemSound.quest_itemget");
                            }
                            return null;
                        }
                    }
                break;
        }
        return null;
    }
}
