package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q364_JovialAccordion extends Quest {
    private static final String qn = "Q364_JovialAccordion";

    private static final int BARBADO = 30959;

    private static final int SWAN = 30957;

    private static final int SABRIN = 30060;

    private static final int XABER = 30075;

    private static final int CLOTH_CHEST = 30961;

    private static final int BEER_CHEST = 30960;

    private static final int KEY_1 = 4323;

    private static final int KEY_2 = 4324;

    private static final int STOLEN_BEER = 4321;

    private static final int STOLEN_CLOTHES = 4322;

    private static final int ECHO = 4421;

    public Q364_JovialAccordion() {
        super(364, "Jovial Accordion");
        setItemsIds(4323, 4324, 4321, 4322);
        addStartNpc(30959);
        addTalkId(30959, 30957, 30060, 30075, 30961, 30960);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q364_JovialAccordion");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30959-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("items", "0");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30957-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4323, 1);
            st.giveItems(4324, 1);
        } else if (event.equalsIgnoreCase("30960-04.htm")) {
            if (st.hasQuestItems(4324)) {
                st.takeItems(4324, 1);
                if (Rnd.nextBoolean()) {
                    htmltext = "30960-02.htm";
                    st.giveItems(4321, 1);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (event.equalsIgnoreCase("30961-04.htm")) {
            if (st.hasQuestItems(4323)) {
                st.takeItems(4323, 1);
                if (Rnd.nextBoolean()) {
                    htmltext = "30961-02.htm";
                    st.giveItems(4322, 1);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, stolenItems;
        QuestState st = player.getQuestState("Q364_JovialAccordion");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30959-00.htm" : "30959-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                stolenItems = st.getInt("items");
                switch (npc.getNpcId()) {
                    case 30959:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30959-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30959-04.htm";
                            st.giveItems(4421, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30957:
                        if (cond == 1) {
                            htmltext = "30957-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (stolenItems > 0) {
                                st.set("cond", "3");
                                st.playSound("ItemSound.quest_middle");
                                if (stolenItems == 2) {
                                    htmltext = "30957-04.htm";
                                    st.rewardItems(57, 100);
                                    break;
                                }
                                htmltext = "30957-05.htm";
                                break;
                            }
                            if (!st.hasQuestItems(4323) && !st.hasQuestItems(4324)) {
                                htmltext = "30957-06.htm";
                                st.playSound("ItemSound.quest_finish");
                                st.exitQuest(true);
                                break;
                            }
                            htmltext = "30957-03.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30957-07.htm";
                        break;
                    case 30960:
                        htmltext = "30960-03.htm";
                        if (cond == 2 && st.hasQuestItems(4324))
                            htmltext = "30960-01.htm";
                        break;
                    case 30961:
                        htmltext = "30961-03.htm";
                        if (cond == 2 && st.hasQuestItems(4323))
                            htmltext = "30961-01.htm";
                        break;
                    case 30060:
                        if (st.hasQuestItems(4321)) {
                            htmltext = "30060-01.htm";
                            st.set("items", String.valueOf(stolenItems + 1));
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(4321, 1);
                            break;
                        }
                        htmltext = "30060-02.htm";
                        break;
                    case 30075:
                        if (st.hasQuestItems(4322)) {
                            htmltext = "30075-01.htm";
                            st.set("items", String.valueOf(stolenItems + 1));
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(4322, 1);
                            break;
                        }
                        htmltext = "30075-02.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
