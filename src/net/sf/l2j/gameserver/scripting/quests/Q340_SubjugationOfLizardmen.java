package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q340_SubjugationOfLizardmen extends Quest {
    private static final String qn = "Q340_SubjugationOfLizardmen";

    private static final int WEISZ = 30385;

    private static final int ADONIUS = 30375;

    private static final int LEVIAN = 30037;

    private static final int CHEST = 30989;

    private static final int CARGO = 4255;

    private static final int HOLY = 4256;

    private static final int ROSARY = 4257;

    private static final int TOTEM = 4258;

    public Q340_SubjugationOfLizardmen() {
        super(340, "Subjugation of Lizardmen");
        setItemsIds(4255, 4256, 4257, 4258);
        addStartNpc(30385);
        addTalkId(30385, 30375, 30037, 30989);
        addKillId(20008, 20010, 20014, 20024, 20027, 20030, 25146);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q340_SubjugationOfLizardmen");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30385-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30385-07.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(4255, -1);
        } else if (event.equalsIgnoreCase("30385-09.htm")) {
            st.takeItems(4255, -1);
            st.rewardItems(57, 4090);
        } else if (event.equalsIgnoreCase("30385-10.htm")) {
            st.takeItems(4255, -1);
            st.rewardItems(57, 4090);
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30375-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30037-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30989-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4258, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q340_SubjugationOfLizardmen");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 17) ? "30385-01.htm" : "30385-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30385:
                        if (cond == 1) {
                            htmltext = (st.getQuestItemsCount(4255) < 30) ? "30385-05.htm" : "30385-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30385-11.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30385-13.htm";
                            st.rewardItems(57, 14700);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30375:
                        if (cond == 2) {
                            htmltext = "30375-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            if (st.hasQuestItems(4257, 4256)) {
                                htmltext = "30375-04.htm";
                                st.set("cond", "4");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4256, -1);
                                st.takeItems(4257, -1);
                                break;
                            }
                            htmltext = "30375-03.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30375-05.htm";
                        break;
                    case 30037:
                        if (cond == 4) {
                            htmltext = "30037-01.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30037-03.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30037-04.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4258, -1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30037-05.htm";
                        break;
                    case 30989:
                        if (cond == 5) {
                            htmltext = "30989-01.htm";
                            break;
                        }
                        htmltext = "30989-03.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20008:
                if (st.getInt("cond") == 1)
                    st.dropItems(4255, 1, 30, 500000);
                break;
            case 20010:
                if (st.getInt("cond") == 1)
                    st.dropItems(4255, 1, 30, 520000);
                break;
            case 20014:
                if (st.getInt("cond") == 1)
                    st.dropItems(4255, 1, 30, 550000);
                break;
            case 20024:
            case 20027:
            case 20030:
                if (st.getInt("cond") == 3)
                    if (st.dropItems(4256, 1, 1, 100000))
                        st.dropItems(4257, 1, 1, 100000);
                break;
            case 25146:
                addSpawn(30989, npc, false, 30000L, false);
                break;
        }
        return null;
    }
}
