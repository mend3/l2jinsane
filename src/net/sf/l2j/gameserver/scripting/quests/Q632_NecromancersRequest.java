package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q632_NecromancersRequest extends Quest {
    private static final String qn = "Q632_NecromancersRequest";

    private static final int[] VAMPIRES = new int[]{
            21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591,
            21592, 21593, 21594, 21595};

    private static final int[] UNDEADS = new int[]{
            21547, 21548, 21549, 21551, 21552, 21555, 21556, 21562, 21571, 21576,
            21577, 21579};

    private static final int VAMPIRE_HEART = 7542;

    private static final int ZOMBIE_BRAIN = 7543;

    public Q632_NecromancersRequest() {
        super(632, "Necromancer's Request");
        setItemsIds(7542, 7543);
        addStartNpc(31522);
        addTalkId(31522);
        addKillId(VAMPIRES);
        addKillId(UNDEADS);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q632_NecromancersRequest");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31522-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31522-06.htm")) {
            if (st.getQuestItemsCount(7542) >= 200) {
                st.set("cond", "1");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7542, -1);
                st.rewardItems(57, 120000);
            } else {
                htmltext = "31522-09.htm";
            }
        } else if (event.equalsIgnoreCase("31522-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q632_NecromancersRequest");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 63) ? "31522-01.htm" : "31522-02.htm";
                break;
            case 1:
                htmltext = (st.getQuestItemsCount(7542) >= 200) ? "31522-05.htm" : "31522-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        for (int undead : UNDEADS) {
            if (undead == npc.getNpcId()) {
                st.dropItems(7543, 1, 0, 330000);
                return null;
            }
        }
        if (st.getInt("cond") == 1 && st.dropItems(7542, 1, 200, 500000))
            st.set("cond", "2");
        return null;
    }
}
