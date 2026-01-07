package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q022_TragedyInVonHellmannForest extends Quest {
    private static final String qn = "Q022_TragedyInVonHellmannForest";

    private static final int WELL = 31527;

    private static final int TIFAREN = 31334;

    private static final int INNOCENTIN = 31328;

    private static final int GHOST_OF_PRIEST = 31528;

    private static final int GHOST_OF_ADVENTURER = 31529;

    private static final int CROSS_OF_EINHASAD = 7141;

    private static final int LOST_SKULL_OF_ELF = 7142;

    private static final int LETTER_OF_INNOCENTIN = 7143;

    private static final int GREEN_JEWEL_OF_ADVENTURER = 7144;

    private static final int RED_JEWEL_OF_ADVENTURER = 7145;

    private static final int SEALED_REPORT_BOX = 7146;

    private static final int REPORT_BOX = 7147;

    private static final int SOUL_OF_WELL = 27217;

    private Npc _ghostOfPriestInstance = null;

    private Npc _soulOfWellInstance = null;

    public Q022_TragedyInVonHellmannForest() {
        super(22, "Tragedy in von Hellmann Forest");
        setItemsIds(7142, 7147, 7146, 7143, 7145, 7144);
        addStartNpc(31334, 31328);
        addTalkId(31328, 31334, 31528, 31529, 31527);
        addAttackId(27217);
        addKillId(27217, 21553, 21554, 21555, 21556, 21561);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q022_TragedyInVonHellmannForest");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31334-03.htm")) {
            QuestState st2 = player.getQuestState("Q021_HiddenTruth");
            if (st2 != null && st2.isCompleted() && player.getLevel() >= 63)
                htmltext = "31334-02.htm";
        } else if (event.equalsIgnoreCase("31334-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31334-07.htm")) {
            if (!st.hasQuestItems(7141)) {
                st.set("cond", "2");
            } else {
                htmltext = "31334-06.htm";
            }
        } else if (event.equalsIgnoreCase("31334-08.htm")) {
            if (st.hasQuestItems(7141)) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7141, 1);
            } else {
                st.set("cond", "2");
                htmltext = "31334-07.htm";
            }
        } else if (event.equalsIgnoreCase("31334-13.htm")) {
            if (this._ghostOfPriestInstance != null) {
                st.set("cond", "6");
                htmltext = "31334-14.htm";
            } else {
                st.set("cond", "7");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7142, 1);
                this._ghostOfPriestInstance = addSpawn(31528, 38418, -49894, -1104, 0, false, 120000L, true);
                this._ghostOfPriestInstance.broadcastNpcSay("Did you call me, " + player.getName() + "?");
                startQuestTimer("ghost_cleanup", 118000L, null, player, false);
            }
        } else if (event.equalsIgnoreCase("31528-08.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            cancelQuestTimer("ghost_cleanup", null, player);
            if (this._ghostOfPriestInstance != null) {
                this._ghostOfPriestInstance.deleteMe();
                this._ghostOfPriestInstance = null;
            }
        } else if (event.equalsIgnoreCase("31328-10.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7143, 1);
        } else if (event.equalsIgnoreCase("31529-12.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7143, 1);
            st.giveItems(7144, 1);
        } else if (event.equalsIgnoreCase("31527-02.htm")) {
            if (this._soulOfWellInstance == null) {
                this._soulOfWellInstance = addSpawn(27217, 34860, -54542, -2048, 0, false, 0L, true);
                ((Attackable) this._soulOfWellInstance).addDamageHate(player, 0, 99999);
                this._soulOfWellInstance.getAI().setIntention(IntentionType.ATTACK, player, Boolean.valueOf(true));
            }
        } else if (event.equalsIgnoreCase("attack_timer")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7144, 1);
            st.giveItems(7145, 1);
        } else if (event.equalsIgnoreCase("31328-13.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7147, 1);
        } else if (event.equalsIgnoreCase("31328-21.htm")) {
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("ghost_cleanup")) {
            this._ghostOfPriestInstance.broadcastNpcSay("I'm confused! Maybe it's time to go back.");
            this._ghostOfPriestInstance = null;
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q022_TragedyInVonHellmannForest");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                switch (npc.getNpcId()) {
                    case 31328:
                        st2 = player.getQuestState("Q021_HiddenTruth");
                        if (st2 != null && st2.isCompleted()) {
                            if (!st.hasQuestItems(7141)) {
                                htmltext = "31328-01.htm";
                                st.giveItems(7141, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "31328-01b.htm";
                        }
                        break;
                    case 31334:
                        htmltext = "31334-01.htm";
                        break;
                }
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31334:
                        if (cond == 1 || cond == 2 || cond == 3) {
                            htmltext = "31334-05.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31334-09.htm";
                            break;
                        }
                        if (cond == 5 || cond == 6) {
                            if (st.hasQuestItems(7142)) {
                                htmltext = (this._ghostOfPriestInstance == null) ? "31334-10.htm" : "31334-11.htm";
                                break;
                            }
                            htmltext = "31334-09.htm";
                            st.set("cond", "4");
                            break;
                        }
                        if (cond == 7) {
                            htmltext = (this._ghostOfPriestInstance != null) ? "31334-15.htm" : "31334-17.htm";
                            break;
                        }
                        if (cond > 7)
                            htmltext = "31334-18.htm";
                        break;
                    case 31328:
                        if (cond < 3) {
                            if (!st.hasQuestItems(7141)) {
                                htmltext = "31328-01.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(7141, 1);
                                break;
                            }
                            htmltext = "31328-01b.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "31328-02.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "31328-03.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "31328-11.htm";
                            break;
                        }
                        if (cond == 14) {
                            if (st.hasQuestItems(7147)) {
                                htmltext = "31328-12.htm";
                                break;
                            }
                            st.set("cond", "13");
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "31328-14.htm";
                            break;
                        }
                        if (cond == 16) {
                            htmltext = (player.getLevel() < 64) ? "31328-23.htm" : "31328-22.htm";
                            st.exitQuest(false);
                            st.playSound("ItemSound.quest_finish");
                        }
                        break;
                    case 31528:
                        if (cond == 7) {
                            htmltext = "31528-01.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "31528-08.htm";
                        break;
                    case 31529:
                        if (cond == 9) {
                            if (st.hasQuestItems(7143)) {
                                htmltext = "31529-01.htm";
                                break;
                            }
                            htmltext = "31529-10.htm";
                            st.set("cond", "8");
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "31529-16.htm";
                            break;
                        }
                        if (cond == 11) {
                            if (st.hasQuestItems(7145)) {
                                htmltext = "31529-17.htm";
                                st.set("cond", "12");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(7145, 1);
                                break;
                            }
                            htmltext = "31529-09.htm";
                            st.set("cond", "10");
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "31529-17.htm";
                            break;
                        }
                        if (cond == 13) {
                            if (st.hasQuestItems(7146)) {
                                htmltext = "31529-18.htm";
                                st.set("cond", "14");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(7146, 1);
                                st.giveItems(7147, 1);
                                break;
                            }
                            htmltext = "31529-10.htm";
                            st.set("cond", "12");
                            break;
                        }
                        if (cond > 13)
                            htmltext = "31529-19.htm";
                        break;
                    case 31527:
                        if (cond == 10) {
                            htmltext = "31527-01.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "31527-03.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "31527-04.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(7146, 1);
                            break;
                        }
                        if (cond > 12)
                            htmltext = "31527-05.htm";
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
        QuestState st = player.getQuestState("Q022_TragedyInVonHellmannForest");
        if (st == null || !st.isStarted())
            return null;
        if (attacker instanceof net.sf.l2j.gameserver.model.actor.Summon)
            return null;
        if (getQuestTimer("attack_timer", null, player) != null)
            return null;
        if (st.getInt("cond") == 10)
            startQuestTimer("attack_timer", 20000L, null, player, false);
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (npc.getNpcId() != 27217) {
            if (st.getInt("cond") == 4 && st.dropItems(7142, 1, 1, 100000))
                st.set("cond", "5");
        } else {
            cancelQuestTimer("attack_timer", null, player);
            this._soulOfWellInstance = null;
        }
        return null;
    }
}
