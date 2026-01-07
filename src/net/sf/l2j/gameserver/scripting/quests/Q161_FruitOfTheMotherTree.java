package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q161_FruitOfTheMotherTree extends Quest {
    private static final String qn = "Q161_FruitOfTheMotherTree";

    private static final int ANDELLIA = 30362;

    private static final int THALIA = 30371;

    private static final int ANDELLIA_LETTER = 1036;

    private static final int MOTHERTREE_FRUIT = 1037;

    public Q161_FruitOfTheMotherTree() {
        super(161, "Fruit of the Mothertree");
        setItemsIds(1036, 1037);
        addStartNpc(30362);
        addTalkId(30362, 30371);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q161_FruitOfTheMotherTree");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30362-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1036, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q161_FruitOfTheMotherTree");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30362-00.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "30362-02.htm";
                    break;
                }
                htmltext = "30362-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30362:
                        if (cond == 1) {
                            htmltext = "30362-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30362-06.htm";
                            st.takeItems(1037, 1);
                            st.rewardItems(57, 1000);
                            st.rewardExpAndSp(1000L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30371:
                        if (cond == 1) {
                            htmltext = "30371-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1036, 1);
                            st.giveItems(1037, 1);
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30371-02.htm";
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
