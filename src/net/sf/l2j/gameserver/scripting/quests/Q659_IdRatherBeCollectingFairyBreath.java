package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q659_IdRatherBeCollectingFairyBreath extends Quest {
    private static final String qn = "Q659_IdRatherBeCollectingFairyBreath";

    private static final int GALATEA = 30634;

    private static final int FAIRY_BREATH = 8286;

    private static final int SOBBING_WIND = 21023;

    private static final int BABBLING_WIND = 21024;

    private static final int GIGGLING_WIND = 21025;

    public Q659_IdRatherBeCollectingFairyBreath() {
        super(659, "I'd Rather Be Collecting Fairy Breath");
        setItemsIds(8286);
        addStartNpc(30634);
        addTalkId(30634);
        addKillId(21025, 21024, 21023);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q659_IdRatherBeCollectingFairyBreath");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30634-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30634-06.htm")) {
            int count = st.getQuestItemsCount(8286);
            if (count > 0) {
                st.takeItems(8286, count);
                if (count < 10) {
                    st.rewardItems(57, count * 50);
                } else {
                    st.rewardItems(57, count * 50 + 5365);
                }
            }
        } else if (event.equalsIgnoreCase("30634-08.htm")) {
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q659_IdRatherBeCollectingFairyBreath");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 26) ? "30634-01.htm" : "30634-02.htm";
                break;
            case 1:
                htmltext = !st.hasQuestItems(8286) ? "30634-04.htm" : "30634-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8286, 1, 0, 900000);
        return null;
    }
}
