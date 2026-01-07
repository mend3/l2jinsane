package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q367_ElectrifyingRecharge extends Quest {
    private static final String qn = "Q367_ElectrifyingRecharge";

    private static final int LORAIN = 30673;

    private static final int LORAIN_LAMP = 5875;

    private static final int TITAN_LAMP_1 = 5876;

    private static final int TITAN_LAMP_2 = 5877;

    private static final int TITAN_LAMP_3 = 5878;

    private static final int TITAN_LAMP_4 = 5879;

    private static final int TITAN_LAMP_5 = 5880;

    private static final int[] REWARDS = new int[]{
            4553, 4554, 4555, 4556, 4557, 4558, 4559, 4560, 4561, 4562,
            4563, 4564};

    private static final int CATHEROK = 21035;

    public Q367_ElectrifyingRecharge() {
        super(367, "Electrifying Recharge!");
        setItemsIds(5875, 5876, 5877, 5878, 5879, 5880);
        addStartNpc(30673);
        addTalkId(30673);
        addSpellFinishedId(21035);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q367_ElectrifyingRecharge");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30673-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1);
        } else if (event.equalsIgnoreCase("30673-09.htm")) {
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1);
        } else if (event.equalsIgnoreCase("30673-08.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30673-07.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q367_ElectrifyingRecharge");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 37) ? "30673-02.htm" : "30673-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    if (st.hasQuestItems(5880)) {
                        htmltext = "30673-05.htm";
                        st.playSound("ItemSound.quest_accept");
                        st.takeItems(5880, 1);
                        st.giveItems(5875, 1);
                        break;
                    }
                    if (st.hasQuestItems(5876)) {
                        htmltext = "30673-04.htm";
                        st.takeItems(5876, 1);
                        break;
                    }
                    if (st.hasQuestItems(5877)) {
                        htmltext = "30673-04.htm";
                        st.takeItems(5877, 1);
                        break;
                    }
                    if (st.hasQuestItems(5878)) {
                        htmltext = "30673-04.htm";
                        st.takeItems(5878, 1);
                        break;
                    }
                    htmltext = "30673-03.htm";
                    break;
                }
                if (cond == 2 && st.hasQuestItems(5879)) {
                    htmltext = "30673-06.htm";
                    st.takeItems(5879, 1);
                    st.rewardItems(Rnd.get(REWARDS), 1);
                    st.playSound("ItemSound.quest_finish");
                }
                break;
        }
        return htmltext;
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (skill.getId() == 4072)
            if (st.hasQuestItems(5875)) {
                int randomItem = Rnd.get(5876, 5880);
                st.takeItems(5875, 1);
                st.giveItems(randomItem, 1);
                if (randomItem == 5879) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        return null;
    }
}
