package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q345_MethodToRaiseTheDead extends Quest {
    private static final String qn = "Q345_MethodToRaiseTheDead";

    private static final int VICTIM_ARM_BONE = 4274;

    private static final int VICTIM_THIGH_BONE = 4275;

    private static final int VICTIM_SKULL = 4276;

    private static final int VICTIM_RIB_BONE = 4277;

    private static final int VICTIM_SPINE = 4278;

    private static final int USELESS_BONE_PIECES = 4280;

    private static final int POWDER_TO_SUMMON_DEAD_SOULS = 4281;

    private static final int XENOVIA = 30912;

    private static final int DOROTHY = 30970;

    private static final int ORPHEUS = 30971;

    private static final int MEDIUM_JAR = 30973;

    private static final int BILL_OF_IASON_HEINE = 4310;

    private static final int IMPERIAL_DIAMOND = 3456;

    public Q345_MethodToRaiseTheDead() {
        super(345, "Method to Raise the Dead");
        setItemsIds(4274, 4275, 4276, 4277, 4278, 4281, 4280);
        addStartNpc(30970);
        addTalkId(30970, 30912, 30973, 30971);
        addKillId(20789, 20791);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q345_MethodToRaiseTheDead");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30970-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30970-06.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30912-04.htm")) {
            if (player.getAdena() >= 1000) {
                htmltext = "30912-03.htm";
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(57, 1000);
                st.giveItems(4281, 1);
            }
        } else if (event.equalsIgnoreCase("30973-04.htm")) {
            if (st.getInt("cond") == 3) {
                int chance = Rnd.get(3);
                if (chance == 0) {
                    st.set("cond", "6");
                    htmltext = "30973-02a.htm";
                } else if (chance == 1) {
                    st.set("cond", "6");
                    htmltext = "30973-02b.htm";
                } else {
                    st.set("cond", "7");
                    htmltext = "30973-02c.htm";
                }
                st.takeItems(4281, -1);
                st.takeItems(4274, -1);
                st.takeItems(4275, -1);
                st.takeItems(4276, -1);
                st.takeItems(4277, -1);
                st.takeItems(4278, -1);
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("30971-02a.htm")) {
            if (st.hasQuestItems(4280))
                htmltext = "30971-02.htm";
        } else if (event.equalsIgnoreCase("30971-03.htm")) {
            if (st.hasQuestItems(4280)) {
                int amount = st.getQuestItemsCount(4280) * 104;
                st.takeItems(4280, -1);
                st.rewardItems(57, amount);
            } else {
                htmltext = "30971-02a.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, amount;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q345_MethodToRaiseTheDead");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 35) ? "30970-00.htm" : "30970-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30970:
                        if (cond == 1) {
                            htmltext = !st.hasQuestItems(4274, 4275, 4276, 4277, 4278) ? "30970-04.htm" : "30970-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30970-07.htm";
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "30970-08.htm";
                            break;
                        }
                        amount = st.getQuestItemsCount(4280) * 70;
                        st.takeItems(4280, -1);
                        if (cond == 7) {
                            htmltext = "30970-10.htm";
                            st.rewardItems(57, 3040 + amount);
                            if (Rnd.get(100) < 10) {
                                st.giveItems(3456, 1);
                            } else {
                                st.giveItems(4310, 5);
                            }
                        } else {
                            htmltext = "30970-09.htm";
                            st.rewardItems(57, 5390 + amount);
                            st.giveItems(4310, 3);
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    case 30912:
                        if (cond == 2) {
                            htmltext = "30912-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30912-06.htm";
                        break;
                    case 30973:
                        htmltext = "30973-01.htm";
                        break;
                    case 30971:
                        htmltext = "30971-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (Rnd.get(4) == 0) {
            int randomPart = Rnd.get(4274, 4278);
            if (!st.hasQuestItems(randomPart)) {
                st.playSound("ItemSound.quest_itemget");
                st.giveItems(randomPart, 1);
                return null;
            }
        }
        st.dropItemsAlways(4280, 1, 0);
        return null;
    }
}
