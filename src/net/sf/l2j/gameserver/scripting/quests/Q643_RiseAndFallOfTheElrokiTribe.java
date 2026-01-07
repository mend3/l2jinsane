package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q643_RiseAndFallOfTheElrokiTribe extends Quest {
    private static final String qn = "Q643_RiseAndFallOfTheElrokiTribe";

    private static final int SINGSING = 32106;

    private static final int KARAKAWEI = 32117;

    private static final int BONES = 8776;

    public Q643_RiseAndFallOfTheElrokiTribe() {
        super(643, "Rise and Fall of the Elroki Tribe");
        setItemsIds(8776);
        addStartNpc(32106);
        addTalkId(32106, 32117);
        addKillId(22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q643_RiseAndFallOfTheElrokiTribe");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32106-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32106-07.htm")) {
            int count = st.getQuestItemsCount(8776);
            st.takeItems(8776, count);
            st.rewardItems(57, count * 1374);
        } else if (event.equalsIgnoreCase("32106-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("32117-03.htm")) {
            int count = st.getQuestItemsCount(8776);
            if (count >= 300) {
                st.takeItems(8776, 300);
                st.rewardItems(Rnd.get(8712, 8722), 5);
            } else {
                htmltext = "32117-04.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q643_RiseAndFallOfTheElrokiTribe");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "32106-00.htm" : "32106-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 32106:
                        htmltext = st.hasQuestItems(8776) ? "32106-06.htm" : "32106-05.htm";
                        break;
                    case 32117:
                        htmltext = "32117-01.htm";
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
        st.dropItems(8776, 1, 0, 750000);
        return null;
    }
}
