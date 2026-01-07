package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q291_RevengeOfTheRedbonnet extends Quest {
    private static final String qn = "Q291_RevengeOfTheRedbonnet";

    private static final int BLACK_WOLF_PELT = 1482;

    private static final int SCROLL_OF_ESCAPE = 736;

    private static final int GRANDMA_PEARL = 1502;

    private static final int GRANDMA_MIRROR = 1503;

    private static final int GRANDMA_NECKLACE = 1504;

    private static final int GRANDMA_HAIRPIN = 1505;

    public Q291_RevengeOfTheRedbonnet() {
        super(291, "Revenge of the Redbonnet");
        setItemsIds(1482);
        addStartNpc(30553);
        addTalkId(30553);
        addKillId(20317);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q291_RevengeOfTheRedbonnet");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30553-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q291_RevengeOfTheRedbonnet");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 4) ? "30553-01.htm" : "30553-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30553-04.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30553-05.htm";
                    st.takeItems(1482, -1);
                    int random = Rnd.get(100);
                    if (random < 3) {
                        st.rewardItems(1502, 1);
                    } else if (random < 21) {
                        st.rewardItems(1503, 1);
                    } else if (random < 46) {
                        st.rewardItems(1504, 1);
                    } else {
                        st.rewardItems(736, 1);
                        st.rewardItems(1505, 1);
                    }
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(true);
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
        if (st.dropItemsAlways(1482, 1, 40))
            st.set("cond", "2");
        return null;
    }
}
