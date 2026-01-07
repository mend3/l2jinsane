package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q151_CureForFeverDisease extends Quest {
    private static final String qn = "Q151_CureForFeverDisease";

    private static final int POISON_SAC = 703;

    private static final int FEVER_MEDICINE = 704;

    private static final int ELIAS = 30050;

    private static final int YOHANES = 30032;

    public Q151_CureForFeverDisease() {
        super(151, "Cure for Fever Disease");
        setItemsIds(704, 703);
        addStartNpc(30050);
        addTalkId(30050, 30032);
        addKillId(20103, 20106, 20108);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q151_CureForFeverDisease");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30050-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q151_CureForFeverDisease");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30050-01.htm" : "30050-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30050:
                        if (cond == 1) {
                            htmltext = "30050-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30050-05.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30050-06.htm";
                            st.takeItems(704, 1);
                            st.giveItems(102, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30032:
                        if (cond == 2) {
                            htmltext = "30032-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(703, 1);
                            st.giveItems(704, 1);
                            break;
                        }
                        if (cond == 3)
                            htmltext = "30032-02.htm";
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
        if (st.dropItems(703, 1, 1, 200000))
            st.set("cond", "2");
        return null;
    }
}
