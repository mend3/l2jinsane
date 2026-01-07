package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q615_MagicalPowerOfFire_Part1 extends Quest {
    private static final String qn = "Q615_MagicalPowerOfFire_Part1";

    private static final int NARAN = 31378;

    private static final int UDAN = 31379;

    private static final int ASEFA_BOX = 31559;

    private static final int EYE = 31684;

    private static final int THIEF_KEY = 1661;

    private static final int STOLEN_RED_TOTEM = 7242;

    private static final int RED_TOTEM = 7243;

    private static final int DIVINE_STONE = 7081;

    public Q615_MagicalPowerOfFire_Part1() {
        super(615, "Magical Power of Fire - Part 1");
        setItemsIds(7242);
        addStartNpc(31378);
        addTalkId(31378, 31379, 31559);
        addAggroRangeEnterId(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362,
                21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374,
                21375);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q615_MagicalPowerOfFire_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31378-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("spawned", "0");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31559-03.htm")) {
            if (st.getInt("spawned") == 1) {
                htmltext = "31559-04.htm";
            } else if (!st.hasQuestItems(1661)) {
                htmltext = "31559-02.htm";
            } else {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1661, 1);
                st.giveItems(7242, 1);
            }
        } else if (event.equalsIgnoreCase("UdanEyeDespawn")) {
            npc.broadcastNpcSay("I'll be waiting for your return.");
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q615_MagicalPowerOfFire_Part1");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() >= 74 && player.getAllianceWithVarkaKetra() <= -2) ? "31378-01.htm" : "31378-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31378:
                        htmltext = "31378-04.htm";
                        break;
                    case 31379:
                        if (cond == 1) {
                            htmltext = "31379-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 2) {
                            if (st.getInt("spawned") == 0) {
                                htmltext = "31379-02.htm";
                                break;
                            }
                            htmltext = "31379-03.htm";
                            st.set("spawned", "0");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 3 && st.hasQuestItems(7242)) {
                            htmltext = "31379-04.htm";
                            st.takeItems(7242, 1);
                            st.giveItems(7243, 1);
                            st.giveItems(7081, 1);
                            st.unset("spawned");
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 31559:
                        if (cond == 2) {
                            htmltext = "31559-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "31559-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        QuestState st = player.getQuestState("Q615_MagicalPowerOfFire_Part1");
        if (st == null)
            return null;
        if (st.getInt("spawned") == 0 && st.getInt("cond") == 2) {
            st.set("spawned", "1");
            Npc udanEye = addSpawn(31684, player, true, 10000L, true);
            if (udanEye != null) {
                startQuestTimer("UdanEyeDespawn", 9000L, udanEye, player, false);
                udanEye.broadcastNpcSay("You cannot escape Udan's Eye!");
                st.playSound("ItemSound.quest_giveup");
            }
        }
        return null;
    }
}
