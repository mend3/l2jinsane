package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q639_GuardiansOfTheHolyGrail extends Quest {
    private static final String qn = "Q639_GuardiansOfTheHolyGrail";

    private static final int DOMINIC = 31350;

    private static final int GREMORY = 32008;

    private static final int HOLY_GRAIL = 32028;

    private static final int SCRIPTURE = 8069;

    private static final int WATER_BOTTLE = 8070;

    private static final int HOLY_WATER_BOTTLE = 8071;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q639_GuardiansOfTheHolyGrail() {
        super(639, "Guardians of the Holy Grail");
        CHANCES.put(22122, 760000);
        CHANCES.put(22123, 750000);
        CHANCES.put(22124, 590000);
        CHANCES.put(22125, 580000);
        CHANCES.put(22126, 590000);
        CHANCES.put(22127, 580000);
        CHANCES.put(22128, 170000);
        CHANCES.put(22129, 590000);
        CHANCES.put(22130, 850000);
        CHANCES.put(22131, 920000);
        CHANCES.put(22132, 580000);
        CHANCES.put(22133, 930000);
        CHANCES.put(22134, 230000);
        CHANCES.put(22135, 580000);
        setItemsIds(8069, 8070, 8071);
        addStartNpc(31350);
        addTalkId(31350, 32008, 32028);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            addKillId(id);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q639_GuardiansOfTheHolyGrail");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31350-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31350-08.htm")) {
            int count = st.getQuestItemsCount(8069);
            st.takeItems(8069, -1);
            st.rewardItems(57, 1625 * count + ((count >= 10) ? 33940 : 0));
        } else if (event.equalsIgnoreCase("31350-09.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("32008-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8070, 1);
        } else if (event.equalsIgnoreCase("32008-09.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8071, 1);
        } else if (event.equalsIgnoreCase("32008-12.htm")) {
            if (st.getQuestItemsCount(8069) >= 4000) {
                htmltext = "32008-11.htm";
                st.takeItems(8069, 4000);
                st.rewardItems(959, 1);
            }
        } else if (event.equalsIgnoreCase("32008-14.htm")) {
            if (st.getQuestItemsCount(8069) >= 400) {
                htmltext = "32008-13.htm";
                st.takeItems(8069, 400);
                st.rewardItems(960, 1);
            }
        } else if (event.equalsIgnoreCase("32028-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8070, 1);
            st.giveItems(8071, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q639_GuardiansOfTheHolyGrail");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31350-02.htm" : "31350-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31350:
                        htmltext = st.hasQuestItems(8069) ? "31350-05.htm" : "31350-06.htm";
                        break;
                    case 32008:
                        if (cond == 1) {
                            htmltext = "32008-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "32008-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32008-08.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "32008-10.htm";
                        break;
                    case 32028:
                        if (cond == 2) {
                            htmltext = "32028-01.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "32028-03.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8069, 1, 0, CHANCES.get(npc.getNpcId()));
        return null;
    }
}
