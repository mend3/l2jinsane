package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q609_MagicalPowerOfWater_Part1 extends Quest {
    private static final String qn = "Q609_MagicalPowerOfWater_Part1";

    private static final int WAHKAN = 31371;

    private static final int ASEFA = 31372;

    private static final int UDAN_BOX = 31561;

    private static final int EYE = 31685;

    private static final int THIEF_KEY = 1661;

    private static final int STOLEN_GREEN_TOTEM = 7237;

    private static final int GREEN_TOTEM = 7238;

    private static final int DIVINE_STONE = 7081;

    public Q609_MagicalPowerOfWater_Part1() {
        super(609, "Magical Power of Water - Part 1");
        setItemsIds(7237);
        addStartNpc(31371);
        addTalkId(31371, 31372, 31561);
        addAggroRangeEnterId(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362,
                21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374,
                21375);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q609_MagicalPowerOfWater_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31371-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("spawned", "0");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31561-03.htm")) {
            if (st.getInt("spawned") == 1) {
                htmltext = "31561-04.htm";
            } else if (!st.hasQuestItems(1661)) {
                htmltext = "31561-02.htm";
            } else {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1661, 1);
                st.giveItems(7237, 1);
            }
        } else if (event.equalsIgnoreCase("AsefaEyeDespawn")) {
            npc.broadcastNpcSay("I'll be waiting for your return.");
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q609_MagicalPowerOfWater_Part1");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() >= 74 && player.getAllianceWithVarkaKetra() >= 2) ? "31371-01.htm" : "31371-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31371:
                        htmltext = "31371-04.htm";
                        break;
                    case 31372:
                        if (cond == 1) {
                            htmltext = "31372-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 2) {
                            if (st.getInt("spawned") == 0) {
                                htmltext = "31372-02.htm";
                                break;
                            }
                            htmltext = "31372-03.htm";
                            st.set("spawned", "0");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 3 && st.hasQuestItems(7237)) {
                            htmltext = "31372-04.htm";
                            st.takeItems(7237, 1);
                            st.giveItems(7238, 1);
                            st.giveItems(7081, 1);
                            st.unset("spawned");
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 31561:
                        if (cond == 2) {
                            htmltext = "31561-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "31561-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        QuestState st = player.getQuestState("Q609_MagicalPowerOfWater_Part1");
        if (st == null)
            return null;
        if (st.getInt("spawned") == 0 && st.getInt("cond") == 2) {
            st.set("spawned", "1");
            Npc asefaEye = addSpawn(31685, player, true, 10000L, true);
            if (asefaEye != null) {
                startQuestTimer("AsefaEyeDespawn", 9000L, asefaEye, player, false);
                asefaEye.broadcastNpcSay("You cannot escape Asefa's Eye!");
                st.playSound("ItemSound.quest_giveup");
            }
        }
        return null;
    }
}
