package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q229_TestOfWitchcraft extends Quest {
    private static final String qn = "Q229_TestOfWitchcraft";

    private static final int ORIM_DIAGRAM = 3308;

    private static final int ALEXANDRIA_BOOK = 3309;

    private static final int IKER_LIST = 3310;

    private static final int DIRE_WYRM_FANG = 3311;

    private static final int LETO_LIZARDMAN_CHARM = 3312;

    private static final int EN_GOLEM_HEARTSTONE = 3313;

    private static final int LARA_MEMO = 3314;

    private static final int NESTLE_MEMO = 3315;

    private static final int LEOPOLD_JOURNAL = 3316;

    private static final int AKLANTOTH_GEM_1 = 3317;

    private static final int AKLANTOTH_GEM_2 = 3318;

    private static final int AKLANTOTH_GEM_3 = 3319;

    private static final int AKLANTOTH_GEM_4 = 3320;

    private static final int AKLANTOTH_GEM_5 = 3321;

    private static final int AKLANTOTH_GEM_6 = 3322;

    private static final int BRIMSTONE_1 = 3323;

    private static final int ORIM_INSTRUCTIONS = 3324;

    private static final int ORIM_LETTER_1 = 3325;

    private static final int ORIM_LETTER_2 = 3326;

    private static final int SIR_VASPER_LETTER = 3327;

    private static final int VADIN_CRUCIFIX = 3328;

    private static final int TAMLIN_ORC_AMULET = 3329;

    private static final int VADIN_SANCTIONS = 3330;

    private static final int IKER_AMULET = 3331;

    private static final int SOULTRAP_CRYSTAL = 3332;

    private static final int PURGATORY_KEY = 3333;

    private static final int ZERUEL_BIND_CRYSTAL = 3334;

    private static final int BRIMSTONE_2 = 3335;

    private static final int SWORD_OF_BINDING = 3029;

    private static final int MARK_OF_WITCHCRAFT = 3307;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int LARA = 30063;

    private static final int ALEXANDRIA = 30098;

    private static final int IKER = 30110;

    private static final int VADIN = 30188;

    private static final int NESTLE = 30314;

    private static final int SIR_KLAUS_VASPER = 30417;

    private static final int LEOPOLD = 30435;

    private static final int KAIRA = 30476;

    private static final int ORIM = 30630;

    private static final int RODERIK = 30631;

    private static final int ENDRIGO = 30632;

    private static final int EVERT = 30633;

    private static final int DIRE_WYRM = 20557;

    private static final int ENCHANTED_STONE_GOLEM = 20565;

    private static final int LETO_LIZARDMAN = 20577;

    private static final int LETO_LIZARDMAN_ARCHER = 20578;

    private static final int LETO_LIZARDMAN_SOLDIER = 20579;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    private static final int LETO_LIZARDMAN_SHAMAN = 20581;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int TAMLIN_ORC = 20601;

    private static final int TAMLIN_ORC_ARCHER = 20602;

    private static final int NAMELESS_REVENANT = 27099;

    private static final int SKELETAL_MERCENARY = 27100;

    private static final int DREVANUL_PRINCE_ZERUEL = 27101;

    private static boolean _drevanulPrinceZeruel = false;

    private static boolean _swordOfBinding = false;

    public Q229_TestOfWitchcraft() {
        super(229, "Test Of Witchcraft");
        setItemsIds(3308, 3309, 3310, 3311, 3312, 3313, 3314, 3315, 3316, 3317,
                3318, 3319, 3320, 3321, 3322, 3323, 3324, 3325, 3326, 3327,
                3328, 3329, 3330, 3331, 3332, 3333, 3334, 3335, 3029);
        addStartNpc(30630);
        addTalkId(30063, 30098, 30110, 30188, 30314, 30417, 30435, 30476, 30630, 30631,
                30632, 30633);
        addAttackId(27099, 27100, 27101);
        addKillId(20557, 20565, 20577, 20578, 20579, 20580, 20581, 20582, 20601, 20602,
                27099, 27100, 27101);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q229_TestOfWitchcraft");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30630-08.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3308, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30630-08a.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30630-14.htm")) {
            st.set("cond", "4");
            st.unset("gem456");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3317, 1);
            st.takeItems(3318, 1);
            st.takeItems(3319, 1);
            st.takeItems(3320, 1);
            st.takeItems(3321, 1);
            st.takeItems(3322, 1);
            st.takeItems(3309, 1);
            st.giveItems(3323, 1);
            addSpawn(27101, 70381, 109638, -3726, 0, false, 120000L, true);
        } else if (event.equalsIgnoreCase("30630-16.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3323, 1);
            st.giveItems(3324, 1);
            st.giveItems(3325, 1);
            st.giveItems(3326, 1);
        } else if (event.equalsIgnoreCase("30630-22.htm")) {
            st.takeItems(3331, 1);
            st.takeItems(3324, 1);
            st.takeItems(3333, 1);
            st.takeItems(3029, 1);
            st.takeItems(3334, 1);
            st.giveItems(3307, 1);
            st.rewardExpAndSp(139796L, 40000);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30098-03.htm")) {
            st.set("cond", "2");
            st.set("gem456", "1");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3308, 1);
            st.giveItems(3309, 1);
        } else if (event.equalsIgnoreCase("30110-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3310, 1);
        } else if (event.equalsIgnoreCase("30110-08.htm")) {
            st.takeItems(3326, 1);
            st.giveItems(3331, 1);
            st.giveItems(3332, 1);
            if (st.hasQuestItems(3029)) {
                st.set("cond", "7");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30476-02.htm")) {
            st.giveItems(3318, 1);
            if (st.hasQuestItems(3317, 3319) && st.getInt("gem456") == 6) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30063-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3314, 1);
        } else if (event.equalsIgnoreCase("30314-02.htm")) {
            st.set("gem456", "2");
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3315, 1);
        } else if (event.equalsIgnoreCase("30435-02.htm")) {
            st.set("gem456", "3");
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3315, 1);
            st.giveItems(3316, 1);
        } else if (event.equalsIgnoreCase("30417-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3325, 1);
            st.giveItems(3327, 1);
        } else if (event.equalsIgnoreCase("30633-02.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(3335, 1);
            if (!_drevanulPrinceZeruel) {
                addSpawn(27101, 13395, 169807, -3708, 0, false, 299000L, true);
                _drevanulPrinceZeruel = true;
                startQuestTimer("zeruel_cleanup", 300000L, null, player, false);
            }
        } else {
            if (event.equalsIgnoreCase("zeruel_despawn")) {
                npc.abortAttack();
                npc.decayMe();
                return null;
            }
            if (event.equalsIgnoreCase("zeruel_cleanup")) {
                _drevanulPrinceZeruel = false;
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, gem456;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q229_TestOfWitchcraft");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.KNIGHT && player.getClassId() != ClassId.HUMAN_WIZARD && player.getClassId() != ClassId.PALUS_KNIGHT) {
                    htmltext = "30630-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30630-02.htm";
                    break;
                }
                htmltext = (player.getClassId() == ClassId.HUMAN_WIZARD) ? "30630-03.htm" : "30630-05.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                gem456 = st.getInt("gem456");
                switch (npc.getNpcId()) {
                    case 30630:
                        if (cond == 1) {
                            htmltext = "30630-09.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30630-10.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30630-11.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30630-14.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30630-15.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30630-17.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30630-18.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 8 || cond == 9) {
                            htmltext = "30630-18.htm";
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30630-19.htm";
                        break;
                    case 30098:
                        if (cond == 1) {
                            htmltext = "30098-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30098-04.htm";
                            break;
                        }
                        htmltext = "30098-05.htm";
                        break;
                    case 30476:
                        if (st.hasQuestItems(3318)) {
                            htmltext = "30476-03.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30476-01.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30476-04.htm";
                        break;
                    case 30110:
                        if (st.hasQuestItems(3317)) {
                            htmltext = "30110-06.htm";
                            break;
                        }
                        if (st.hasQuestItems(3310)) {
                            if (st.getQuestItemsCount(3311) + st.getQuestItemsCount(3312) + st.getQuestItemsCount(3313) < 60) {
                                htmltext = "30110-04.htm";
                                break;
                            }
                            htmltext = "30110-05.htm";
                            st.takeItems(3310, 1);
                            st.takeItems(3311, -1);
                            st.takeItems(3313, -1);
                            st.takeItems(3312, -1);
                            st.giveItems(3317, 1);
                            if (st.hasQuestItems(3318, 3319) && gem456 == 6) {
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30110-01.htm";
                            break;
                        }
                        if (cond == 6 && !st.hasQuestItems(3332)) {
                            htmltext = "30110-07.htm";
                            break;
                        }
                        if (cond >= 6 && cond < 10) {
                            htmltext = "30110-09.htm";
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30110-10.htm";
                        break;
                    case 30063:
                        if (st.hasQuestItems(3319)) {
                            htmltext = "30063-04.htm";
                            break;
                        }
                        if (st.hasQuestItems(3314)) {
                            htmltext = "30063-03.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30063-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30063-05.htm";
                        break;
                    case 30631:
                    case 30632:
                        if (st.hasAtLeastOneQuestItem(3314, 3319))
                            htmltext = npc.getNpcId() + "-01.htm";
                        break;
                    case 30314:
                        if (gem456 == 1) {
                            htmltext = "30314-01.htm";
                            break;
                        }
                        if (gem456 == 2) {
                            htmltext = "30314-03.htm";
                            break;
                        }
                        if (gem456 > 2)
                            htmltext = "30314-04.htm";
                        break;
                    case 30435:
                        if (gem456 == 2) {
                            htmltext = "30435-01.htm";
                            break;
                        }
                        if (gem456 > 2 && gem456 < 6) {
                            htmltext = "30435-03.htm";
                            break;
                        }
                        if (gem456 == 6) {
                            htmltext = "30435-04.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30435-05.htm";
                        break;
                    case 30417:
                        if (st.hasAtLeastOneQuestItem(3327, 3328)) {
                            htmltext = "30417-04.htm";
                            break;
                        }
                        if (st.hasQuestItems(3330)) {
                            htmltext = "30417-05.htm";
                            st.takeItems(3330, 1);
                            st.giveItems(3029, 1);
                            if (st.hasQuestItems(3332)) {
                                st.set("cond", "7");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30417-01.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "30417-06.htm";
                        break;
                    case 30188:
                        if (st.hasQuestItems(3327)) {
                            htmltext = "30188-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(3327, 1);
                            st.giveItems(3328, 1);
                            break;
                        }
                        if (st.hasQuestItems(3328)) {
                            if (st.getQuestItemsCount(3329) < 20) {
                                htmltext = "30188-02.htm";
                                break;
                            }
                            htmltext = "30188-03.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(3329, -1);
                            st.takeItems(3328, -1);
                            st.giveItems(3330, 1);
                            break;
                        }
                        if (st.hasQuestItems(3330)) {
                            htmltext = "30188-04.htm";
                            break;
                        }
                        if (cond > 6)
                            htmltext = "30188-05.htm";
                        break;
                    case 30633:
                        if (cond == 7 || cond == 8) {
                            htmltext = "30633-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30633-02.htm";
                            if (!_drevanulPrinceZeruel) {
                                addSpawn(27101, 13395, 169807, -3708, 0, false, 299000L, true);
                                _drevanulPrinceZeruel = true;
                                startQuestTimer("zeruel_cleanup", 300000L, null, player, false);
                            }
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30633-03.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 27099:
                if (st.hasQuestItems(3314) && !npc.isScriptValue(1)) {
                    npc.setScriptValue(1);
                    npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!");
                }
                break;
            case 27100:
                if (st.getInt("gem456") > 2 && st.getInt("gem456") < 6 && !npc.isScriptValue(1)) {
                    npc.setScriptValue(1);
                    npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!");
                }
                break;
            case 27101:
                if (cond == 4 && !npc.isScriptValue(1)) {
                    st.set("cond", "5");
                    st.playSound("ItemSound.quest_middle");
                    npc.setScriptValue(1);
                    npc.broadcastNpcSay("I'll take your lives later!!");
                    startQuestTimer("zeruel_despawn", 1000L, npc, player, false);
                    break;
                }
                if (cond == 9 && _drevanulPrinceZeruel) {
                    if (st.getItemEquipped(7) == 3029) {
                        _swordOfBinding = true;
                        if (!npc.isScriptValue(1)) {
                            npc.setScriptValue(1);
                            npc.broadcastNpcSay("That sword is really...!");
                        }
                        break;
                    }
                    _swordOfBinding = false;
                }
                break;
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        int gem456;
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20557:
                if (st.hasQuestItems(3310))
                    st.dropItemsAlways(3311, 1, 20);
                break;
            case 20565:
                if (st.hasQuestItems(3310))
                    st.dropItemsAlways(3313, 1, 20);
                break;
            case 20577:
            case 20578:
                if (st.hasQuestItems(3310))
                    st.dropItems(3312, 1, 20, 500000);
                break;
            case 20579:
            case 20580:
                if (st.hasQuestItems(3310))
                    st.dropItems(3312, 1, 20, 600000);
                break;
            case 20581:
            case 20582:
                if (st.hasQuestItems(3310))
                    st.dropItems(3312, 1, 20, 700000);
                break;
            case 27099:
                if (st.hasQuestItems(3314)) {
                    st.takeItems(3314, 1);
                    st.giveItems(3319, 1);
                    if (st.hasQuestItems(3317, 3318) && st.getInt("gem456") == 6) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 27100:
                gem456 = st.getInt("gem456");
                if (gem456 == 3) {
                    st.set("gem456", "4");
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(3320, 1);
                    break;
                }
                if (gem456 == 4) {
                    st.set("gem456", "5");
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(3321, 1);
                    break;
                }
                if (gem456 == 5) {
                    st.set("gem456", "6");
                    st.takeItems(3316, 1);
                    st.giveItems(3322, 1);
                    if (st.hasQuestItems(3317, 3318, 3319)) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20601:
            case 20602:
                if (st.hasQuestItems(3328))
                    st.dropItems(3329, 1, 20, 500000);
                break;
            case 27101:
                if (cond == 9 && _drevanulPrinceZeruel) {
                    if (_swordOfBinding) {
                        st.set("cond", "10");
                        st.playSound("ItemSound.quest_itemget");
                        st.takeItems(3335, 1);
                        st.takeItems(3332, 1);
                        st.giveItems(3333, 1);
                        st.giveItems(3334, 1);
                        npc.broadcastNpcSay("No! I haven't completely finished the command for destruction and slaughter yet!!!");
                    }
                    cancelQuestTimer("zeruel_cleanup", null, player);
                    _drevanulPrinceZeruel = false;
                }
                break;
        }
        return null;
    }
}
