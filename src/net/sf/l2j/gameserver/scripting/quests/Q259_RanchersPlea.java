package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q259_RanchersPlea extends Quest {
    private static final String qn = "Q259_RanchersPlea";

    private static final int EDMOND = 30497;

    private static final int MARIUS = 30405;

    private static final int GIANT_SPIDER = 20103;

    private static final int TALON_SPIDER = 20106;

    private static final int BLADE_SPIDER = 20108;

    private static final int GIANT_SPIDER_SKIN = 1495;

    private static final int ADENA = 57;

    private static final int HEALING_POTION = 1061;

    private static final int WOODEN_ARROW = 17;

    public Q259_RanchersPlea() {
        super(259, "Rancher's Plea");
        setItemsIds(1495);
        addStartNpc(30497);
        addTalkId(30497, 30405);
        addKillId(20103, 20106, 20108);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q259_RanchersPlea");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30497-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30497-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30405-04.htm")) {
            if (st.getQuestItemsCount(1495) >= 10) {
                st.takeItems(1495, 10);
                st.rewardItems(1061, 1);
            } else {
                htmltext = "<html><body>Incorrect item count</body></html>";
            }
        } else if (event.equalsIgnoreCase("30405-05.htm")) {
            if (st.getQuestItemsCount(1495) >= 10) {
                st.takeItems(1495, 10);
                st.rewardItems(17, 50);
            } else {
                htmltext = "<html><body>Incorrect item count</body></html>";
            }
        } else if (event.equalsIgnoreCase("30405-07.htm")) {
            if (st.getQuestItemsCount(1495) >= 10)
                htmltext = "30405-06.htm";
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q259_RanchersPlea");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30497-01.htm" : "30497-02.htm";
                break;
            case 1:
                count = st.getQuestItemsCount(1495);
                switch (npc.getNpcId()) {
                    case 30497:
                        if (count == 0) {
                            htmltext = "30497-04.htm";
                            break;
                        }
                        htmltext = "30497-05.htm";
                        st.takeItems(1495, -1);
                        st.rewardItems(57, ((count >= 10) ? 250 : 0) + count * 25);
                        break;
                    case 30405:
                        htmltext = (count < 10) ? "30405-01.htm" : "30405-02.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItemsAlways(1495, 1, 0);
        return null;
    }
}
