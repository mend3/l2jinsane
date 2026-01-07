package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q016_TheComingDarkness extends Quest {
    private static final String qn = "Q016_TheComingDarkness";

    private static final int HIERARCH = 31517;

    private static final int EVIL_ALTAR_1 = 31512;

    private static final int EVIL_ALTAR_2 = 31513;

    private static final int EVIL_ALTAR_3 = 31514;

    private static final int EVIL_ALTAR_4 = 31515;

    private static final int EVIL_ALTAR_5 = 31516;

    private static final int CRYSTAL_OF_SEAL = 7167;

    public Q016_TheComingDarkness() {
        super(16, "The Coming Darkness");
        setItemsIds(7167);
        addStartNpc(31517);
        addTalkId(31517, 31512, 31513, 31514, 31515, 31516);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q016_TheComingDarkness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31517-2.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7167, 5);
        } else if (event.equalsIgnoreCase("31512-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7167, 1);
        } else if (event.equalsIgnoreCase("31513-1.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7167, 1);
        } else if (event.equalsIgnoreCase("31514-1.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7167, 1);
        } else if (event.equalsIgnoreCase("31515-1.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7167, 1);
        } else if (event.equalsIgnoreCase("31516-1.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7167, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, npcId, condAltar;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q016_TheComingDarkness");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 62) ? "31517-0a.htm" : "31517-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                npcId = npc.getNpcId();
                switch (npcId) {
                    case 31517:
                        if (cond == 6) {
                            htmltext = "31517-4.htm";
                            st.rewardExpAndSp(221958L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                            break;
                        }
                        if (st.hasQuestItems(7167)) {
                            htmltext = "31517-3.htm";
                            break;
                        }
                        htmltext = "31517-3a.htm";
                        st.exitQuest(true);
                        break;
                    case 31512:
                    case 31513:
                    case 31514:
                    case 31515:
                    case 31516:
                        condAltar = npcId - 31511;
                        if (cond == condAltar) {
                            if (st.hasQuestItems(7167)) {
                                htmltext = npcId + "-0.htm";
                                break;
                            }
                            htmltext = "altar_nocrystal.htm";
                            break;
                        }
                        if (cond > condAltar)
                            htmltext = npcId + "-2.htm";
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
