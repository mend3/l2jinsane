package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q163_LegacyOfThePoet extends Quest {
    private static final String qn = "Q163_LegacyOfThePoet";

    private static final int STARDEN = 30220;

    private static final int[] RUMIELS_POEMS = new int[]{1038, 1039, 1040, 1041};

    private static final int[][] DROPLIST = new int[][]{{RUMIELS_POEMS[0], 1, 1, 100000}, {RUMIELS_POEMS[1], 1, 1, 200000}, {RUMIELS_POEMS[2], 1, 1, 200000}, {RUMIELS_POEMS[3], 1, 1, 400000}};

    public Q163_LegacyOfThePoet() {
        super(163, "Legacy of the Poet");
        setItemsIds(RUMIELS_POEMS);
        addStartNpc(30220);
        addTalkId(30220);
        addKillId(20372, 20373);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q163_LegacyOfThePoet");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30220-07.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q163_LegacyOfThePoet");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() == ClassRace.DARK_ELF) {
                    htmltext = "30220-00.htm";
                    break;
                }
                if (player.getLevel() < 11) {
                    htmltext = "30220-02.htm";
                    break;
                }
                htmltext = "30220-03.htm";
                break;
            case 1:
                if (st.getInt("cond") == 2) {
                    htmltext = "30220-09.htm";
                    for (int poem : RUMIELS_POEMS)
                        st.takeItems(poem, -1);
                    st.rewardItems(57, 13890);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                    break;
                }
                htmltext = "30220-08.htm";
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
        if (st.dropMultipleItems(DROPLIST))
            st.set("cond", "2");
        return null;
    }
}
