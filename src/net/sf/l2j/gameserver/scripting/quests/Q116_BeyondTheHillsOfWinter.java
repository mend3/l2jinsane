package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q116_BeyondTheHillsOfWinter extends Quest {
    private static final String qn = "Q116_BeyondTheHillsOfWinter";

    private static final int FILAUR = 30535;

    private static final int OBI = 32052;

    private static final int BANDAGE = 1833;

    private static final int ENERGY_STONE = 5589;

    private static final int THIEF_KEY = 1661;

    private static final int GOODS = 8098;

    private static final int SSD = 1463;

    public Q116_BeyondTheHillsOfWinter() {
        super(116, "Beyond the Hills of Winter");
        setItemsIds(8098);
        addStartNpc(30535);
        addTalkId(30535, 32052);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q116_BeyondTheHillsOfWinter");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30535-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30535-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8098, 1);
        } else if (event.equalsIgnoreCase("materials")) {
            htmltext = "32052-02.htm";
            st.takeItems(8098, -1);
            st.rewardItems(1463, 1650);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("adena")) {
            htmltext = "32052-02.htm";
            st.takeItems(8098, -1);
            st.giveItems(57, 16500);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q116_BeyondTheHillsOfWinter");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 30 || player.getRace() != ClassRace.DWARF) ? "30535-00.htm" : "30535-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30535:
                        if (cond == 1) {
                            if (st.getQuestItemsCount(1833) >= 20 && st.getQuestItemsCount(5589) >= 5 && st.getQuestItemsCount(1661) >= 10) {
                                htmltext = "30535-03.htm";
                                st.takeItems(1833, 20);
                                st.takeItems(5589, 5);
                                st.takeItems(1661, 10);
                                break;
                            }
                            htmltext = "30535-04.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30535-05.htm";
                        break;
                    case 32052:
                        if (cond == 2)
                            htmltext = "32052-00.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
