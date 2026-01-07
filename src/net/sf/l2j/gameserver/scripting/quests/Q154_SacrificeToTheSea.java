package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q154_SacrificeToTheSea extends Quest {
    private static final String qn = "Q154_SacrificeToTheSea";

    private static final int ROCKSWELL = 30312;

    private static final int CRISTEL = 30051;

    private static final int ROLFE = 30055;

    private static final int FOX_FUR = 1032;

    private static final int FOX_FUR_YARN = 1033;

    private static final int MAIDEN_DOLL = 1034;

    private static final int EARING = 113;

    public Q154_SacrificeToTheSea() {
        super(154, "Sacrifice to the Sea");
        setItemsIds(1032, 1033, 1034);
        addStartNpc(30312);
        addTalkId(30312, 30051, 30055);
        addKillId(20481, 20544, 20545);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q154_SacrificeToTheSea");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30312-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q154_SacrificeToTheSea");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 2) ? "30312-02.htm" : "30312-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30312:
                        if (cond == 1) {
                            htmltext = "30312-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30312-08.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30312-06.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30312-07.htm";
                            st.takeItems(1034, -1);
                            st.giveItems(113, 1);
                            st.rewardExpAndSp(100L, 0);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30051:
                        if (cond == 1) {
                            htmltext = st.hasQuestItems(1032) ? "30051-01.htm" : "30051-01a.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30051-02.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1032, -1);
                            st.giveItems(1033, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30051-03.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30051-04.htm";
                        break;
                    case 30055:
                        if (cond < 3) {
                            htmltext = "30055-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30055-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1033, 1);
                            st.giveItems(1034, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30055-02.htm";
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(1032, 1, 10, 400000))
            st.set("cond", "2");
        return null;
    }
}
