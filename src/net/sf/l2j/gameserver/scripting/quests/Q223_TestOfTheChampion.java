package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q223_TestOfTheChampion extends Quest {
    private static final String qn = "Q223_TestOfTheChampion";

    private static final int ASCALON_LETTER_1 = 3277;

    private static final int MASON_LETTER = 3278;

    private static final int IRON_ROSE_RING = 3279;

    private static final int ASCALON_LETTER_2 = 3280;

    private static final int WHITE_ROSE_INSIGNIA = 3281;

    private static final int GROOT_LETTER = 3282;

    private static final int ASCALON_LETTER_3 = 3283;

    private static final int MOUEN_ORDER_1 = 3284;

    private static final int MOUEN_ORDER_2 = 3285;

    private static final int MOUEN_LETTER = 3286;

    private static final int HARPY_EGG = 3287;

    private static final int MEDUSA_VENOM = 3288;

    private static final int WINDSUS_BILE = 3289;

    private static final int BLOODY_AXE_HEAD = 3290;

    private static final int ROAD_RATMAN_HEAD = 3291;

    private static final int LETO_LIZARDMAN_FANG = 3292;

    private static final int MARK_OF_CHAMPION = 3276;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int ASCALON = 30624;

    private static final int GROOT = 30093;

    private static final int MOUEN = 30196;

    private static final int MASON = 30625;

    private static final int HARPY = 20145;

    private static final int HARPY_MATRIARCH = 27088;

    private static final int MEDUSA = 20158;

    private static final int WINDSUS = 20553;

    private static final int ROAD_COLLECTOR = 27089;

    private static final int ROAD_SCAVENGER = 20551;

    private static final int LETO_LIZARDMAN = 20577;

    private static final int LETO_LIZARDMAN_ARCHER = 20578;

    private static final int LETO_LIZARDMAN_SOLDIER = 20579;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    private static final int LETO_LIZARDMAN_SHAMAN = 20581;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final int BLOODY_AXE_ELITE = 20780;

    public Q223_TestOfTheChampion() {
        super(223, "Test of the Champion");
        setItemsIds(3278, 3288, 3289, 3281, 3287, 3282, 3286, 3277, 3279, 3290,
                3280, 3283, 3284, 3291, 3285, 3292);
        addStartNpc(30624);
        addTalkId(30624, 30093, 30196, 30625);
        addAttackId(20145, 20551);
        addKillId(20145, 20158, 27088, 27089, 20551, 20553, 20577, 20578, 20579, 20580,
                20581, 20582, 20780);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q223_TestOfTheChampion");
        if (st == null)
            return htmltext;
        if (event.equals("30624-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3277, 1);
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30624-06a.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equals("30624-10.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3278, 1);
            st.giveItems(3280, 1);
        } else if (event.equals("30624-14.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3282, 1);
            st.giveItems(3283, 1);
        } else if (event.equals("30625-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3277, 1);
            st.giveItems(3279, 1);
        } else if (event.equals("30093-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3280, 1);
            st.giveItems(3281, 1);
        } else if (event.equals("30196-03.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3283, 1);
            st.giveItems(3284, 1);
        } else if (event.equals("30196-06.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3284, 1);
            st.takeItems(3291, 1);
            st.giveItems(3285, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        ClassId classId;
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q223_TestOfTheChampion");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                classId = player.getClassId();
                if (classId != ClassId.WARRIOR && classId != ClassId.ORC_RAIDER) {
                    htmltext = "30624-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30624-02.htm";
                    break;
                }
                htmltext = (classId == ClassId.WARRIOR) ? "30624-03.htm" : "30624-04.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30624:
                        if (cond == 1) {
                            htmltext = "30624-07.htm";
                            break;
                        }
                        if (cond < 4) {
                            htmltext = "30624-08.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30624-09.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30624-11.htm";
                            break;
                        }
                        if (cond > 5 && cond < 8) {
                            htmltext = "30624-12.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30624-13.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30624-15.htm";
                            break;
                        }
                        if (cond > 9 && cond < 14) {
                            htmltext = "30624-16.htm";
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30624-17.htm";
                            st.takeItems(3286, 1);
                            st.giveItems(3276, 1);
                            st.rewardExpAndSp(117454L, 25000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30625:
                        if (cond == 1) {
                            htmltext = "30625-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30625-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30625-05.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3290, -1);
                            st.takeItems(3279, 1);
                            st.giveItems(3278, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30625-06.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30625-07.htm";
                        break;
                    case 30093:
                        if (cond == 5) {
                            htmltext = "30093-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30093-03.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30093-04.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3281, 1);
                            st.takeItems(3287, -1);
                            st.takeItems(3288, -1);
                            st.takeItems(3289, -1);
                            st.giveItems(3282, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30093-05.htm";
                            break;
                        }
                        if (cond > 8)
                            htmltext = "30093-06.htm";
                        break;
                    case 30196:
                        if (cond == 9) {
                            htmltext = "30196-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30196-04.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30196-05.htm";
                            break;
                        }
                        if (cond == 12) {
                            htmltext = "30196-07.htm";
                            break;
                        }
                        if (cond == 13) {
                            htmltext = "30196-08.htm";
                            st.set("cond", "14");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3292, -1);
                            st.takeItems(3285, 1);
                            st.giveItems(3286, 1);
                            break;
                        }
                        if (cond > 13)
                            htmltext = "30196-09.htm";
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
        switch (npc.getNpcId()) {
            case 20145:
                if (st.getInt("cond") == 6 && Rnd.nextBoolean() && !npc.isScriptValue(1)) {
                    for (int i = 1; i < ((Rnd.get(10) < 7) ? 2 : 3); i++) {
                        Attackable collector = (Attackable) addSpawn(27088, npc, true, 0L, false);
                        collector.setRunning();
                        collector.addDamageHate(attacker, 0, 999);
                        collector.getAI().setIntention(IntentionType.ATTACK, attacker);
                    }
                    npc.setScriptValue(1);
                }
                break;
            case 20551:
                if (st.getInt("cond") == 10 && Rnd.nextBoolean() && !npc.isScriptValue(1)) {
                    for (int i = 1; i < ((Rnd.get(10) < 7) ? 2 : 3); i++) {
                        Attackable collector = (Attackable) addSpawn(27089, npc, true, 0L, false);
                        collector.setRunning();
                        collector.addDamageHate(attacker, 0, 999);
                        collector.getAI().setIntention(IntentionType.ATTACK, attacker);
                    }
                    npc.setScriptValue(1);
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
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 20780:
                if (st.getInt("cond") == 2 && st.dropItemsAlways(3290, 1, 100))
                    st.set("cond", "3");
                break;
            case 20145:
            case 27088:
                if (st.getInt("cond") == 6 && st.dropItems(3287, 1, 30, 500000) &&
                        st.getQuestItemsCount(3288) == 30 && st.getQuestItemsCount(3289) == 30)
                    st.set("cond", "7");
                break;
            case 20158:
                if (st.getInt("cond") == 6 && st.dropItems(3288, 1, 30, 500000) &&
                        st.getQuestItemsCount(3287) == 30 && st.getQuestItemsCount(3289) == 30)
                    st.set("cond", "7");
                break;
            case 20553:
                if (st.getInt("cond") == 6 && st.dropItems(3289, 1, 30, 500000) &&
                        st.getQuestItemsCount(3287) == 30 && st.getQuestItemsCount(3288) == 30)
                    st.set("cond", "7");
                break;
            case 20551:
            case 27089:
                if (st.getInt("cond") == 10 && st.dropItemsAlways(3291, 1, 100))
                    st.set("cond", "11");
                break;
            case 20577:
            case 20578:
            case 20579:
            case 20580:
            case 20581:
            case 20582:
                if (st.getInt("cond") == 12 && st.dropItems(3292, 1, 100, 500000 + 100000 * (npcId - 20577)))
                    st.set("cond", "13");
                break;
        }
        return null;
    }
}
