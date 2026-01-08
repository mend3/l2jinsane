package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q382_KailsMagicCoin extends Quest {
    private static final String qn = "Q382_KailsMagicCoin";

    private static final int FALLEN_ORC = 21017;

    private static final int FALLEN_ORC_ARCHER = 21019;

    private static final int FALLEN_ORC_SHAMAN = 21020;

    private static final int FALLEN_ORC_CAPTAIN = 21022;

    private static final int ROYAL_MEMBERSHIP = 5898;

    private static final int SILVER_BASILISK = 5961;

    private static final int GOLD_GOLEM = 5962;

    private static final int BLOOD_DRAGON = 5963;

    public Q382_KailsMagicCoin() {
        super(382, "Kail's Magic Coin");
        setItemsIds(5961, 5962, 5963);
        addStartNpc(30687);
        addTalkId(30687);
        addKillId(21017, 21019, 21020, 21022);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q382_KailsMagicCoin");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30687-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q382_KailsMagicCoin");
        if (st == null)
            return htmltext;
        htmltext = switch (st.getState()) {
            case 0 -> (player.getLevel() < 55 || !st.hasQuestItems(5898)) ? "30687-01.htm" : "30687-02.htm";
            case 1 -> "30687-04.htm";
            default -> htmltext;
        };
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 21017:
                st.dropItems(5961, 1, 0, 100000);
                break;
            case 21019:
                st.dropItems(5962, 1, 0, 100000);
                break;
            case 21020:
                st.dropItems(5963, 1, 0, 100000);
                break;
            case 21022:
                st.dropItems(5961 + Rnd.get(3), 1, 0, 100000);
                break;
        }
        return null;
    }
}
