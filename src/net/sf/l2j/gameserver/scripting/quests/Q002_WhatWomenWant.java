package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q002_WhatWomenWant extends Quest {
    private static final String qn = "Q002_WhatWomenWant";

    private static final int ARUJIEN = 30223;

    private static final int MIRABEL = 30146;

    private static final int HERBIEL = 30150;

    private static final int GREENIS = 30157;

    private static final int ARUJIEN_LETTER_1 = 1092;

    private static final int ARUJIEN_LETTER_2 = 1093;

    private static final int ARUJIEN_LETTER_3 = 1094;

    private static final int POETRY_BOOK = 689;

    private static final int GREENIS_LETTER = 693;

    private static final int MYSTICS_EARRING = 113;

    public Q002_WhatWomenWant() {
        super(2, "What Women Want");
        setItemsIds(1092, 1093, 1094, 689, 693);
        addStartNpc(30223);
        addTalkId(30223, 30146, 30150, 30157);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q002_WhatWomenWant");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30223-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1092, 1);
        } else if (event.equalsIgnoreCase("30223-08.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1094, 1);
            st.giveItems(689, 1);
        } else if (event.equalsIgnoreCase("30223-09.htm")) {
            st.takeItems(1094, 1);
            st.rewardItems(57, 450);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q002_WhatWomenWant");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF && player.getRace() != ClassRace.HUMAN) {
                    htmltext = "30223-00.htm";
                    break;
                }
                if (player.getLevel() < 2) {
                    htmltext = "30223-01.htm";
                    break;
                }
                htmltext = "30223-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30223:
                        if (st.hasQuestItems(1092)) {
                            htmltext = "30223-05.htm";
                            break;
                        }
                        if (st.hasQuestItems(1094)) {
                            htmltext = "30223-07.htm";
                            break;
                        }
                        if (st.hasQuestItems(1093)) {
                            htmltext = "30223-06.htm";
                            break;
                        }
                        if (st.hasQuestItems(689)) {
                            htmltext = "30223-11.htm";
                            break;
                        }
                        if (st.hasQuestItems(693)) {
                            htmltext = "30223-10.htm";
                            st.takeItems(693, 1);
                            st.giveItems(113, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30146:
                        if (cond == 1) {
                            htmltext = "30146-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1092, 1);
                            st.giveItems(1093, 1);
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30146-02.htm";
                        break;
                    case 30150:
                        if (cond == 2) {
                            htmltext = "30150-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1093, 1);
                            st.giveItems(1094, 1);
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30150-02.htm";
                        break;
                    case 30157:
                        if (cond < 4) {
                            htmltext = "30157-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30157-02.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(689, 1);
                            st.giveItems(693, 1);
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30157-03.htm";
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
