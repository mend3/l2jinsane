package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q626_ADarkTwilight extends Quest {
    private static final String qn = "Q626_ADarkTwilight";

    private static final int BLOOD_OF_SAINT = 7169;

    private static final int HIERARCH = 31517;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q626_ADarkTwilight() {
        super(626, "A Dark Twilight");
        CHANCES.put(Integer.valueOf(21520), Integer.valueOf(533000));
        CHANCES.put(Integer.valueOf(21523), Integer.valueOf(566000));
        CHANCES.put(Integer.valueOf(21524), Integer.valueOf(603000));
        CHANCES.put(Integer.valueOf(21525), Integer.valueOf(603000));
        CHANCES.put(Integer.valueOf(21526), Integer.valueOf(587000));
        CHANCES.put(Integer.valueOf(21529), Integer.valueOf(606000));
        CHANCES.put(Integer.valueOf(21530), Integer.valueOf(560000));
        CHANCES.put(Integer.valueOf(21531), Integer.valueOf(669000));
        CHANCES.put(Integer.valueOf(21532), Integer.valueOf(651000));
        CHANCES.put(Integer.valueOf(21535), Integer.valueOf(672000));
        CHANCES.put(Integer.valueOf(21536), Integer.valueOf(597000));
        CHANCES.put(Integer.valueOf(21539), Integer.valueOf(739000));
        CHANCES.put(Integer.valueOf(21540), Integer.valueOf(739000));
        CHANCES.put(Integer.valueOf(21658), Integer.valueOf(669000));
        setItemsIds(7169);
        addStartNpc(31517);
        addTalkId(31517);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q626_ADarkTwilight");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31517-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("reward1")) {
            if (st.getQuestItemsCount(7169) == 300) {
                htmltext = "31517-07.htm";
                st.takeItems(7169, 300);
                st.rewardExpAndSp(162773L, 12500);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "31517-08.htm";
            }
        } else if (event.equalsIgnoreCase("reward2")) {
            if (st.getQuestItemsCount(7169) == 300) {
                htmltext = "31517-07.htm";
                st.takeItems(7169, 300);
                st.rewardItems(57, 100000);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "31517-08.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q626_ADarkTwilight");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 60) ? "31517-02.htm" : "31517-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31517-05.htm";
                    break;
                }
                htmltext = "31517-04.htm";
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
        if (st.dropItems(7169, 1, 300, CHANCES.get(Integer.valueOf(npc.getNpcId()))))
            st.set("cond", "2");
        return null;
    }
}
