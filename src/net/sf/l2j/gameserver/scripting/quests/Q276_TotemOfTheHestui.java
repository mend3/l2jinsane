package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q276_TotemOfTheHestui extends Quest {
    private static final String qn = "Q276_TotemOfTheHestui";

    private static final int KASHA_PARASITE = 1480;

    private static final int KASHA_CRYSTAL = 1481;

    private static final int HESTUI_TOTEM = 1500;

    private static final int LEATHER_PANTS = 29;

    public Q276_TotemOfTheHestui() {
        super(276, "Totem of the Hestui");
        setItemsIds(1480, 1481);
        addStartNpc(30571);
        addTalkId(30571);
        addKillId(20479, 27044);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q276_TotemOfTheHestui");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30571-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q276_TotemOfTheHestui");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30571-00.htm";
                    break;
                }
                if (player.getLevel() < 15) {
                    htmltext = "30571-01.htm";
                    break;
                }
                htmltext = "30571-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30571-04.htm";
                    break;
                }
                htmltext = "30571-05.htm";
                st.takeItems(1481, -1);
                st.takeItems(1480, -1);
                st.giveItems(1500, 1);
                st.giveItems(29, 1);
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
        if (!st.hasQuestItems(1481)) {
            int count;
            int random;
            switch (npc.getNpcId()) {
                case 20479:
                    count = st.getQuestItemsCount(1480);
                    random = Rnd.get(100);
                    if (count >= 79 || (count >= 69 && random <= 20) || (count >= 59 && random <= 15) || (count >= 49 && random <= 10) || (count >= 39 && random < 2)) {
                        addSpawn(27044, npc, true, 0L, true);
                        st.takeItems(1480, count);
                        break;
                    }
                    st.dropItemsAlways(1480, 1, 0);
                    break;
                case 27044:
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1481, 1);
                    break;
            }
        }
        return null;
    }
}
