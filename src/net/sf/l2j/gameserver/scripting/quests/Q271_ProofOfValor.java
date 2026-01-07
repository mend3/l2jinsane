package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q271_ProofOfValor extends Quest {
    private static final String qn = "Q271_ProofOfValor";

    private static final int KASHA_WOLF_FANG = 1473;

    private static final int NECKLACE_OF_VALOR = 1507;

    private static final int NECKLACE_OF_COURAGE = 1506;

    public Q271_ProofOfValor() {
        super(271, "Proof of Valor");
        setItemsIds(1473);
        addStartNpc(30577);
        addTalkId(30577);
        addKillId(20475);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q271_ProofOfValor");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30577-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            if (st.hasAtLeastOneQuestItem(1506, 1507))
                htmltext = "30577-07.htm";
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q271_ProofOfValor");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30577-00.htm";
                    break;
                }
                if (player.getLevel() < 4) {
                    htmltext = "30577-01.htm";
                    break;
                }
                htmltext = st.hasAtLeastOneQuestItem(1506, 1507) ? "30577-06.htm" : "30577-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = st.hasAtLeastOneQuestItem(1506, 1507) ? "30577-07.htm" : "30577-04.htm";
                    break;
                }
                htmltext = "30577-05.htm";
                st.takeItems(1473, -1);
                st.giveItems((Rnd.get(100) < 10) ? 1507 : 1506, 1);
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
        if (st.dropItemsAlways(1473, (Rnd.get(4) == 0) ? 2 : 1, 50))
            st.set("cond", "2");
        return null;
    }
}
