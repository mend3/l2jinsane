package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q417_PathToBecomeAScavenger extends Quest {
    private static final String qn = "Q417_PathToBecomeAScavenger";

    private static final int RING_OF_RAVEN = 1642;

    private static final int PIPPI_LETTER = 1643;

    private static final int RAUT_TELEPORT_SCROLL = 1644;

    private static final int SUCCUBUS_UNDIES = 1645;

    private static final int MION_LETTER = 1646;

    private static final int BRONK_INGOT = 1647;

    private static final int SHARI_AXE = 1648;

    private static final int ZIMENF_POTION = 1649;

    private static final int BRONK_PAY = 1650;

    private static final int SHARI_PAY = 1651;

    private static final int ZIMENF_PAY = 1652;

    private static final int BEAR_PICTURE = 1653;

    private static final int TARANTULA_PICTURE = 1654;

    private static final int HONEY_JAR = 1655;

    private static final int BEAD = 1656;

    private static final int BEAD_PARCEL_1 = 1657;

    private static final int BEAD_PARCEL_2 = 8543;

    private static final int RAUT = 30316;

    private static final int SHARI = 30517;

    private static final int MION = 30519;

    private static final int PIPPI = 30524;

    private static final int BRONK = 30525;

    private static final int ZIMENF = 30538;

    private static final int TOMA = 30556;

    private static final int TORAI = 30557;

    private static final int YASHENI = 31958;

    private static final int HUNTER_TARANTULA = 20403;

    private static final int PLUNDER_TARANTULA = 20508;

    private static final int HUNTER_BEAR = 20777;

    private static final int HONEY_BEAR = 27058;

    public Q417_PathToBecomeAScavenger() {
        super(417, "Path To Become A Scavenger");
        setItemsIds(1643, 1644, 1645, 1646, 1647, 1648, 1649, 1650, 1651, 1652,
                1653, 1654, 1655, 1656, 1657, 8543);
        addStartNpc(30524);
        addTalkId(30316, 30517, 30519, 30524, 30525, 30538, 30556, 30557, 31958);
        addKillId(20403, 20508, 20777, 27058);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q417_PathToBecomeAScavenger");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30524-05.htm")) {
            if (player.getClassId() != ClassId.DWARVEN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.SCAVENGER) ? "30524-02a.htm" : "30524-08.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30524-02.htm";
            } else if (st.hasQuestItems(1642)) {
                htmltext = "30524-04.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1643, 1);
            }
        } else if (event.equalsIgnoreCase("30519_1")) {
            int random = Rnd.get(3);
            htmltext = "30519-0" + random + 2 + ".htm";
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1643, -1);
            st.giveItems(1649 - random, 1);
        } else if (event.equalsIgnoreCase("30519_2")) {
            int random = Rnd.get(3);
            htmltext = "30519-0" + random + 2 + ".htm";
            st.takeItems(1650, -1);
            st.takeItems(1651, -1);
            st.takeItems(1652, -1);
            st.giveItems(1649 - random, 1);
        } else if (event.equalsIgnoreCase("30519-07.htm")) {
            st.set("id", String.valueOf(st.getInt("id") + 1));
        } else if (event.equalsIgnoreCase("30519-09.htm")) {
            int id = st.getInt("id");
            if (id / 10 < 2) {
                htmltext = "30519-07.htm";
                st.set("id", String.valueOf(id + 1));
            } else if (id / 10 == 2) {
                st.set("id", String.valueOf(id + 1));
            } else if (id / 10 >= 3) {
                htmltext = "30519-10.htm";
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(1648, -1);
                st.takeItems(1649, -1);
                st.takeItems(1647, -1);
                st.giveItems(1646, 1);
            }
        } else if (event.equalsIgnoreCase("30519-11.htm") && Rnd.nextBoolean()) {
            htmltext = "30519-06.htm";
        } else if (event.equalsIgnoreCase("30556-05b.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1656, -1);
            st.takeItems(1654, 1);
            st.giveItems(1657, 1);
        } else if (event.equalsIgnoreCase("30556-06b.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1656, -1);
            st.takeItems(1654, 1);
            st.giveItems(8543, 1);
        } else if (event.equalsIgnoreCase("30316-02.htm") || event.equalsIgnoreCase("30316-03.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1657, 1);
            st.giveItems(1644, 1);
        } else if (event.equalsIgnoreCase("30557-03.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1644, 1);
            st.giveItems(1645, 1);
        } else if (event.equalsIgnoreCase("31958-02.htm")) {
            st.takeItems(8543, 1);
            st.giveItems(1642, 1);
            st.rewardExpAndSp(3200L, 7080);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q417_PathToBecomeAScavenger");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30524-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30524:
                        if (cond == 1) {
                            htmltext = "30524-06.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30524-07.htm";
                        break;
                    case 30519:
                        if (st.hasQuestItems(1643)) {
                            htmltext = "30519-01.htm";
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1647, 1648, 1649)) {
                            int id = st.getInt("id");
                            if (id / 10 == 0) {
                                htmltext = "30519-05.htm";
                                break;
                            }
                            htmltext = "30519-08.htm";
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1650, 1651, 1652)) {
                            int id = st.getInt("id");
                            if (id < 50) {
                                htmltext = "30519-12.htm";
                                break;
                            }
                            htmltext = "30519-15.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1650, -1);
                            st.takeItems(1651, -1);
                            st.takeItems(1652, -1);
                            st.giveItems(1646, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30519-13.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30519-14.htm";
                        break;
                    case 30517:
                        if (st.hasQuestItems(1648)) {
                            int id = st.getInt("id");
                            if (id < 20) {
                                htmltext = "30517-01.htm";
                            } else {
                                htmltext = "30517-02.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.set("id", String.valueOf(id + 10));
                            st.takeItems(1648, 1);
                            st.giveItems(1651, 1);
                            break;
                        }
                        if (st.hasQuestItems(1651))
                            htmltext = "30517-03.htm";
                        break;
                    case 30525:
                        if (st.hasQuestItems(1647)) {
                            int id = st.getInt("id");
                            if (id < 20) {
                                htmltext = "30525-01.htm";
                            } else {
                                htmltext = "30525-02.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.set("id", String.valueOf(id + 10));
                            st.takeItems(1647, 1);
                            st.giveItems(1650, 1);
                            break;
                        }
                        if (st.hasQuestItems(1650))
                            htmltext = "30525-03.htm";
                        break;
                    case 30538:
                        if (st.hasQuestItems(1649)) {
                            int id = st.getInt("id");
                            if (id < 20) {
                                htmltext = "30538-01.htm";
                            } else {
                                htmltext = "30538-02.htm";
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                            }
                            st.set("id", String.valueOf(id + 10));
                            st.takeItems(1649, 1);
                            st.giveItems(1652, 1);
                            break;
                        }
                        if (st.hasQuestItems(1652))
                            htmltext = "30538-03.htm";
                        break;
                    case 30556:
                        if (cond == 4) {
                            htmltext = "30556-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1646, 1);
                            st.giveItems(1653, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30556-02.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30556-03.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1655, -1);
                            st.takeItems(1653, 1);
                            st.giveItems(1654, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30556-04.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30556-05a.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30556-06a.htm";
                            break;
                        }
                        if (cond == 10 || cond == 11) {
                            htmltext = "30556-07.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "30556-06c.htm";
                        break;
                    case 30316:
                        if (cond == 9) {
                            htmltext = "30316-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30316-04.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "30316-05.htm";
                            st.takeItems(1645, 1);
                            st.giveItems(1642, 1);
                            st.rewardExpAndSp(3200L, 7080);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30557:
                        if (cond == 10)
                            htmltext = "30557-01.htm";
                        break;
                    case 31958:
                        if (cond == 12)
                            htmltext = "31958-01.htm";
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
            case 20777:
                if (st.getInt("cond") == 5) {
                    int step = st.getInt("step");
                    if (step > 20) {
                        if ((step - 20) * 10 >= Rnd.get(100)) {
                            addSpawn(27058, npc, false, 300000L, true);
                            st.unset("step");
                            break;
                        }
                        st.set("step", String.valueOf(step + 1));
                        break;
                    }
                    st.set("step", String.valueOf(step + 1));
                }
                break;
            case 27058:
                if (st.getInt("cond") == 5 && ((Monster) npc).getSpoilerId() == player.getObjectId() && st.dropItemsAlways(1655, 1, 5))
                    st.set("cond", "6");
                break;
            case 20403:
            case 20508:
                if (st.getInt("cond") == 7 && ((Monster) npc).getSpoilerId() == player.getObjectId() && st.dropItems(1656, 1, 20, (npc.getNpcId() == 20403) ? 333333 : 600000))
                    st.set("cond", "8");
                break;
        }
        return null;
    }
}
