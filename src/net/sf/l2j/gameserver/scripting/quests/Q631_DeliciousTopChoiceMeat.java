package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q631_DeliciousTopChoiceMeat extends Quest {
    private static final String qn = "Q631_DeliciousTopChoiceMeat";

    private static final int TUNATUN = 31537;

    private static final int TOP_QUALITY_MEAT = 7546;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final int[][] REWARDS = new int[][]{{4039, 15}, {4043, 15}, {4044, 15}, {4040, 10}, {4042, 10}, {4041, 5}};

    public Q631_DeliciousTopChoiceMeat() {
        super(631, "Delicious Top Choice Meat");
        CHANCES.put(Integer.valueOf(21460), Integer.valueOf(601000));
        CHANCES.put(Integer.valueOf(21461), Integer.valueOf(480000));
        CHANCES.put(Integer.valueOf(21462), Integer.valueOf(447000));
        CHANCES.put(Integer.valueOf(21463), Integer.valueOf(808000));
        CHANCES.put(Integer.valueOf(21464), Integer.valueOf(447000));
        CHANCES.put(Integer.valueOf(21465), Integer.valueOf(808000));
        CHANCES.put(Integer.valueOf(21466), Integer.valueOf(447000));
        CHANCES.put(Integer.valueOf(21467), Integer.valueOf(808000));
        CHANCES.put(Integer.valueOf(21479), Integer.valueOf(477000));
        CHANCES.put(Integer.valueOf(21480), Integer.valueOf(863000));
        CHANCES.put(Integer.valueOf(21481), Integer.valueOf(477000));
        CHANCES.put(Integer.valueOf(21482), Integer.valueOf(863000));
        CHANCES.put(Integer.valueOf(21483), Integer.valueOf(477000));
        CHANCES.put(Integer.valueOf(21484), Integer.valueOf(863000));
        CHANCES.put(Integer.valueOf(21485), Integer.valueOf(477000));
        CHANCES.put(Integer.valueOf(21486), Integer.valueOf(863000));
        CHANCES.put(Integer.valueOf(21498), Integer.valueOf(509000));
        CHANCES.put(Integer.valueOf(21499), Integer.valueOf(920000));
        CHANCES.put(Integer.valueOf(21500), Integer.valueOf(509000));
        CHANCES.put(Integer.valueOf(21501), Integer.valueOf(920000));
        CHANCES.put(Integer.valueOf(21502), Integer.valueOf(509000));
        CHANCES.put(Integer.valueOf(21503), Integer.valueOf(920000));
        CHANCES.put(Integer.valueOf(21504), Integer.valueOf(509000));
        CHANCES.put(Integer.valueOf(21505), Integer.valueOf(920000));
        setItemsIds(7546);
        addStartNpc(31537);
        addTalkId(31537);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q631_DeliciousTopChoiceMeat");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31537-03.htm")) {
            if (player.getLevel() >= 65) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "31537-02.htm";
                st.exitQuest(true);
            }
        } else if (StringUtil.isDigit(event)) {
            if (st.getQuestItemsCount(7546) >= 120) {
                htmltext = "31537-06.htm";
                st.takeItems(7546, -1);
                int[] reward = REWARDS[Integer.parseInt(event)];
                st.rewardItems(reward[0], reward[1]);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                st.set("cond", "1");
                htmltext = "31537-07.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q631_DeliciousTopChoiceMeat");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "31537-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "31537-03a.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7546) >= 120) {
                        htmltext = "31537-04.htm";
                        break;
                    }
                    st.set("cond", "1");
                    htmltext = "31537-03a.htm";
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "1");
        if (st == null)
            return null;
        if (st.dropItems(7546, 1, 120, CHANCES.get(Integer.valueOf(npc.getNpcId()))))
            st.set("cond", "2");
        return null;
    }
}
