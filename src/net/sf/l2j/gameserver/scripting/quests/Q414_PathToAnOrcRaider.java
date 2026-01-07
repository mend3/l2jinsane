package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q414_PathToAnOrcRaider extends Quest {
    private static final String qn = "Q414_PathToAnOrcRaider";

    private static final int GREEN_BLOOD = 1578;

    private static final int GOBLIN_DWELLING_MAP = 1579;

    private static final int KURUKA_RATMAN_TOOTH = 1580;

    private static final int BETRAYER_REPORT_1 = 1589;

    private static final int BETRAYER_REPORT_2 = 1590;

    private static final int HEAD_OF_BETRAYER = 1591;

    private static final int MARK_OF_RAIDER = 1592;

    private static final int TIMORA_ORC_HEAD = 8544;

    private static final int KARUKIA = 30570;

    private static final int KASMAN = 30501;

    private static final int TAZEER = 31978;

    private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;

    private static final int KURUKA_RATMAN_LEADER = 27045;

    private static final int UMBAR_ORC = 27054;

    private static final int TIMORA_ORC = 27320;

    public Q414_PathToAnOrcRaider() {
        super(414, "Path To An Orc Raider");
        setItemsIds(1578, 1579, 1580, 1589, 1590, 1591, 8544);
        addStartNpc(30570);
        addTalkId(30570, 30501, 31978);
        addKillId(20320, 27045, 27054, 27320);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q414_PathToAnOrcRaider");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30570-05.htm")) {
            if (player.getClassId() != ClassId.ORC_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ORC_RAIDER) ? "30570-02a.htm" : "30570-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30570-02.htm";
            } else if (st.hasQuestItems(1592)) {
                htmltext = "30570-04.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1579, 1);
            }
        } else if (event.equalsIgnoreCase("30570-07a.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1579, 1);
            st.takeItems(1580, -1);
            st.giveItems(1589, 1);
            st.giveItems(1590, 1);
        } else if (event.equalsIgnoreCase("30570-07b.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1579, 1);
            st.takeItems(1580, -1);
        } else if (event.equalsIgnoreCase("31978-03.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q414_PathToAnOrcRaider");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30570-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30570:
                        if (cond == 1) {
                            htmltext = "30570-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30570-07.htm";
                            break;
                        }
                        if (cond == 3 || cond == 4) {
                            htmltext = "30570-08.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30570-07b.htm";
                        break;
                    case 30501:
                        if (cond == 3) {
                            htmltext = "30501-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (st.getQuestItemsCount(1591) == 1) {
                                htmltext = "30501-02.htm";
                                break;
                            }
                            htmltext = "30501-03.htm";
                            st.takeItems(1589, 1);
                            st.takeItems(1590, 1);
                            st.takeItems(1591, -1);
                            st.giveItems(1592, 1);
                            st.rewardExpAndSp(3200L, 2360);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 31978:
                        if (cond == 5) {
                            htmltext = "31978-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "31978-04.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "31978-05.htm";
                            st.takeItems(8544, 1);
                            st.giveItems(1592, 1);
                            st.rewardExpAndSp(3200L, 2360);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
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
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20320:
                if (cond == 1) {
                    if (st.getQuestItemsCount(1578) <= Rnd.get(20)) {
                        st.playSound("ItemSound.quest_itemget");
                        st.giveItems(1578, 1);
                        break;
                    }
                    st.takeItems(1578, -1);
                    addSpawn(27045, npc, false, 300000L, true);
                }
                break;
            case 27045:
                if (cond == 1 && st.dropItemsAlways(1580, 1, 10))
                    st.set("cond", "2");
                break;
            case 27054:
                if ((cond == 3 || cond == 4) && st.getQuestItemsCount(1591) < 2 && Rnd.get(10) < 2) {
                    if (cond == 3)
                        st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1591, 1);
                }
                break;
            case 27320:
                if (cond == 6 && st.dropItems(8544, 1, 1, 600000))
                    st.set("cond", "7");
                break;
        }
        return null;
    }
}
