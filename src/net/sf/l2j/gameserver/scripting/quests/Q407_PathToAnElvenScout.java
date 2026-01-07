package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q407_PathToAnElvenScout extends Quest {
    private static final String qn = "Q407_PathToAnElvenScout";

    private static final int REISA_LETTER = 1207;

    private static final int PRIAS_TORN_LETTER_1 = 1208;

    private static final int PRIAS_TORN_LETTER_2 = 1209;

    private static final int PRIAS_TORN_LETTER_3 = 1210;

    private static final int PRIAS_TORN_LETTER_4 = 1211;

    private static final int MORETTI_HERB = 1212;

    private static final int MORETTI_LETTER = 1214;

    private static final int PRIAS_LETTER = 1215;

    private static final int HONORARY_GUARD = 1216;

    private static final int REISA_RECOMMENDATION = 1217;

    private static final int RUSTED_KEY = 1293;

    private static final int REISA = 30328;

    private static final int BABENCO = 30334;

    private static final int MORETTI = 30337;

    private static final int PRIAS = 30426;

    public Q407_PathToAnElvenScout() {
        super(407, "Path to an Elven Scout");
        setItemsIds(1207, 1208, 1209, 1210, 1211, 1212, 1214, 1215, 1216, 1293);
        addStartNpc(30328);
        addTalkId(30328, 30337, 30334, 30426);
        addKillId(20053, 27031);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q407_PathToAnElvenScout");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30328-05.htm")) {
            if (player.getClassId() != ClassId.ELVEN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ELVEN_SCOUT) ? "30328-02a.htm" : "30328-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30328-03.htm";
            } else if (st.hasQuestItems(1217)) {
                htmltext = "30328-04.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.giveItems(1207, 1);
            }
        } else if (event.equalsIgnoreCase("30337-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1207, -1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q407_PathToAnElvenScout");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30328-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30328:
                        if (cond == 1) {
                            htmltext = "30328-06.htm";
                            break;
                        }
                        if (cond > 1 && cond < 8) {
                            htmltext = "30328-08.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30328-07.htm";
                            st.takeItems(1216, -1);
                            st.giveItems(1217, 1);
                            st.rewardExpAndSp(3200L, 1000);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30337:
                        if (cond == 1) {
                            htmltext = "30337-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = !st.hasQuestItems(1208) ? "30337-04.htm" : "30337-05.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30337-06.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1208, -1);
                            st.takeItems(1209, -1);
                            st.takeItems(1210, -1);
                            st.takeItems(1211, -1);
                            st.giveItems(1212, 1);
                            st.giveItems(1214, 1);
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30337-09.htm";
                            break;
                        }
                        if (cond == 7 && st.hasQuestItems(1215)) {
                            htmltext = "30337-07.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1215, -1);
                            st.giveItems(1216, 1);
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30337-08.htm";
                        break;
                    case 30334:
                        if (cond == 2)
                            htmltext = "30334-01.htm";
                        break;
                    case 30426:
                        if (cond == 4) {
                            htmltext = "30426-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30426-01.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30426-02.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1293, -1);
                            st.takeItems(1212, -1);
                            st.takeItems(1214, -1);
                            st.giveItems(1215, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30426-04.htm";
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
        if (npc.getNpcId() == 20053) {
            if (cond == 2)
                if (!st.hasQuestItems(1208)) {
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(1208, 1);
                } else if (!st.hasQuestItems(1209)) {
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(1209, 1);
                } else if (!st.hasQuestItems(1210)) {
                    st.playSound("ItemSound.quest_itemget");
                    st.giveItems(1210, 1);
                } else if (!st.hasQuestItems(1211)) {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1211, 1);
                }
        } else if ((cond == 4 || cond == 5) && st.dropItems(1293, 1, 1, 600000)) {
            st.set("cond", "6");
        }
        return null;
    }
}
