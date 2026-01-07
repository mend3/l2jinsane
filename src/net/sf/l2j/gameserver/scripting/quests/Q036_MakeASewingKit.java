package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q036_MakeASewingKit extends Quest {
    private static final String qn = "Q036_MakeASewingKit";

    private static final int REINFORCED_STEEL = 7163;

    private static final int ARTISANS_FRAME = 1891;

    private static final int ORIHARUKON = 1893;

    private static final int SEWING_KIT = 7078;

    public Q036_MakeASewingKit() {
        super(36, "Make a Sewing Kit");
        setItemsIds(7163);
        addStartNpc(30847);
        addTalkId(30847);
        addKillId(20566);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q036_MakeASewingKit");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30847-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30847-3.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7163, 5);
        } else if (event.equalsIgnoreCase("30847-5.htm")) {
            if (st.getQuestItemsCount(1893) >= 10 && st.getQuestItemsCount(1891) >= 10) {
                st.takeItems(1891, 10);
                st.takeItems(1893, 10);
                st.giveItems(7078, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "30847-4a.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q036_MakeASewingKit");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 60) {
                    QuestState fwear = player.getQuestState("Q037_MakeFormalWear");
                    if (fwear != null && fwear.getInt("cond") == 6) {
                        htmltext = "30847-0.htm";
                        break;
                    }
                    htmltext = "30847-0a.htm";
                    break;
                }
                htmltext = "30847-0b.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30847-1a.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30847-2.htm";
                    break;
                }
                if (cond == 3)
                    htmltext = (st.getQuestItemsCount(1893) < 10 || st.getQuestItemsCount(1891) < 10) ? "30847-4a.htm" : "30847-4.htm";
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
        if (st.dropItems(7163, 1, 5, 500000))
            st.set("cond", "2");
        return null;
    }
}
