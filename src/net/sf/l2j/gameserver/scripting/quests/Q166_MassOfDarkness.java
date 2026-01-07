package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q166_MassOfDarkness extends Quest {
    private static final String qn = "Q166_MassOfDarkness";

    private static final int UNDRIAS = 30130;

    private static final int IRIA = 30135;

    private static final int DORANKUS = 30139;

    private static final int TRUDY = 30143;

    private static final int UNDRIAS_LETTER = 1088;

    private static final int CEREMONIAL_DAGGER = 1089;

    private static final int DREVIANT_WINE = 1090;

    private static final int GARMIEL_SCRIPTURE = 1091;

    public Q166_MassOfDarkness() {
        super(166, "Mass of Darkness");
        setItemsIds(1088, 1089, 1090, 1091);
        addStartNpc(30130);
        addTalkId(30130, 30135, 30139, 30143);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q166_MassOfDarkness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30130-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1088, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q166_MassOfDarkness");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30130-00.htm";
                    break;
                }
                if (player.getLevel() < 2) {
                    htmltext = "30130-02.htm";
                    break;
                }
                htmltext = "30130-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30130:
                        if (cond == 1) {
                            htmltext = "30130-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30130-06.htm";
                            st.takeItems(1089, 1);
                            st.takeItems(1090, 1);
                            st.takeItems(1091, 1);
                            st.takeItems(1088, 1);
                            st.rewardItems(57, 500);
                            st.rewardExpAndSp(500L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30135:
                        if (cond == 1 && !st.hasQuestItems(1089)) {
                            htmltext = "30135-01.htm";
                            st.giveItems(1089, 1);
                            if (st.hasQuestItems(1090, 1091)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30135-02.htm";
                        break;
                    case 30139:
                        if (cond == 1 && !st.hasQuestItems(1090)) {
                            htmltext = "30139-01.htm";
                            st.giveItems(1090, 1);
                            if (st.hasQuestItems(1089, 1091)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30139-02.htm";
                        break;
                    case 30143:
                        if (cond == 1 && !st.hasQuestItems(1091)) {
                            htmltext = "30143-01.htm";
                            st.giveItems(1091, 1);
                            if (st.hasQuestItems(1089, 1090)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30143-02.htm";
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
