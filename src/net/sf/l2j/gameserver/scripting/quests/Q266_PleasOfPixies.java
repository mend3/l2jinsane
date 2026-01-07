package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q266_PleasOfPixies extends Quest {
    private static final String qn = "Q266_PleasOfPixies";

    private static final int PREDATOR_FANG = 1334;

    private static final int GLASS_SHARD = 1336;

    private static final int EMERALD = 1337;

    private static final int BLUE_ONYX = 1338;

    private static final int ONYX = 1339;

    public Q266_PleasOfPixies() {
        super(266, "Pleas of Pixies");
        setItemsIds(1334);
        addStartNpc(31852);
        addTalkId(31852);
        addKillId(20525, 20530, 20534, 20537);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q266_PleasOfPixies");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31852-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int n;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q266_PleasOfPixies");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "31852-00.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "31852-01.htm";
                    break;
                }
                htmltext = "31852-02.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(1334) < 100) {
                    htmltext = "31852-04.htm";
                    break;
                }
                htmltext = "31852-05.htm";
                st.takeItems(1334, -1);
                n = Rnd.get(100);
                if (n < 10) {
                    st.playSound("ItemSound.quest_jackpot");
                    st.rewardItems(1337, 1);
                } else if (n < 30) {
                    st.rewardItems(1338, 1);
                } else if (n < 60) {
                    st.rewardItems(1339, 1);
                } else {
                    st.rewardItems(1336, 1);
                }
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
        switch (npc.getNpcId()) {
            case 20525:
                if (st.dropItemsAlways(1334, Rnd.get(2, 3), 100))
                    st.set("cond", "2");
                break;
            case 20530:
                if (st.dropItems(1334, 1, 100, 800000))
                    st.set("cond", "2");
                break;
            case 20534:
                if (st.dropItems(1334, (Rnd.get(3) == 0) ? 1 : 2, 100, 600000))
                    st.set("cond", "2");
                break;
            case 20537:
                if (st.dropItemsAlways(1334, 2, 100))
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
