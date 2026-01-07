package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q607_ProveYourCourage extends Quest {
    private static final String qn = "Q607_ProveYourCourage";

    private static final int HEAD_OF_SHADITH = 7235;

    private static final int TOTEM_OF_VALOR = 7219;

    private static final int KETRA_ALLIANCE_3 = 7213;

    public Q607_ProveYourCourage() {
        super(607, "Prove your courage!");
        setItemsIds(7235);
        addStartNpc(31370);
        addTalkId(31370);
        addKillId(25309);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q607_ProveYourCourage");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31370-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31370-07.htm")) {
            if (st.hasQuestItems(7235)) {
                st.takeItems(7235, -1);
                st.giveItems(7219, 1);
                st.rewardExpAndSp(10000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31370-06.htm";
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q607_ProveYourCourage");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 75) {
                    htmltext = "31370-03.htm";
                    break;
                }
                if (player.getAllianceWithVarkaKetra() >= 3 && st.hasQuestItems(7213) && !st.hasQuestItems(7219)) {
                    htmltext = "31370-01.htm";
                    break;
                }
                htmltext = "31370-02.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7235) ? "31370-05.htm" : "31370-06.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "1")) {
                if (st.getPlayer().getAllianceWithVarkaKetra() >= 3 && st.hasQuestItems(7213)) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(7235, 1);
                }
            }
        return null;
    }
}
