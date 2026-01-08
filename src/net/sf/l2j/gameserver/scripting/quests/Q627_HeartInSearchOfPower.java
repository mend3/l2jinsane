package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q627_HeartInSearchOfPower extends Quest {
    private static final String qn = "Q627_HeartInSearchOfPower";

    private static final int NECROMANCER = 31518;

    private static final int ENFEUX = 31519;

    private static final int SEAL_OF_LIGHT = 7170;

    private static final int BEAD_OF_OBEDIENCE = 7171;

    private static final int GEM_OF_SAINTS = 7172;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final Map<String, int[]> REWARDS = new HashMap<>();

    public Q627_HeartInSearchOfPower() {
        super(627, "Heart in Search of Power");
        CHANCES.put(21520, 550000);
        CHANCES.put(21523, 584000);
        CHANCES.put(21524, 621000);
        CHANCES.put(21525, 621000);
        CHANCES.put(21526, 606000);
        CHANCES.put(21529, 625000);
        CHANCES.put(21530, 578000);
        CHANCES.put(21531, 690000);
        CHANCES.put(21532, 671000);
        CHANCES.put(21535, 693000);
        CHANCES.put(21536, 615000);
        CHANCES.put(21539, 762000);
        CHANCES.put(21540, 762000);
        CHANCES.put(21658, 690000);
        REWARDS.put("adena", new int[]{0, 0, 100000});
        REWARDS.put("asofe", new int[]{4043, 13, 6400});
        REWARDS.put("thon", new int[]{4044, 13, 6400});
        REWARDS.put("enria", new int[]{4042, 6, 13600});
        REWARDS.put("mold", new int[]{4041, 3, 17200});
        setItemsIds(7171);
        addStartNpc(31518);
        addTalkId(31518, 31519);
        for (int npcId : CHANCES.keySet()) {
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q627_HeartInSearchOfPower");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31518-01.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31518-03.htm")) {
            if (st.getQuestItemsCount(7171) == 300) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7171, -1);
                st.giveItems(7170, 1);
            } else {
                htmltext = "31518-03a.htm";
                st.set("cond", "1");
                st.takeItems(7171, -1);
            }
        } else if (event.equalsIgnoreCase("31519-01.htm")) {
            if (st.getQuestItemsCount(7170) == 1) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7170, 1);
                st.giveItems(7172, 1);
            }
        } else if (REWARDS.containsKey(event)) {
            if (st.getQuestItemsCount(7172) == 1) {
                htmltext = "31518-07.htm";
                st.takeItems(7172, 1);
                if (((int[]) REWARDS.get(event))[0] > 0)
                    st.giveItems(((int[]) REWARDS.get(event))[0], ((int[]) REWARDS.get(event))[1]);
                st.rewardItems(57, ((int[]) REWARDS.get(event))[2]);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31518-7.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q627_HeartInSearchOfPower");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "31518-00a.htm" : "31518-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31518:
                        if (cond == 1) {
                            htmltext = "31518-01a.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31518-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "31518-04.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "31518-05.htm";
                        break;
                    case 31519:
                        if (cond == 3) {
                            htmltext = "31519-00.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "31519-02.htm";
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
        if (st.dropItems(7171, 1, 300, CHANCES.get(npc.getNpcId())))
            st.set("cond", "2");
        return null;
    }
}
