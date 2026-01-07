package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q337_AudienceWithTheLandDragon extends Quest {
    private static final String qn = "Q337_AudienceWithTheLandDragon";
    private static final int GABRIELLE = 30753;
    private static final int ORVEN = 30857;
    private static final int KENDRA = 30851;
    private static final int CHAKIRIS = 30705;
    private static final int KAIENA = 30720;
    private static final int MOKE = 30498;
    private static final int HELTON = 30678;
    private static final int GILMORE = 30754;
    private static final int THEODRIC = 30755;
    private static final int BLOOD_QUEEN = 18001;
    private static final int SACRIFICE_OF_THE_SACRIFICED = 27171;
    private static final int HARIT_LIZARDMAN_SHAMAN = 20644;
    private static final int HARIT_LIZARDMAN_MATRIARCH = 20645;
    private static final int HARIT_LIZARDMAN_ZEALOT = 27172;
    private static final int KRANROT = 20650;
    private static final int HAMRUT = 20649;
    private static final int MARSH_DRAKE = 20680;
    private static final int MARSH_STALKER = 20679;
    private static final int ABYSSAL_JEWEL_1 = 27165;
    private static final int JEWEL_GUARDIAN_MARA = 27168;
    private static final int ABYSSAL_JEWEL_2 = 27166;
    private static final int JEWEL_GUARDIAN_MUSFEL = 27169;
    private static final int CAVE_MAIDEN_1 = 20134;
    private static final int CAVE_MAIDEN_2 = 20287;
    private static final int CAVE_KEEPER_1 = 20246;
    private static final int CAVE_KEEPER_2 = 20277;
    private static final int ABYSSAL_JEWEL_3 = 27167;
    private static final int JEWEL_GUARDIAN_PYTON = 27170;
    private static final int FEATHER_OF_GABRIELLE = 3852;
    private static final int MARK_OF_WATCHMAN = 3864;
    private static final int REMAINS_OF_SACRIFIED = 3857;
    private static final int TOTEM_OF_LAND_DRAGON = 3858;
    private static final int KRANROT_SKIN = 3855;
    private static final int HAMRUT_LEG = 3856;
    private static final int MARSH_DRAKE_TALONS = 3854;
    private static final int MARSH_STALKER_HORN = 3853;
    private static final int FIRST_FRAGMENT_OF_ABYSS_JEWEL = 3859;
    private static final int MARA_FANG = 3862;
    private static final int SECOND_FRAGMENT_OF_ABYSS_JEWEL = 3860;
    private static final int MUSFEL_FANG = 3863;
    private static final int HERALD_OF_SLAYER = 3890;
    private static final int THIRD_FRAGMENT_OF_ABYSS_JEWEL = 3861;
    private static final int PORTAL_STONE = 3865;
    private static final int[][] DROPS_ON_KILL = new int[][]{{27171, 1, 1, 3857}, {27172, 1, 2, 3858}, {20650, 1, 3, 3855}, {20649, 1, 3, 3856}, {20680, 1, 4, 3854}, {20679, 1, 4, 3853}, {27168, 2, 5, 3862}, {27169, 2, 6, 3863}};
    private static final int[][] DROP_ON_ATTACK = new int[][]{{27165, 2, 5, 3859, 20, 27168}, {27166, 2, 6, 3860, 20, 27169}, {27167, 4, 7, 3861, 3, 27170}};
    private static boolean _jewel1 = false;
    private static boolean _jewel2 = false;
    private static boolean _jewel3 = false;

    public Q337_AudienceWithTheLandDragon() {
        super(337, "Audience with the Land Dragon");
        setItemsIds(3852, 3864, 3857, 3858, 3855, 3856, 3854, 3853, 3859, 3862,
                3860, 3863, 3890, 3861);
        addStartNpc(30753);
        addTalkId(30753, 30857, 30851, 30705, 30720, 30498, 30678, 30754, 30755);
        addAttackId(27165, 27166, 27167);
        addKillId(18001, 27171, 20644, 20645, 27172, 20650, 20649, 20680, 20679, 27168,
                27169, 20134, 20287, 20246, 20277, 27170);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q337_AudienceWithTheLandDragon");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30753-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("drop1", "1");
            st.set("drop2", "1");
            st.set("drop3", "1");
            st.set("drop4", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3852, 1);
        } else if (event.equalsIgnoreCase("30753-09.htm")) {
            if (st.getQuestItemsCount(3864) >= 4) {
                st.set("cond", "2");
                st.set("drop5", "2");
                st.set("drop6", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(3864, 4);
            } else {
                htmltext = null;
            }
        } else if (event.equalsIgnoreCase("30755-05.htm")) {
            if (st.hasQuestItems(3861)) {
                st.takeItems(3861, 1);
                st.takeItems(3890, 1);
                st.giveItems(3865, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q337_AudienceWithTheLandDragon");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 50) ? "30753-02.htm" : "30753-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30753:
                        if (cond == 1) {
                            htmltext = (st.getQuestItemsCount(3864) < 4) ? "30753-06.htm" : "30753-08.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (st.getQuestItemsCount(3864) < 2) {
                                htmltext = "30753-10.htm";
                                break;
                            }
                            htmltext = "30753-11.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(3852, 1);
                            st.takeItems(3864, 1);
                            st.giveItems(3890, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30753-12.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30753-13.htm";
                        break;
                    case 30857:
                        if (cond == 1) {
                            if (st.getInt("drop1") == 1) {
                                if (st.hasQuestItems(3857)) {
                                    htmltext = "30857-02.htm";
                                    st.unset("drop1");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(3857, 1);
                                    st.giveItems(3864, 1);
                                    break;
                                }
                                htmltext = "30857-01.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(3864) < 4) {
                                htmltext = "30857-03.htm";
                                break;
                            }
                            htmltext = "30857-04.htm";
                        }
                        break;
                    case 30851:
                        if (cond == 1) {
                            if (st.getInt("drop2") == 1) {
                                if (st.hasQuestItems(3858)) {
                                    htmltext = "30851-02.htm";
                                    st.unset("drop2");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(3858, 1);
                                    st.giveItems(3864, 1);
                                    break;
                                }
                                htmltext = "30851-01.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(3864) < 4) {
                                htmltext = "30851-03.htm";
                                break;
                            }
                            htmltext = "30851-04.htm";
                        }
                        break;
                    case 30705:
                        if (cond == 1) {
                            if (st.getInt("drop3") == 1) {
                                if (st.hasQuestItems(3855, 3856)) {
                                    htmltext = "30705-02.htm";
                                    st.unset("drop3");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(3855, 1);
                                    st.takeItems(3856, 1);
                                    st.giveItems(3864, 1);
                                    break;
                                }
                                htmltext = "30705-01.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(3864) < 4) {
                                htmltext = "30705-03.htm";
                                break;
                            }
                            htmltext = "30705-04.htm";
                        }
                        break;
                    case 30720:
                        if (cond == 1) {
                            if (st.getInt("drop4") == 1) {
                                if (st.hasQuestItems(3854, 3853)) {
                                    htmltext = "30720-02.htm";
                                    st.unset("drop4");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(3854, 1);
                                    st.takeItems(3853, 1);
                                    st.giveItems(3864, 1);
                                    break;
                                }
                                htmltext = "30720-01.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(3864) < 4) {
                                htmltext = "30720-03.htm";
                                break;
                            }
                            htmltext = "30720-04.htm";
                        }
                        break;
                    case 30498:
                        if (cond == 2)
                            switch (st.getInt("drop5")) {
                                case 2:
                                    htmltext = "30498-01.htm";
                                    st.set("drop5", "1");
                                    break;
                                case 1:
                                    if (st.hasQuestItems(3859, 3862)) {
                                        htmltext = "30498-03.htm";
                                        st.unset("drop5");
                                        st.playSound("ItemSound.quest_middle");
                                        st.takeItems(3859, 1);
                                        st.takeItems(3862, 1);
                                        st.giveItems(3864, 1);
                                        break;
                                    }
                                    htmltext = "30498-02.htm";
                                    break;
                                case 0:
                                    if (st.getQuestItemsCount(3864) < 2) {
                                        htmltext = "30498-04.htm";
                                        break;
                                    }
                                    htmltext = "30498-05.htm";
                                    break;
                            }
                        break;
                    case 30678:
                        if (cond == 2)
                            switch (st.getInt("drop6")) {
                                case 2:
                                    htmltext = "30678-01.htm";
                                    st.set("drop6", "1");
                                    break;
                                case 1:
                                    if (st.hasQuestItems(3860, 3863)) {
                                        htmltext = "30678-03.htm";
                                        st.unset("drop6");
                                        st.playSound("ItemSound.quest_middle");
                                        st.takeItems(3860, 1);
                                        st.takeItems(3863, 1);
                                        st.giveItems(3864, 1);
                                        break;
                                    }
                                    htmltext = "30678-02.htm";
                                    break;
                                case 0:
                                    if (st.getQuestItemsCount(3864) < 2) {
                                        htmltext = "30678-04.htm";
                                        break;
                                    }
                                    htmltext = "30678-05.htm";
                                    break;
                            }
                        break;
                    case 30754:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30754-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30754-02.htm";
                            st.set("cond", "4");
                            st.set("drop7", "1");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 4)
                            htmltext = !st.hasQuestItems(3861) ? "30754-04.htm" : "30754-05.htm";
                        break;
                    case 30755:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30755-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30755-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = !st.hasQuestItems(3861) ? "30755-03.htm" : "30755-04.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        int[][] arrayOfInt;
        int i;
        byte b;
        for (arrayOfInt = DROP_ON_ATTACK, i = arrayOfInt.length, b = 0; b < i; ) {
            int[] npcInfo = arrayOfInt[b];
            if (npcInfo[0] != npcId) {
                b++;
                continue;
            }
            if (npcInfo[1] != st.getInt("cond"))
                break;
            double percentHp = (npc.getCurrentHp() + damage) * 100.0D / npc.getMaxHp();
            if (percentHp < 33.0D) {
                if (Rnd.get(100) < 33 && st.getInt("drop" + npcInfo[2]) == 1) {
                    int itemId = npcInfo[3];
                    if (!st.hasQuestItems(itemId)) {
                        st.giveItems(itemId, 1);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
            }
            if (percentHp < 66.0D) {
                if (Rnd.get(100) < 33 && st.getInt("drop" + npcInfo[2]) == 1) {
                    boolean spawn;
                    if (npcId == 27167) {
                        spawn = _jewel3;
                    } else if (npcId == 27166) {
                        spawn = _jewel2;
                    } else {
                        spawn = _jewel1;
                    }
                    if (spawn) {
                        for (int j = 0; j < npcInfo[4]; j++) {
                            Npc mob = addSpawn(npcInfo[5], npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), npc.getHeading(), true, 60000L, false);
                            mob.setRunning();
                            ((Attackable) mob).addDamageHate(attacker, 0, 500);
                            mob.getAI().setIntention(IntentionType.ATTACK, attacker);
                        }
                        if (npcId == 27167) {
                            _jewel3 = false;
                            break;
                        }
                        if (npcId == 27166) {
                            _jewel2 = false;
                            break;
                        }
                        _jewel1 = false;
                    }
                }
                break;
            }
            if (percentHp > 90.0D) {
                if (npcId == 27167) {
                    _jewel3 = true;
                    break;
                }
                if (npcId == 27166) {
                    _jewel2 = true;
                    break;
                }
                _jewel1 = true;
            }
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        int[][] arrayOfInt;
        int i;
        byte b;
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 20649:
            case 20650:
            case 20679:
            case 20680:
            case 27168:
            case 27169:
            case 27171:
            case 27172:
                for (arrayOfInt = DROPS_ON_KILL, i = arrayOfInt.length, b = 0; b < i; ) {
                    int[] npcInfo = arrayOfInt[b];
                    if (npcInfo[0] != npcId) {
                        b++;
                        continue;
                    }
                    if (npcInfo[1] == cond && st.getInt("drop" + npcInfo[2]) == 1) {
                        int itemId = npcInfo[3];
                        if (!st.hasQuestItems(itemId)) {
                            st.giveItems(itemId, 1);
                            st.playSound("ItemSound.quest_itemget");
                        }
                    }
                }
                break;
            case 18001:
                if (cond == 1 && st.getInt("drop1") == 1 && !st.hasQuestItems(3857))
                    for (int j = 0; j < 8; j++)
                        addSpawn(27171, npc.getX() + Rnd.get(-100, 100), npc.getY() + Rnd.get(-100, 100), npc.getZ(), npc.getHeading(), true, 60000L, false);
                break;
            case 20644:
            case 20645:
                if (cond == 1 && Rnd.get(5) == 0 && st.getInt("drop2") == 1 && !st.hasQuestItems(3858))
                    for (int j = 0; j < 3; j++)
                        addSpawn(27172, npc.getX() + Rnd.get(-50, 50), npc.getY() + Rnd.get(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
                break;
            case 20134:
            case 20246:
            case 20277:
            case 20287:
                if (cond == 4 && Rnd.get(5) == 0 && !st.hasQuestItems(3861))
                    addSpawn(27167, npc.getX() + Rnd.get(-50, 50), npc.getY() + Rnd.get(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
                break;
        }
        return null;
    }
}
