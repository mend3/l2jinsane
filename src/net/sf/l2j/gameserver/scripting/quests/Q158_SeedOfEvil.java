package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q158_SeedOfEvil extends Quest {
    private static final String qn = "Q158_SeedOfEvil";

    private static final int CLAY_TABLET = 1025;

    private static final int ENCHANT_ARMOR_D = 956;

    public Q158_SeedOfEvil() {
        super(158, "Seed of Evil");
        setItemsIds(1025);
        addStartNpc(30031);
        addTalkId(30031);
        addKillId(27016);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q158_SeedOfEvil");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30031-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q158_SeedOfEvil");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 21) ? "30031-02.htm" : "30031-03.htm";
                break;
            case 1:
                if (!st.hasQuestItems(1025)) {
                    htmltext = "30031-05.htm";
                    break;
                }
                htmltext = "30031-06.htm";
                st.takeItems(1025, 1);
                st.giveItems(956, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        st.set("cond", "2");
        st.playSound("ItemSound.quest_middle");
        st.giveItems(1025, 1);
        return null;
    }
}
