package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q299_GatherIngredientsForPie extends Quest {
    private static final String qn = "Q299_GatherIngredientsForPie";

    private static final int LARA = 30063;

    private static final int BRIGHT = 30466;

    private static final int EMILY = 30620;

    private static final int FRUIT_BASKET = 7136;

    private static final int AVELLAN_SPICE = 7137;

    private static final int HONEY_POUCH = 7138;

    public Q299_GatherIngredientsForPie() {
        super(299, "Gather Ingredients for Pie");
        setItemsIds(7136, 7137, 7138);
        addStartNpc(30620);
        addTalkId(30620, 30063, 30466);
        addKillId(20934, 20935);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q299_GatherIngredientsForPie");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30620-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30620-3.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7138, -1);
        } else if (event.equalsIgnoreCase("30063-1.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7137, 1);
        } else if (event.equalsIgnoreCase("30620-5.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7137, 1);
        } else if (event.equalsIgnoreCase("30466-1.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7136, 1);
        } else if (event.equalsIgnoreCase("30620-7a.htm")) {
            if (st.hasQuestItems(7136)) {
                htmltext = "30620-7.htm";
                st.takeItems(7136, 1);
                st.rewardItems(57, 25000);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                st.set("cond", "5");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q299_GatherIngredientsForPie");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 34) ? "30620-0a.htm" : "30620-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30620:
                        if (cond == 1) {
                            htmltext = "30620-1a.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (st.getQuestItemsCount(7138) >= 100) {
                                htmltext = "30620-2.htm";
                                break;
                            }
                            htmltext = "30620-2a.htm";
                            st.exitQuest(true);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30620-3a.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (st.hasQuestItems(7137)) {
                                htmltext = "30620-4.htm";
                                break;
                            }
                            htmltext = "30620-4a.htm";
                            st.exitQuest(true);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30620-5a.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30620-6.htm";
                        break;
                    case 30063:
                        if (cond == 3) {
                            htmltext = "30063-0.htm";
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30063-1a.htm";
                        break;
                    case 30466:
                        if (cond == 5) {
                            htmltext = "30466-0.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30466-1a.htm";
                        break;
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
        if (st.dropItems(7138, 1, 100, (npc.getNpcId() == 20934) ? 571000 : 625000))
            st.set("cond", "2");
        return null;
    }
}
