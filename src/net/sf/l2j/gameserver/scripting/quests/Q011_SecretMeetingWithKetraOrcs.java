package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q011_SecretMeetingWithKetraOrcs extends Quest {
    private static final String qn = "Q011_SecretMeetingWithKetraOrcs";

    private static final int CADMON = 31296;

    private static final int LEON = 31256;

    private static final int WAHKAN = 31371;

    private static final int MUNITIONS_BOX = 7231;

    public Q011_SecretMeetingWithKetraOrcs() {
        super(11, "Secret Meeting With Ketra Orcs");
        setItemsIds(7231);
        addStartNpc(31296);
        addTalkId(31296, 31256, 31371);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q011_SecretMeetingWithKetraOrcs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31296-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31256-02.htm")) {
            st.giveItems(7231, 1);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31371-02.htm")) {
            st.takeItems(7231, 1);
            st.rewardExpAndSp(79787L, 0);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q011_SecretMeetingWithKetraOrcs");
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
                    case 31256:
                        if (cond == 1) {
                            htmltext = "31256-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31256-03.htm";
                        break;
                    case 31371:
                        if (cond == 2)
                            htmltext = "31371-01.htm";
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
