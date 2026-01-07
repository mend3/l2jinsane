package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q371_ShriekOfGhosts extends Quest {
    private static final String qn = "Q371_ShriekOfGhosts";

    private static final int REVA = 30867;

    private static final int PATRIN = 30929;

    private static final int URN = 5903;

    private static final int PORCELAIN = 6002;

    private static final Map<Integer, int[]> CHANCES = new HashMap<>();

    public Q371_ShriekOfGhosts() {
        super(371, "Shriek of Ghosts");
        CHANCES.put(20818, new int[]{38, 43});
        CHANCES.put(20820, new int[]{48, 56});
        CHANCES.put(20824, new int[]{50, 58});
        setItemsIds(5903, 6002);
        addStartNpc(30867);
        addTalkId(30867, 30929);
        addKillId(20818, 20820, 20824);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q371_ShriekOfGhosts");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30867-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30867-07.htm")) {
            int urns = st.getQuestItemsCount(5903);
            if (urns > 0) {
                st.takeItems(5903, urns);
                if (urns >= 100) {
                    urns += 13;
                    htmltext = "30867-08.htm";
                } else {
                    urns += 7;
                }
                st.rewardItems(57, urns * 1000);
            }
        } else if (event.equalsIgnoreCase("30867-10.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("APPR")) {
            if (st.hasQuestItems(6002)) {
                int chance = Rnd.get(100);
                st.takeItems(6002, 1);
                if (chance < 2) {
                    st.giveItems(6003, 1);
                    htmltext = "30929-03.htm";
                } else if (chance < 32) {
                    st.giveItems(6004, 1);
                    htmltext = "30929-04.htm";
                } else if (chance < 62) {
                    st.giveItems(6005, 1);
                    htmltext = "30929-05.htm";
                } else if (chance < 77) {
                    st.giveItems(6006, 1);
                    htmltext = "30929-06.htm";
                } else {
                    htmltext = "30929-07.htm";
                }
            } else {
                htmltext = "30929-02.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q371_ShriekOfGhosts");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 59) ? "30867-01.htm" : "30867-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30867:
                        if (st.hasQuestItems(5903)) {
                            htmltext = st.hasQuestItems(6002) ? "30867-05.htm" : "30867-04.htm";
                            break;
                        }
                        htmltext = "30867-06.htm";
                        break;
                    case 30929:
                        htmltext = "30929-01.htm";
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
            st.dropItemsAlways((random < chances[0]) ? 5903 : 6002, 1, 0);
        return null;
    }
}
