package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q157_RecoverSmuggledGoods extends Quest {
    private static final String qn = "Q157_RecoverSmuggledGoods";

    private static final int ADAMANTITE_ORE = 1024;

    private static final int BUCKLER = 20;

    public Q157_RecoverSmuggledGoods() {
        super(157, "Recover Smuggled Goods");
        setItemsIds(1024);
        addStartNpc(30005);
        addTalkId(30005);
        addKillId(20121);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q157_RecoverSmuggledGoods");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30005-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q157_RecoverSmuggledGoods");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 5) ? "30005-02.htm" : "30005-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30005-06.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30005-07.htm";
                    st.takeItems(1024, -1);
                    st.giveItems(20, 1);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                }
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
        if (st.dropItems(1024, 1, 20, 400000))
            st.set("cond", "2");
        return null;
    }
}
