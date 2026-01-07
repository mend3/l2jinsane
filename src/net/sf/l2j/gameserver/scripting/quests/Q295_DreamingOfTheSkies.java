package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q295_DreamingOfTheSkies extends Quest {
    private static final String qn = "Q295_DreamingOfTheSkies";

    private static final int FLOATING_STONE = 1492;

    private static final int RING_OF_FIREFLY = 1509;

    public Q295_DreamingOfTheSkies() {
        super(295, "Dreaming of the Skies");
        setItemsIds(1492);
        addStartNpc(30536);
        addTalkId(30536);
        addKillId(20153);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q295_DreamingOfTheSkies");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30536-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q295_DreamingOfTheSkies");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 11) ? "30536-01.htm" : "30536-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30536-04.htm";
                    break;
                }
                st.takeItems(1492, -1);
                if (!st.hasQuestItems(1509)) {
                    htmltext = "30536-05.htm";
                    st.giveItems(1509, 1);
                } else {
                    htmltext = "30536-06.htm";
                    st.rewardItems(57, 2400);
                }
                st.rewardExpAndSp(0L, 500);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItemsAlways(1492, (Rnd.get(100) > 25) ? 1 : 2, 50))
            st.set("cond", "2");
        return null;
    }
}
