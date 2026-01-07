package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q300_HuntingLetoLizardman extends Quest {
    private static final String qn = "Q300_HuntingLetoLizardman";

    private static final int BRACELET = 7139;

    private static final int LETO_LIZARDMAN = 20577;

    private static final int LETO_LIZARDMAN_ARCHER = 20578;

    private static final int LETO_LIZARDMAN_SOLDIER = 20579;

    private static final int LETO_LIZARDMAN_WARRIOR = 20580;

    private static final int LETO_LIZARDMAN_OVERLORD = 20582;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q300_HuntingLetoLizardman() {
        super(300, "Hunting Leto Lizardman");
        CHANCES.put(Integer.valueOf(20577), Integer.valueOf(300000));
        CHANCES.put(Integer.valueOf(20578), Integer.valueOf(320000));
        CHANCES.put(Integer.valueOf(20579), Integer.valueOf(350000));
        CHANCES.put(Integer.valueOf(20580), Integer.valueOf(650000));
        CHANCES.put(Integer.valueOf(20582), Integer.valueOf(700000));
        setItemsIds(7139);
        addStartNpc(30126);
        addTalkId(30126);
        addKillId(20577, 20578, 20579, 20580, 20582);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q300_HuntingLetoLizardman");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30126-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30126-05.htm")) {
            if (st.getQuestItemsCount(7139) >= 60) {
                htmltext = "30126-06.htm";
                st.takeItems(7139, -1);
                int luck = Rnd.get(3);
                if (luck == 0) {
                    st.rewardItems(57, 30000);
                } else if (luck == 1) {
                    st.rewardItems(1867, 50);
                } else if (luck == 2) {
                    st.rewardItems(1872, 50);
                }
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q300_HuntingLetoLizardman");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 34) ? "30126-01.htm" : "30126-02.htm";
                break;
            case 1:
                htmltext = (st.getInt("cond") == 1) ? "30126-04a.htm" : "30126-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(7139, 1, 60, CHANCES.get(Integer.valueOf(npc.getNpcId()))))
            st.set("cond", "2");
        return null;
    }
}
