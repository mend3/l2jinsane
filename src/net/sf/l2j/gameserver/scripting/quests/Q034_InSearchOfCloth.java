package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q034_InSearchOfCloth extends Quest {
    private static final String qn = "Q034_InSearchOfCloth";

    private static final int RADIA = 30088;

    private static final int RALFORD = 30165;

    private static final int VARAN = 30294;

    private static final int TRISALIM_SPIDER = 20560;

    private static final int TRISALIM_TARANTULA = 20561;

    private static final int SPINNERET = 7528;

    private static final int SUEDE = 1866;

    private static final int THREAD = 1868;

    private static final int SPIDERSILK = 7161;

    private static final int MYSTERIOUS_CLOTH = 7076;

    public Q034_InSearchOfCloth() {
        super(34, "In Search of Cloth");
        setItemsIds(7528, 7161);
        addStartNpc(30088);
        addTalkId(30088, 30165, 30294);
        addKillId(20560, 20561);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q034_InSearchOfCloth");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30088-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30294-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30088-3.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30165-1.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30165-3.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7528, 10);
            st.giveItems(7161, 1);
        } else if (event.equalsIgnoreCase("30088-5.htm")) {
            if (st.getQuestItemsCount(1866) >= 3000 && st.getQuestItemsCount(1868) >= 5000 && st.hasQuestItems(7161)) {
                st.takeItems(7161, 1);
                st.takeItems(1866, 3000);
                st.takeItems(1868, 5000);
                st.giveItems(7076, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "30088-4a.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q034_InSearchOfCloth");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 60) {
                    QuestState fwear = player.getQuestState("Q037_MakeFormalWear");
                    if (fwear != null && fwear.getInt("cond") == 6) {
                        htmltext = "30088-0.htm";
                        break;
                    }
                    htmltext = "30088-0a.htm";
                    break;
                }
                htmltext = "30088-0b.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30088:
                        if (cond == 1) {
                            htmltext = "30088-1a.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30088-2.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30088-3a.htm";
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(1866) < 3000 || st.getQuestItemsCount(1868) < 5000 || !st.hasQuestItems(7161)) {
                                htmltext = "30088-4a.htm";
                                break;
                            }
                            htmltext = "30088-4.htm";
                        }
                        break;
                    case 30294:
                        if (cond == 1) {
                            htmltext = "30294-0.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30294-1a.htm";
                        break;
                    case 30165:
                        if (cond == 3) {
                            htmltext = "30165-0.htm";
                            break;
                        }
                        if (cond == 4 && st.getQuestItemsCount(7528) < 10) {
                            htmltext = "30165-1a.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30165-2.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30165-3a.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "4");
        if (st == null)
            return null;
        if (st.dropItems(7528, 1, 10, 500000))
            st.set("cond", "5");
        return null;
    }
}
