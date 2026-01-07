package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q165_ShilensHunt extends Quest {
    private static final String qn = "Q165_ShilensHunt";

    private static final int ASHEN_WOLF = 20456;

    private static final int YOUNG_BROWN_KELTIR = 20529;

    private static final int BROWN_KELTIR = 20532;

    private static final int ELDER_BROWN_KELTIR = 20536;

    private static final int DARK_BEZOAR = 1160;

    private static final int LESSER_HEALING_POTION = 1060;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q165_ShilensHunt() {
        super(165, "Shilen's Hunt");
        CHANCES.put(20456, 1000000);
        CHANCES.put(20529, 333333);
        CHANCES.put(20532, 333333);
        CHANCES.put(20536, 666667);
        setItemsIds(1160);
        addStartNpc(30348);
        addTalkId(30348);
        addKillId(20456, 20529, 20532, 20536);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q165_ShilensHunt");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30348-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q165_ShilensHunt");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30348-00.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "30348-01.htm";
                    break;
                }
                htmltext = "30348-02.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(1160) >= 13) {
                    htmltext = "30348-05.htm";
                    st.takeItems(1160, -1);
                    st.rewardItems(1060, 5);
                    st.rewardExpAndSp(1000L, 0);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                    break;
                }
                htmltext = "30348-04.htm";
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
        if (st.dropItems(1160, 1, 13, CHANCES.get(npc.getNpcId())))
            st.set("cond", "2");
        return null;
    }
}
