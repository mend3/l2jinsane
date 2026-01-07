package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q636_TruthBeyondTheGate extends Quest {
    private static final String qn = "Q636_TruthBeyondTheGate";

    private static final int ELIYAH = 31329;

    private static final int FLAURON = 32010;

    private static final int VISITOR_MARK = 8064;

    private static final int FADED_VISITOR_MARK = 8065;

    public Q636_TruthBeyondTheGate() {
        super(636, "The Truth Beyond the Gate");
        addStartNpc(31329);
        addTalkId(31329, 32010);
        addEnterZoneId(100000);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q636_TruthBeyondTheGate");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31329-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32010-02.htm")) {
            st.giveItems(8064, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q636_TruthBeyondTheGate");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31329-01.htm" : "31329-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31329:
                        htmltext = "31329-05.htm";
                        break;
                    case 32010:
                        htmltext = st.hasQuestItems(8064) ? "32010-03.htm" : "32010-01.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public final String onEnterZone(Creature character, ZoneType zone) {
        if (character instanceof Player)
            if (character.getActingPlayer().destroyItemByItemId("Mark", 8064, 1, character, false))
                character.getActingPlayer().addItem("Mark", 8065, 1, character, true);
        return null;
    }
}
