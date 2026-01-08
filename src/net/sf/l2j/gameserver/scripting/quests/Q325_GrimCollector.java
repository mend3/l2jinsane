package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.*;

public class Q325_GrimCollector extends Quest {
    private static final String qn = "Q325_GrimCollector";

    private static final int ANATOMY_DIAGRAM = 1349;

    private static final int ZOMBIE_HEAD = 1350;

    private static final int ZOMBIE_HEART = 1351;

    private static final int ZOMBIE_LIVER = 1352;

    private static final int SKULL = 1353;

    private static final int RIB_BONE = 1354;

    private static final int SPINE = 1355;

    private static final int ARM_BONE = 1356;

    private static final int THIGH_BONE = 1357;

    private static final int COMPLETE_SKELETON = 1358;

    private static final int CURTIS = 30336;

    private static final int VARSAK = 30342;

    private static final int SAMED = 30434;

    private static final Map<Integer, List<IntIntHolder>> DROPLIST = new HashMap<>();

    public Q325_GrimCollector() {
        super(325, "Grim Collector");
        DROPLIST.put(20026, Arrays.asList(new IntIntHolder(1350, 30), new IntIntHolder(1351, 50), new IntIntHolder(1352, 75)));
        DROPLIST.put(20029, Arrays.asList(new IntIntHolder(1350, 30), new IntIntHolder(1351, 52), new IntIntHolder(1352, 75)));
        DROPLIST.put(20035, Arrays.asList(new IntIntHolder(1353, 5), new IntIntHolder(1354, 15), new IntIntHolder(1355, 29), new IntIntHolder(1357, 79)));
        DROPLIST.put(20042, Arrays.asList(new IntIntHolder(1353, 6), new IntIntHolder(1354, 19), new IntIntHolder(1356, 69), new IntIntHolder(1357, 86)));
        DROPLIST.put(20045, Arrays.asList(new IntIntHolder(1353, 9), new IntIntHolder(1355, 59), new IntIntHolder(1356, 77), new IntIntHolder(1357, 97)));
        DROPLIST.put(20051, Arrays.asList(new IntIntHolder(1353, 9), new IntIntHolder(1354, 59), new IntIntHolder(1355, 79), new IntIntHolder(1356, 100)));
        DROPLIST.put(20457, Arrays.asList(new IntIntHolder(1350, 40), new IntIntHolder(1351, 60), new IntIntHolder(1352, 80)));
        DROPLIST.put(20458, Arrays.asList(new IntIntHolder(1350, 40), new IntIntHolder(1351, 70), new IntIntHolder(1352, 100)));
        DROPLIST.put(20514, Arrays.asList(new IntIntHolder(1353, 6), new IntIntHolder(1354, 21), new IntIntHolder(1355, 30), new IntIntHolder(1356, 31), new IntIntHolder(1357, 64)));
        DROPLIST.put(20515, Arrays.asList(new IntIntHolder(1353, 5), new IntIntHolder(1354, 20), new IntIntHolder(1355, 31), new IntIntHolder(1356, 33), new IntIntHolder(1357, 69)));
        setItemsIds(1350, 1351, 1352, 1353, 1354, 1355, 1356, 1357, 1358, 1349);
        addStartNpc(30336);
        addTalkId(30336, 30342, 30434);
        for (int npcId : DROPLIST.keySet()) {
            addKillId(npcId);
        }
    }

    private static int getNumberOfPieces(QuestState st) {
        return st.getQuestItemsCount(1350) + st.getQuestItemsCount(1355) + st.getQuestItemsCount(1356) + st.getQuestItemsCount(1351) + st.getQuestItemsCount(1352) + st.getQuestItemsCount(1353) + st.getQuestItemsCount(1354) + st.getQuestItemsCount(1357) + st.getQuestItemsCount(1358);
    }

    private static void payback(QuestState st) {
        int count = getNumberOfPieces(st);
        if (count > 0) {
            int reward = 30 * st.getQuestItemsCount(1350) + 20 * st.getQuestItemsCount(1351) + 20 * st.getQuestItemsCount(1352) + 100 * st.getQuestItemsCount(1353) + 40 * st.getQuestItemsCount(1354) + 14 * st.getQuestItemsCount(1355) + 14 * st.getQuestItemsCount(1356) + 14 * st.getQuestItemsCount(1357) + 341 * st.getQuestItemsCount(1358);
            if (count > 10)
                reward += 1629;
            if (st.hasQuestItems(1358))
                reward += 543;
            st.takeItems(1350, -1);
            st.takeItems(1351, -1);
            st.takeItems(1352, -1);
            st.takeItems(1353, -1);
            st.takeItems(1354, -1);
            st.takeItems(1355, -1);
            st.takeItems(1356, -1);
            st.takeItems(1357, -1);
            st.takeItems(1358, -1);
            st.rewardItems(57, reward);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q325_GrimCollector");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30336-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30434-03.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(1349, 1);
        } else if (event.equalsIgnoreCase("30434-06.htm")) {
            st.takeItems(1349, -1);
            payback(st);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30434-07.htm")) {
            payback(st);
        } else if (event.equalsIgnoreCase("30434-09.htm")) {
            int skeletons = st.getQuestItemsCount(1358);
            if (skeletons > 0) {
                st.playSound("ItemSound.quest_middle");
                st.takeItems(1358, -1);
                st.rewardItems(57, 543 + 341 * skeletons);
            }
        } else if (event.equalsIgnoreCase("30342-03.htm")) {
            if (!st.hasQuestItems(1355, 1356, 1353, 1354, 1357)) {
                htmltext = "30342-02.htm";
            } else {
                st.takeItems(1355, 1);
                st.takeItems(1353, 1);
                st.takeItems(1356, 1);
                st.takeItems(1354, 1);
                st.takeItems(1357, 1);
                if (Rnd.get(10) < 9) {
                    st.giveItems(1358, 1);
                } else {
                    htmltext = "30342-04.htm";
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q325_GrimCollector");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30336-01.htm" : "30336-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30336:
                        htmltext = !st.hasQuestItems(1349) ? "30336-04.htm" : "30336-05.htm";
                        break;
                    case 30434:
                        if (!st.hasQuestItems(1349)) {
                            htmltext = "30434-01.htm";
                            break;
                        }
                        if (getNumberOfPieces(st) == 0) {
                            htmltext = "30434-04.htm";
                            break;
                        }
                        htmltext = !st.hasQuestItems(1358) ? "30434-05.htm" : "30434-08.htm";
                        break;
                    case 30342:
                        htmltext = "30342-01.htm";
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
        if (st.hasQuestItems(1349)) {
            int chance = Rnd.get(100);
            for (IntIntHolder drop : DROPLIST.get(npc.getNpcId())) {
                if (chance < drop.getValue()) {
                    st.dropItemsAlways(drop.getId(), 1, 0);
                    break;
                }
            }
        }
        return null;
    }
}
