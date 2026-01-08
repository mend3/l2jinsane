package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q432_BirthdayPartySong extends Quest {
    private static final String qn = "Q432_BirthdayPartySong";

    private static final int OCTAVIA = 31043;

    private static final int RED_CRYSTAL = 7541;

    public Q432_BirthdayPartySong() {
        super(432, "Birthday Party Song");
        setItemsIds(7541);
        addStartNpc(31043);
        addTalkId(31043);
        addKillId(21103);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q432_BirthdayPartySong");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31043-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31043-06.htm")) {
            if (st.getQuestItemsCount(7541) == 50) {
                htmltext = "31043-05.htm";
                st.takeItems(7541, -1);
                st.rewardItems(7061, 25);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q432_BirthdayPartySong");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        htmltext = switch (st.getState()) {
            case 0 -> (player.getLevel() < 31) ? "31043-00.htm" : "31043-01.htm";
            case 1 -> (st.getQuestItemsCount(7541) < 50) ? "31043-03.htm" : "31043-04.htm";
            default -> htmltext;
        };
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(7541, 1, 50, 500000))
            st.set("cond", "2");
        return null;
    }
}
