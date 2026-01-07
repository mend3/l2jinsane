package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q418_PathToAnArtisan extends Quest {
    private static final String qn = "Q418_PathToAnArtisan";

    private static final int SILVERA_RING = 1632;

    private static final int FIRST_PASS_CERTIFICATE = 1633;

    private static final int SECOND_PASS_CERTIFICATE = 1634;

    private static final int FINAL_PASS_CERTIFICATE = 1635;

    private static final int BOOGLE_RATMAN_TOOTH = 1636;

    private static final int BOOGLE_RATMAN_LEADER_TOOTH = 1637;

    private static final int KLUTO_LETTER = 1638;

    private static final int FOOTPRINT_OF_THIEF = 1639;

    private static final int STOLEN_SECRET_BOX = 1640;

    private static final int SECRET_BOX = 1641;

    private static final int SILVERA = 30527;

    private static final int KLUTO = 30317;

    private static final int PINTER = 30298;

    private static final int OBI = 32052;

    private static final int HITCHI = 31963;

    private static final int LOCKIRIN = 30531;

    private static final int RYDEL = 31956;

    public Q418_PathToAnArtisan() {
        super(418, "Path to an Artisan");
        setItemsIds(1632, 1633, 1634, 1636, 1637, 1638, 1639, 1640, 1641);
        addStartNpc(30527);
        addTalkId(30527, 30317, 30298, 32052, 31963, 30531, 31956);
        addKillId(20389, 20390, 20017);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q418_PathToAnArtisan");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30527-05.htm")) {
            if (player.getClassId() != ClassId.DWARVEN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ARTISAN) ? "30527-02a.htm" : "30527-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30527-03.htm";
            } else if (st.hasQuestItems(1635)) {
                htmltext = "30527-04.htm";
            }
        } else if (event.equalsIgnoreCase("30527-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1632, 1);
        } else if (event.equalsIgnoreCase("30527-08a.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1637, -1);
            st.takeItems(1636, -1);
            st.takeItems(1632, 1);
            st.giveItems(1633, 1);
        } else if (event.equalsIgnoreCase("30527-08b.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1637, -1);
            st.takeItems(1636, -1);
            st.takeItems(1632, 1);
        } else if (event.equalsIgnoreCase("30317-04.htm") || event.equalsIgnoreCase("30317-07.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1638, 1);
        } else if (event.equalsIgnoreCase("30317-10.htm")) {
            st.takeItems(1633, 1);
            st.takeItems(1634, 1);
            st.takeItems(1641, 1);
            st.giveItems(1635, 1);
            st.rewardExpAndSp(3200L, 6980);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30317-12.htm") || event.equalsIgnoreCase("30531-05.htm") || event.equalsIgnoreCase("32052-11.htm") || event.equalsIgnoreCase("31963-10.htm") || event.equalsIgnoreCase("31956-04.htm")) {
            st.takeItems(1633, 1);
            st.takeItems(1634, 1);
            st.takeItems(1641, 1);
            st.giveItems(1635, 1);
            st.rewardExpAndSp(3200L, 3490);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30298-03.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1638, -1);
            st.giveItems(1639, 1);
        } else if (event.equalsIgnoreCase("30298-06.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1639, -1);
            st.takeItems(1640, -1);
            st.giveItems(1634, 1);
            st.giveItems(1641, 1);
        } else if (event.equalsIgnoreCase("32052-06.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31963-04.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31963-05.htm")) {
            st.set("cond", "11");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31963-07.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q418_PathToAnArtisan");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30527-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30527:
                        if (cond == 1) {
                            htmltext = "30527-07.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30527-08.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30527-09.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30527-09a.htm";
                        break;
                    case 30317:
                        if (cond == 3) {
                            htmltext = "30317-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30317-08.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30317-09.htm";
                        break;
                    case 30298:
                        if (cond == 4) {
                            htmltext = "30298-01.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30298-04.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30298-05.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30298-07.htm";
                        break;
                    case 32052:
                        if (cond == 8) {
                            htmltext = "32052-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "32052-06a.htm";
                            break;
                        }
                        if (cond == 11)
                            htmltext = "32052-07.htm";
                        break;
                    case 31963:
                        if (cond == 9) {
                            htmltext = "31963-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "31963-04.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "31963-06a.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "31963-08.htm";
                        break;
                    case 30531:
                        if (cond == 10)
                            htmltext = "30531-01.htm";
                        break;
                    case 31956:
                        if (cond == 12)
                            htmltext = "31956-01.htm";
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
            case 20389:
                if (st.getInt("cond") == 1 && st.dropItems(1636, 1, 10, 700000) && st.getQuestItemsCount(1637) == 2)
                    st.set("cond", "2");
                break;
            case 20390:
                if (st.getInt("cond") == 1 && st.dropItems(1637, 1, 2, 500000) && st.getQuestItemsCount(1636) == 10)
                    st.set("cond", "2");
                break;
            case 20017:
                if (st.getInt("cond") == 5 && st.dropItems(1640, 1, 1, 200000))
                    st.set("cond", "6");
                break;
        }
        return null;
    }
}
