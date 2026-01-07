package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q227_TestOfTheReformer extends Quest {
    private static final String qn = "Q227_TestOfTheReformer";

    private static final int BOOK_OF_REFORM = 2822;

    private static final int LETTER_OF_INTRODUCTION = 2823;

    private static final int SLA_LETTER = 2824;

    private static final int GREETINGS = 2825;

    private static final int OL_MAHUM_MONEY = 2826;

    private static final int KATARI_LETTER = 2827;

    private static final int NYAKURI_LETTER = 2828;

    private static final int UNDEAD_LIST = 2829;

    private static final int RAMUS_LETTER = 2830;

    private static final int RIPPED_DIARY = 2831;

    private static final int HUGE_NAIL = 2832;

    private static final int LETTER_OF_BETRAYER = 2833;

    private static final int BONE_FRAGMENT_4 = 2834;

    private static final int BONE_FRAGMENT_5 = 2835;

    private static final int BONE_FRAGMENT_6 = 2836;

    private static final int BONE_FRAGMENT_7 = 2837;

    private static final int BONE_FRAGMENT_8 = 2838;

    private static final int BONE_FRAGMENT_9 = 2839;

    private static final int KAKAN_LETTER = 3037;

    private static final int MARK_OF_REFORMER = 2821;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int PUPINA = 30118;

    private static final int SLA = 30666;

    private static final int RAMUS = 30667;

    private static final int KATARI = 30668;

    private static final int KAKAN = 30669;

    private static final int NYAKURI = 30670;

    private static final int OL_MAHUM_PILGRIM = 30732;

    private static final int MISERY_SKELETON = 20022;

    private static final int SKELETON_ARCHER = 20100;

    private static final int SKELETON_MARKSMAN = 20102;

    private static final int SKELETON_LORD = 20104;

    private static final int SILENT_HORROR = 20404;

    private static final int NAMELESS_REVENANT = 27099;

    private static final int ARURAUNE = 27128;

    private static final int OL_MAHUM_INSPECTOR = 27129;

    private static final int OL_MAHUM_BETRAYER = 27130;

    private static final int CRIMSON_WEREWOLF = 27131;

    private static final int KRUDEL_LIZARDMAN = 27132;
    private static final int[] ALLOWED_SKILLS = new int[]{1031, 1069, 1164, 1168, 1147, 1177, 1184, 1201, 1206};
    private static long _timer;
    private static Npc _olMahumInspector;
    private static Npc _olMahumPilgrim;
    private static Npc _olMahumBetrayer;
    private static boolean _crimsonWerewolf = false;
    private static boolean _krudelLizardman = false;

    public Q227_TestOfTheReformer() {
        super(227, "Test Of The Reformer");
        setItemsIds(2822, 2823, 2824, 2825, 2826, 2827, 2828, 2829, 2830, 2831,
                2832, 2833, 2834, 2835, 2836, 2837, 2838, 2839, 3037);
        addStartNpc(30118);
        addTalkId(30118, 30666, 30667, 30668, 30669, 30670, 30732);
        addAttackId(27099, 27131);
        addKillId(20022, 20100, 20102, 20104, 20404, 27099, 27128, 27129, 27130, 27131,
                27132);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q227_TestOfTheReformer");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30118-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2822, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30118-04b.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30118-06.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2822, 1);
            st.takeItems(2832, 1);
            st.giveItems(2823, 1);
        } else if (event.equalsIgnoreCase("30666-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(2823, 1);
            st.giveItems(2824, 1);
        } else if (event.equalsIgnoreCase("30669-03.htm")) {
            if (st.getInt("cond") != 12) {
                st.set("cond", "12");
                st.playSound("ItemSound.quest_middle");
            }
            if (!_crimsonWerewolf) {
                addSpawn(27131, -9382, -89852, -2333, 0, false, 299000L, true);
                _crimsonWerewolf = true;
                startQuestTimer("werewolf_cleanup", 300000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("30670-03.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            if (!_krudelLizardman) {
                addSpawn(27132, 126019, -179983, -1781, 0, false, 299000L, true);
                _krudelLizardman = true;
                startQuestTimer("lizardman_cleanup", 300000L, null, player, false);
            }
        } else {
            if (event.equalsIgnoreCase("werewolf_despawn")) {
                npc.abortAttack();
                npc.broadcastNpcSay("Cowardly guy!");
                npc.decayMe();
                _crimsonWerewolf = false;
                cancelQuestTimer("werewolf_cleanup", null, player);
                return null;
            }
            if (event.equalsIgnoreCase("ol_mahums_despawn")) {
                _timer++;
                if (st.getInt("cond") == 8 || _timer >= 60L) {
                    if (_olMahumPilgrim != null) {
                        _olMahumPilgrim.deleteMe();
                        _olMahumPilgrim = null;
                    }
                    if (_olMahumInspector != null) {
                        _olMahumInspector.deleteMe();
                        _olMahumInspector = null;
                    }
                    cancelQuestTimer("ol_mahums_despawn", null, player);
                    _timer = 0L;
                }
                return null;
            }
            if (event.equalsIgnoreCase("betrayer_despawn")) {
                if (_olMahumBetrayer != null) {
                    _olMahumBetrayer.deleteMe();
                    _olMahumBetrayer = null;
                }
                return null;
            }
            if (event.equalsIgnoreCase("werewolf_cleanup")) {
                _crimsonWerewolf = false;
                return null;
            }
            if (event.equalsIgnoreCase("lizardman_cleanup")) {
                _krudelLizardman = false;
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q227_TestOfTheReformer");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() == ClassId.CLERIC || player.getClassId() == ClassId.SHILLIEN_ORACLE) {
                    htmltext = (player.getLevel() < 39) ? "30118-01.htm" : "30118-03.htm";
                    break;
                }
                htmltext = "30118-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30118:
                        if (cond < 3) {
                            htmltext = "30118-04a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30118-05.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30118-07.htm";
                        break;
                    case 30666:
                        if (cond == 4) {
                            htmltext = "30666-01.htm";
                            break;
                        }
                        if (cond > 4 && cond < 10) {
                            htmltext = "30666-05.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30666-06.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2826, 1);
                            st.giveItems(2825, 3);
                            break;
                        }
                        if (cond > 10 && cond < 20) {
                            htmltext = "30666-06.htm";
                            break;
                        }
                        if (cond == 20) {
                            htmltext = "30666-07.htm";
                            st.takeItems(2827, 1);
                            st.takeItems(3037, 1);
                            st.takeItems(2828, 1);
                            st.takeItems(2830, 1);
                            st.giveItems(2821, 1);
                            st.rewardExpAndSp(164032L, 17500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30668:
                        if (cond == 5 || cond == 6) {
                            htmltext = "30668-01.htm";
                            if (cond == 5) {
                                st.set("cond", "6");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(2824, 1);
                            }
                            if (_olMahumPilgrim == null && _olMahumInspector == null) {
                                _olMahumPilgrim = addSpawn(30732, -4015, 40141, -3664, 0, false, 0L, true);
                                _olMahumInspector = addSpawn(27129, -4034, 40201, -3665, 0, false, 0L, true);
                                startQuestTimer("ol_mahums_despawn", 5000L, null, player, true);
                                ((Attackable) _olMahumInspector).addDamageHate(_olMahumPilgrim, 0, 99999);
                                _olMahumInspector.getAI().setIntention(IntentionType.ATTACK, _olMahumPilgrim);
                            }
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30668-01.htm";
                            if (_olMahumPilgrim == null) {
                                _olMahumPilgrim = addSpawn(30732, -4015, 40141, -3664, 0, false, 0L, true);
                                startQuestTimer("ol_mahums_despawn", 5000L, null, player, true);
                            }
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30668-02.htm";
                            if (_olMahumBetrayer == null) {
                                _olMahumBetrayer = addSpawn(27130, -4106, 40174, -3660, 0, false, 0L, true);
                                _olMahumBetrayer.setRunning();
                                _olMahumBetrayer.getAI().setIntention(IntentionType.MOVE_TO, new Location(-7732, 36787, -3709));
                                startQuestTimer("betrayer_despawn", 40000L, null, player, false);
                            }
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30668-03.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2833, 1);
                            st.giveItems(2827, 1);
                            break;
                        }
                        if (cond > 9)
                            htmltext = "30668-04.htm";
                        break;
                    case 30732:
                        if (cond == 7) {
                            htmltext = "30732-01.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(2826, 1);
                        }
                        break;
                    case 30669:
                        if (cond == 11 || cond == 12) {
                            htmltext = "30669-01.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30669-04.htm";
                            st.set("cond", "14");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2825, 1);
                            st.giveItems(3037, 1);
                            break;
                        }
                        if (cond > 13)
                            htmltext = "30669-04.htm";
                        break;
                    case 30670:
                        if (cond == 14 || cond == 15) {
                            htmltext = "30670-01.htm";
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "30670-04.htm";
                            st.set("cond", "17");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2825, 1);
                            st.giveItems(2828, 1);
                            break;
                        }
                        if (cond > 16)
                            htmltext = "30670-04.htm";
                        break;
                    case 30667:
                        if (cond == 17) {
                            htmltext = "30667-01.htm";
                            st.set("cond", "18");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2825, 1);
                            st.giveItems(2829, 1);
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "30667-02.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "30667-03.htm";
                            st.set("cond", "20");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(2834, 1);
                            st.takeItems(2835, 1);
                            st.takeItems(2836, 1);
                            st.takeItems(2837, 1);
                            st.takeItems(2838, 1);
                            st.takeItems(2829, 1);
                            st.giveItems(2830, 1);
                            break;
                        }
                        if (cond > 19)
                            htmltext = "30667-03.htm";
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
                if ((cond == 1 || cond == 2) && skill != null && skill.getId() == 1031)
                    npc.setScriptValue(1);
                break;
            case 27131:
                if (cond == 12 && !npc.isScriptValue(1) && (skill == null || !ArraysUtil.contains(ALLOWED_SKILLS, skill.getId()))) {
                    npc.setScriptValue(1);
                    startQuestTimer("werewolf_despawn", 1000L, npc, player, false);
                }
                break;
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 27099:
                if ((cond == 1 || cond == 2) && npc.isScriptValue(1) && st.dropItemsAlways(2831, 1, 7)) {
                    st.set("cond", "2");
                    st.takeItems(2831, -1);
                    addSpawn(27128, npc, false, 300000L, true);
                }
                break;
            case 27128:
                if (cond == 2) {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2832, 1);
                    npc.broadcastNpcSay("The concealed truth will always be revealed...!");
                }
                break;
            case 27129:
                if (cond == 6) {
                    st.set("cond", "7");
                    st.playSound("ItemSound.quest_middle");
                }
                break;
            case 27130:
                if (cond == 8) {
                    st.set("cond", "9");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(2833, 1);
                    cancelQuestTimer("betrayer_despawn", null, player);
                    _olMahumBetrayer = null;
                }
                break;
            case 27131:
                if (cond == 12) {
                    st.set("cond", "13");
                    st.playSound("ItemSound.quest_middle");
                    cancelQuestTimer("werewolf_cleanup", null, player);
                    _crimsonWerewolf = false;
                }
                break;
            case 27132:
                if (cond == 15) {
                    st.set("cond", "16");
                    st.playSound("ItemSound.quest_middle");
                    cancelQuestTimer("lizardman_cleanup", null, player);
                    _krudelLizardman = false;
                }
                break;
            case 20404:
                if (cond == 18 && !st.hasQuestItems(2834)) {
                    st.giveItems(2834, 1);
                    if (st.hasQuestItems(2835, 2836, 2837, 2838)) {
                        st.set("cond", "19");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20104:
                if (cond == 18 && !st.hasQuestItems(2835)) {
                    st.giveItems(2835, 1);
                    if (st.hasQuestItems(2834, 2836, 2837, 2838)) {
                        st.set("cond", "19");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20102:
                if (cond == 18 && !st.hasQuestItems(2836)) {
                    st.giveItems(2836, 1);
                    if (st.hasQuestItems(2834, 2835, 2837, 2838)) {
                        st.set("cond", "19");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20022:
                if (cond == 18 && !st.hasQuestItems(2837)) {
                    st.giveItems(2837, 1);
                    if (st.hasQuestItems(2834, 2835, 2836, 2838)) {
                        st.set("cond", "19");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20100:
                if (cond == 18 && !st.hasQuestItems(2838)) {
                    st.giveItems(2838, 1);
                    if (st.hasQuestItems(2834, 2835, 2836, 2837)) {
                        st.set("cond", "19");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
        }
        return null;
    }
}
