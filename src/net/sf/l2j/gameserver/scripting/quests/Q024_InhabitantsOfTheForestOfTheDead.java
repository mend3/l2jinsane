package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q024_InhabitantsOfTheForestOfTheDead extends Quest {
    private static final String qn = "Q024_InhabitantsOfTheForestOfTheDead";

    private static final int DORIAN = 31389;

    private static final int MYSTERIOUS_WIZARD = 31522;

    private static final int TOMBSTONE = 31531;

    private static final int LIDIA_MAID = 31532;

    private static final int BONE_SNATCHER = 21557;

    private static final int BONE_SNATCHER_A = 21558;

    private static final int BONE_SHAPER = 21560;

    private static final int BONE_COLLECTOR = 21563;

    private static final int SKULL_COLLECTOR = 21564;

    private static final int BONE_ANIMATOR = 21565;

    private static final int SKULL_ANIMATOR = 21566;

    private static final int BONE_SLAYER = 21567;

    private static final int LIDIAS_LETTER = 7065;

    private static final int LIDIAS_HAIRPIN = 7148;

    private static final int SUSPICIOUS_TOTEM_DOLL = 7151;

    private static final int FLOWER_BOUQUET = 7152;

    private static final int SILVER_CROSS_OF_EINHASAD = 7153;

    private static final int BROKEN_SILVER_CROSS_OF_EINHASAD = 7154;

    private static final int SUSPICIOUS_TOTEM_DOLL_2 = 7156;

    public Q024_InhabitantsOfTheForestOfTheDead() {
        super(24, "Inhabitants of the Forest of the Dead");
        setItemsIds(7065, 7148, 7151, 7152, 7153, 7154);
        addStartNpc(31389);
        addTalkId(31389, 31522, 31532, 31531);
        addKillId(21557, 21558, 21560, 21563, 21564, 21565, 21566, 21567);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q024_InhabitantsOfTheForestOfTheDead");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31389-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("state", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(7152, 1);
        } else if (event.equalsIgnoreCase("31389-08.htm")) {
            st.set("state", "3");
        } else if (event.equalsIgnoreCase("31389-13.htm")) {
            st.set("cond", "3");
            st.set("state", "4");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7153, 1);
        } else if (event.equalsIgnoreCase("31389-18.htm")) {
            st.playSound("InterfaceSound.charstat_open_01");
        } else if (event.equalsIgnoreCase("31389-19.htm")) {
            st.set("cond", "5");
            st.set("state", "5");
            st.takeItems(7154, -1);
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31522-03.htm")) {
            st.set("state", "12");
            st.takeItems(7151, -1);
        } else if (event.equalsIgnoreCase("31522-08.htm")) {
            st.set("cond", "11");
            st.set("state", "13");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31522-17.htm")) {
            st.set("state", "14");
        } else if (event.equalsIgnoreCase("31522-21.htm")) {
            st.giveItems(7156, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("31532-04.htm")) {
            st.set("cond", "6");
            st.set("state", "6");
            st.giveItems(7065, 1);
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31532-06.htm")) {
            if (st.hasQuestItems(7148)) {
                st.set("state", "8");
                st.takeItems(7065, -1);
                st.takeItems(7148, -1);
            } else {
                st.set("cond", "7");
                st.set("state", "7");
                htmltext = "31532-07.htm";
            }
        } else if (event.equalsIgnoreCase("31532-10.htm")) {
            st.set("state", "9");
        } else if (event.equalsIgnoreCase("31532-14.htm")) {
            st.set("state", "10");
        } else if (event.equalsIgnoreCase("31532-19.htm")) {
            st.set("cond", "9");
            st.set("state", "11");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31531-02.htm")) {
            st.set("cond", "2");
            st.set("state", "2");
            st.takeItems(7152, -1);
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int state;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q024_InhabitantsOfTheForestOfTheDead");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                st2 = player.getQuestState("Q023_LidiasHeart");
                if (st2 == null || !st2.isCompleted() || player.getLevel() < 65) {
                    htmltext = "31389-02.htm";
                    break;
                }
                htmltext = "31389-01.htm";
                break;
            case 1:
                state = st.getInt("state");
                switch (npc.getNpcId()) {
                    case 31389:
                        if (state == 1) {
                            htmltext = "31389-04.htm";
                            break;
                        }
                        if (state == 2) {
                            htmltext = "31389-05.htm";
                            break;
                        }
                        if (state == 3) {
                            htmltext = "31389-09.htm";
                            break;
                        }
                        if (state == 4) {
                            if (st.hasQuestItems(7153)) {
                                htmltext = "31389-14.htm";
                                break;
                            }
                            if (st.hasQuestItems(7154))
                                htmltext = "31389-15.htm";
                            break;
                        }
                        if (state == 5) {
                            htmltext = "31389-20.htm";
                            break;
                        }
                        if (state == 7 && !st.hasQuestItems(7148)) {
                            htmltext = "31389-21.htm";
                            st.set("cond", "8");
                            st.giveItems(7148, 1);
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if ((state == 7 && st.hasQuestItems(7148)) || state == 6)
                            htmltext = "31389-22.htm";
                        break;
                    case 31522:
                        if (state == 11 && st.hasQuestItems(7151)) {
                            htmltext = "31522-01.htm";
                            break;
                        }
                        if (state == 12) {
                            htmltext = "31522-04.htm";
                            break;
                        }
                        if (state == 13) {
                            htmltext = "31522-09.htm";
                            break;
                        }
                        if (state == 14)
                            htmltext = "31522-18.htm";
                        break;
                    case 31532:
                        if (state == 5) {
                            htmltext = "31532-01.htm";
                            break;
                        }
                        if (state == 6 && st.hasQuestItems(7065)) {
                            htmltext = "31532-05.htm";
                            break;
                        }
                        if (state == 7) {
                            htmltext = "31532-07a.htm";
                            break;
                        }
                        if (state == 8) {
                            htmltext = "31532-08.htm";
                            break;
                        }
                        if (state == 9) {
                            htmltext = "31532-11.htm";
                            break;
                        }
                        if (state == 10) {
                            htmltext = "31532-15.htm";
                            break;
                        }
                        if (state == 11)
                            htmltext = "31532-20.htm";
                        break;
                    case 31531:
                        if (state == 1 && st.hasQuestItems(7152)) {
                            htmltext = "31531-01.htm";
                            st.playSound("AmdSound.d_wind_loot_02");
                            break;
                        }
                        if (state == 2)
                            htmltext = "31531-03.htm";
                        break;
                }
                break;
            case 2:
                if (npc.getNpcId() == 31522) {
                    htmltext = "31522-22.htm";
                    break;
                }
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "9");
        if (st == null)
            return null;
        if (st.dropItems(7151, 1, 1, 100000))
            st.set("cond", "10");
        return null;
    }
}
