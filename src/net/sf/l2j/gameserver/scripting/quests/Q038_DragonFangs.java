package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q038_DragonFangs extends Quest {
    private static final String qn = "Q038_DragonFangs";

    private static final int FEATHER_ORNAMENT = 7173;

    private static final int TOOTH_OF_TOTEM = 7174;

    private static final int TOOTH_OF_DRAGON = 7175;

    private static final int LETTER_OF_IRIS = 7176;

    private static final int LETTER_OF_ROHMER = 7177;

    private static final int LUIS = 30386;

    private static final int IRIS = 30034;

    private static final int ROHMER = 30344;

    private static final int[][] REWARD = new int[][]{{45, 5200}, {627, 1500}, {1123, 3200}, {605, 3200}};

    private static final Map<Integer, int[]> DROPLIST = new HashMap<>();

    public Q038_DragonFangs() {
        super(38, "Dragon Fangs");
        DROPLIST.put(Integer.valueOf(21100), new int[]{1, 7173, 100, 1000000});
        DROPLIST.put(Integer.valueOf(20357), new int[]{1, 7173, 100, 1000000});
        DROPLIST.put(Integer.valueOf(21101), new int[]{6, 7175, 50, 500000});
        DROPLIST.put(Integer.valueOf(20356), new int[]{6, 7175, 50, 500000});
        setItemsIds(7173, 7174, 7175, 7176, 7177);
        addStartNpc(30386);
        addTalkId(30386, 30034, 30344);
        for (Iterator<Integer> iterator = DROPLIST.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q038_DragonFangs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30386-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30386-04.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7173, 100);
            st.giveItems(7174, 1);
        } else if (event.equalsIgnoreCase("30034-02a.htm")) {
            if (st.hasQuestItems(7174)) {
                htmltext = "30034-02.htm";
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7174, 1);
                st.giveItems(7176, 1);
            }
        } else if (event.equalsIgnoreCase("30344-02a.htm")) {
            if (st.hasQuestItems(7176)) {
                htmltext = "30344-02.htm";
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7176, 1);
                st.giveItems(7177, 1);
            }
        } else if (event.equalsIgnoreCase("30034-04a.htm")) {
            if (st.hasQuestItems(7177)) {
                htmltext = "30034-04.htm";
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7177, 1);
            }
        } else if (event.equalsIgnoreCase("30034-06a.htm")) {
            if (st.getQuestItemsCount(7175) >= 50) {
                int position = Rnd.get(REWARD.length);
                htmltext = "30034-06.htm";
                st.takeItems(7175, 50);
                st.giveItems(REWARD[position][0], 1);
                st.rewardItems(57, REWARD[position][1]);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q038_DragonFangs");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 19) ? "30386-01a.htm" : "30386-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30386:
                        if (cond == 1) {
                            htmltext = "30386-02a.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30386-03.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30386-03a.htm";
                        break;
                    case 30034:
                        if (cond == 3) {
                            htmltext = "30034-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30034-02b.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30034-03.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30034-05a.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30034-05.htm";
                        break;
                    case 30344:
                        if (cond == 4) {
                            htmltext = "30344-01.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30344-03.htm";
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int[] droplist = DROPLIST.get(Integer.valueOf(npc.getNpcId()));
        if (st.getInt("cond") == droplist[0] && st.dropItems(droplist[1], 1, droplist[2], droplist[3]))
            st.set("cond", String.valueOf(droplist[0] + 1));
        return null;
    }
}
