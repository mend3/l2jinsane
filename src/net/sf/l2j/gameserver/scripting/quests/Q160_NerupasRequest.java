package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q160_NerupasRequest extends Quest {
    private static final String qn = "Q160_NerupasRequest";

    private static final int SILVERY_SPIDERSILK = 1026;

    private static final int UNOREN_RECEIPT = 1027;

    private static final int CREAMEES_TICKET = 1028;

    private static final int NIGHTSHADE_LEAF = 1029;

    private static final int LESSER_HEALING_POTION = 1060;

    private static final int NERUPA = 30370;

    private static final int UNOREN = 30147;

    private static final int CREAMEES = 30149;

    private static final int JULIA = 30152;

    public Q160_NerupasRequest() {
        super(160, "Nerupa's Request");
        setItemsIds(1026, 1027, 1028, 1029);
        addStartNpc(30370);
        addTalkId(30370, 30147, 30149, 30152);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q160_NerupasRequest");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30370-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1026, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q160_NerupasRequest");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30370-00.htm";
                    break;
                }
                if (player.getLevel() < 3) {
                    htmltext = "30370-02.htm";
                    break;
                }
                htmltext = "30370-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30370:
                        if (cond < 4) {
                            htmltext = "30370-05.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30370-06.htm";
                            st.takeItems(1029, 1);
                            st.rewardItems(1060, 5);
                            st.rewardExpAndSp(1000L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30147:
                        if (cond == 1) {
                            htmltext = "30147-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1026, 1);
                            st.giveItems(1027, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30147-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30147-03.htm";
                        break;
                    case 30149:
                        if (cond == 2) {
                            htmltext = "30149-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1027, 1);
                            st.giveItems(1028, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30149-02.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30149-03.htm";
                        break;
                    case 30152:
                        if (cond == 3) {
                            htmltext = "30152-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1028, 1);
                            st.giveItems(1029, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30152-02.htm";
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
