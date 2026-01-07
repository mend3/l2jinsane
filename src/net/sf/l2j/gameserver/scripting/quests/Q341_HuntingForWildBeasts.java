package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q341_HuntingForWildBeasts extends Quest {
    private static final String qn = "Q341_HuntingForWildBeasts";

    private static final int BEAR_SKIN = 4259;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q341_HuntingForWildBeasts() {
        super(341, "Hunting for Wild Beasts");
        CHANCES.put(Integer.valueOf(20021), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20203), Integer.valueOf(900000));
        CHANCES.put(Integer.valueOf(20310), Integer.valueOf(500000));
        CHANCES.put(Integer.valueOf(20335), Integer.valueOf(700000));
        setItemsIds(4259);
        addStartNpc(30078);
        addTalkId(30078);
        addKillId(20021, 20203, 20310, 20335);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q341_HuntingForWildBeasts");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30078-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q341_HuntingForWildBeasts");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "30078-00.htm" : "30078-01.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(4259) < 20) {
                    htmltext = "30078-03.htm";
                    break;
                }
                htmltext = "30078-04.htm";
                st.takeItems(4259, -1);
                st.rewardItems(57, 3710);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(4259, 1, 20, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
