package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q012_SecretMeetingWithVarkaSilenos extends Quest {
    private static final String qn = "Q012_SecretMeetingWithVarkaSilenos";

    private static final int CADMON = 31296;

    private static final int HELMUT = 31258;

    private static final int NARAN_ASHANUK = 31378;

    private static final int MUNITIONS_BOX = 7232;

    public Q012_SecretMeetingWithVarkaSilenos() {
        super(12, "Secret Meeting With Varka Silenos");
        setItemsIds(7232);
        addStartNpc(31296);
        addTalkId(31296, 31258, 31378);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q012_SecretMeetingWithVarkaSilenos");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31296-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31258-02.htm")) {
            st.giveItems(7232, 1);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31378-02.htm")) {
            st.takeItems(7232, 1);
            st.rewardExpAndSp(79761L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q012_SecretMeetingWithVarkaSilenos");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 74) ? "31296-02.htm" : "31296-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31296:
                        if (cond == 1)
                            htmltext = "31296-04.htm";
                        break;
                    case 31258:
                        if (cond == 1) {
                            htmltext = "31258-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31258-03.htm";
                        break;
                    case 31378:
                        if (cond == 2)
                            htmltext = "31378-01.htm";
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
