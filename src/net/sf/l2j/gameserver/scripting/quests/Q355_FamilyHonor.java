package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q355_FamilyHonor extends Quest {
    private static final String qn = "Q355_FamilyHonor";

    private static final int GALIBREDO = 30181;

    private static final int PATRIN = 30929;

    private static final int TIMAK_ORC_TROOP_LEADER = 20767;

    private static final int TIMAK_ORC_TROOP_SHAMAN = 20768;

    private static final int TIMAK_ORC_TROOP_WARRIOR = 20769;

    private static final int TIMAK_ORC_TROOP_ARCHER = 20770;

    private static final int GALIBREDO_BUST = 4252;

    private static final int WORK_OF_BERONA = 4350;

    private static final int STATUE_PROTOTYPE = 4351;

    private static final int STATUE_ORIGINAL = 4352;

    private static final int STATUE_REPLICA = 4353;

    private static final int STATUE_FORGERY = 4354;

    private static final Map<Integer, int[]> CHANCES = new HashMap<>();

    public Q355_FamilyHonor() {
        super(355, "Family Honor");
        CHANCES.put(20767, new int[]{44, 54});
        CHANCES.put(20768, new int[]{36, 45});
        CHANCES.put(20769, new int[]{35, 43});
        CHANCES.put(20770, new int[]{32, 42});
        setItemsIds(4252);
        addStartNpc(30181);
        addTalkId(30181, 30929);
        addKillId(20767, 20768, 20769, 20770);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q355_FamilyHonor");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30181-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30181-4b.htm")) {
            int count = st.getQuestItemsCount(4252);
            if (count > 0) {
                htmltext = "30181-4.htm";
                int reward = 2800 + count * 120;
                if (count >= 100) {
                    htmltext = "30181-4a.htm";
                    reward += 5000;
                }
                st.takeItems(4252, count);
                st.rewardItems(57, reward);
            }
        } else if (event.equalsIgnoreCase("30929-7.htm")) {
            if (st.hasQuestItems(4350)) {
                st.takeItems(4350, 1);
                int appraising = Rnd.get(100);
                if (appraising < 20) {
                    htmltext = "30929-2.htm";
                } else if (appraising < 40) {
                    htmltext = "30929-3.htm";
                    st.giveItems(4353, 1);
                } else if (appraising < 60) {
                    htmltext = "30929-4.htm";
                    st.giveItems(4352, 1);
                } else if (appraising < 80) {
                    htmltext = "30929-5.htm";
                    st.giveItems(4354, 1);
                } else {
                    htmltext = "30929-6.htm";
                    st.giveItems(4351, 1);
                }
            }
        } else if (event.equalsIgnoreCase("30181-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q355_FamilyHonor");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 36) ? "30181-0a.htm" : "30181-0.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30181:
                        htmltext = st.hasQuestItems(4252) ? "30181-3a.htm" : "30181-3.htm";
                        break;
                    case 30929:
                        htmltext = "30929-0.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int[] chances = CHANCES.get(npc.getNpcId());
        int random = Rnd.get(100);
        if (random < chances[1])
            st.dropItemsAlways((random < chances[0]) ? 4252 : 4350, 1, 0);
        return null;
    }
}
