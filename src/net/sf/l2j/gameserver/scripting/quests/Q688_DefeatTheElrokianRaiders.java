package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q688_DefeatTheElrokianRaiders extends Quest {
    private static final String qn = "Q688_DefeatTheElrokianRaiders";

    private static final int DINOSAUR_FANG_NECKLACE = 8785;

    private static final int DINN = 32105;

    private static final int ELROKI = 22214;

    public Q688_DefeatTheElrokianRaiders() {
        super(688, "Defeat the Elrokian Raiders!");
        setItemsIds(8785);
        addStartNpc(32105);
        addTalkId(32105);
        addKillId(22214);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q688_DefeatTheElrokianRaiders");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32105-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32105-08.htm")) {
            int count = st.getQuestItemsCount(8785);
            if (count > 0) {
                st.takeItems(8785, -1);
                st.rewardItems(57, count * 3000);
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("32105-06.htm")) {
            int count = st.getQuestItemsCount(8785);
            st.takeItems(8785, -1);
            st.rewardItems(57, count * 3000);
        } else if (event.equalsIgnoreCase("32105-07.htm")) {
            int count = st.getQuestItemsCount(8785);
            if (count >= 100) {
                st.takeItems(8785, 100);
                st.rewardItems(57, 450000);
            } else {
                htmltext = "32105-04.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q688_DefeatTheElrokianRaiders");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "32105-00.htm" : "32105-01.htm";
                break;
            case 1:
                htmltext = !st.hasQuestItems(8785) ? "32105-04.htm" : "32105-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8785, 1, 0, 500000);
        return null;
    }
}
