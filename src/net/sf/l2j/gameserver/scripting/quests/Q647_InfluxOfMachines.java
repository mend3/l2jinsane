package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q647_InfluxOfMachines extends Quest {
    private static final String qn = "Q647_InfluxOfMachines";

    private static final int DESTROYED_GOLEM_SHARD = 8100;

    private static final int GUTENHAGEN = 32069;

    public Q647_InfluxOfMachines() {
        super(647, "Influx of Machines");
        setItemsIds(8100);
        addStartNpc(32069);
        addTalkId(32069);
        for (int i = 22052; i < 22079; i++) {
            addKillId(i);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q647_InfluxOfMachines");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32069-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32069-06.htm")) {
            st.takeItems(8100, -1);
            st.giveItems(Rnd.get(4963, 4972), 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q647_InfluxOfMachines");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 46) ? "32069-03.htm" : "32069-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32069-04.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32069-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(8100, 1, 500, 300000))
            st.set("cond", "2");
        return null;
    }
}
