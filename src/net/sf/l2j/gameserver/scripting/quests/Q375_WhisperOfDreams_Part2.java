package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q375_WhisperOfDreams_Part2 extends Quest {
    private static final String qn = "Q375_WhisperOfDreams_Part2";

    private static final int MANAKIA = 30515;

    private static final int KARIK = 20629;

    private static final int CAVE_HOWLER = 20624;

    private static final int MYSTERIOUS_STONE = 5887;

    private static final int KARIK_HORN = 5888;

    private static final int CAVE_HOWLER_SKULL = 5889;

    private static final int[] REWARDS = new int[]{5348, 5350, 5352};

    public Q375_WhisperOfDreams_Part2() {
        super(375, "Whisper of Dreams, Part 2");
        setItemsIds(5888, 5889);
        addStartNpc(30515);
        addTalkId(30515);
        addKillId(20629, 20624);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q375_WhisperOfDreams_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30515-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.takeItems(5887, 1);
        } else if (event.equalsIgnoreCase("30515-07.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q375_WhisperOfDreams_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (!st.hasQuestItems(5887) || player.getLevel() < 60) ? "30515-01.htm" : "30515-02.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(5888) >= 100 && st.getQuestItemsCount(5889) >= 100) {
                    htmltext = "30515-05.htm";
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(5888, 100);
                    st.takeItems(5889, 100);
                    st.giveItems(Rnd.get(REWARDS), 1);
                    break;
                }
                htmltext = "30515-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20629:
                st.dropItemsAlways(5888, 1, 100);
                break;
            case 20624:
                st.dropItems(5889, 1, 100, 900000);
                break;
        }
        return null;
    }
}
