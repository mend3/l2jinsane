package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q628_HuntOfTheGoldenRamMercenaryForce extends Quest {
    private static final String qn = "Q628_HuntOfTheGoldenRamMercenaryForce";

    private static final int KAHMAN = 31554;

    private static final int SPLINTER_STAKATO_CHITIN = 7248;

    private static final int NEEDLE_STAKATO_CHITIN = 7249;

    private static final int GOLDEN_RAM_BADGE_RECRUIT = 7246;

    private static final int GOLDEN_RAM_BADGE_SOLDIER = 7247;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q628_HuntOfTheGoldenRamMercenaryForce() {
        super(628, "Hunt of the Golden Ram Mercenary Force");
        CHANCES.put(21508, 500000);
        CHANCES.put(21509, 430000);
        CHANCES.put(21510, 521000);
        CHANCES.put(21511, 575000);
        CHANCES.put(21512, 746000);
        CHANCES.put(21513, 500000);
        CHANCES.put(21514, 430000);
        CHANCES.put(21515, 520000);
        CHANCES.put(21516, 531000);
        CHANCES.put(21517, 744000);
        setItemsIds(7248, 7249, 7246, 7247);
        addStartNpc(31554);
        addTalkId(31554);
        for (int npcId : CHANCES.keySet()) {
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q628_HuntOfTheGoldenRamMercenaryForce");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31554-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31554-03a.htm")) {
            if (st.getQuestItemsCount(7248) >= 100 && st.getInt("cond") == 1) {
                htmltext = "31554-04.htm";
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7248, -1);
                st.giveItems(7246, 1);
            }
        } else if (event.equalsIgnoreCase("31554-07.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q628_HuntOfTheGoldenRamMercenaryForce");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 66) ? "31554-01a.htm" : "31554-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    if (st.getQuestItemsCount(7248) >= 100) {
                        htmltext = "31554-03.htm";
                        break;
                    }
                    htmltext = "31554-03a.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7248) >= 100 && st.getQuestItemsCount(7249) >= 100) {
                        htmltext = "31554-05.htm";
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_finish");
                        st.takeItems(7248, -1);
                        st.takeItems(7249, -1);
                        st.takeItems(7246, 1);
                        st.giveItems(7247, 1);
                        break;
                    }
                    if (!st.hasQuestItems(7248) && !st.hasQuestItems(7249)) {
                        htmltext = "31554-04b.htm";
                        break;
                    }
                    htmltext = "31554-04a.htm";
                    break;
                }
                if (cond == 3)
                    htmltext = "31554-05a.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 21508:
            case 21509:
            case 21510:
            case 21511:
            case 21512:
                if (cond == 1 || cond == 2)
                    st.dropItems(7248, 1, 100, CHANCES.get(npcId));
                break;
            case 21513:
            case 21514:
            case 21515:
            case 21516:
            case 21517:
                if (cond == 2)
                    st.dropItems(7249, 1, 100, CHANCES.get(npcId));
                break;
        }
        return null;
    }
}
