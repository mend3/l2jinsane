package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q257_TheGuardIsBusy extends Quest {
    private static final String qn = "Q257_TheGuardIsBusy";

    private static final int GLUDIO_LORD_MARK = 1084;

    private static final int ORC_AMULET = 752;

    private static final int ORC_NECKLACE = 1085;

    private static final int WEREWOLF_FANG = 1086;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    public Q257_TheGuardIsBusy() {
        super(257, "The Guard Is Busy");
        setItemsIds(752, 1085, 1086, 1084);
        addStartNpc(30039);
        addTalkId(30039);
        addKillId(20006, 20093, 20096, 20098, 20130, 20131, 20132, 20342, 20343);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q257_TheGuardIsBusy");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30039-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1084, 1);
        } else if (event.equalsIgnoreCase("30039-05.htm")) {
            st.takeItems(1084, 1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int amulets, necklaces, fangs, reward;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q257_TheGuardIsBusy");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 6) ? "30039-01.htm" : "30039-02.htm";
                break;
            case 1:
                amulets = st.getQuestItemsCount(752);
                necklaces = st.getQuestItemsCount(1085);
                fangs = st.getQuestItemsCount(1086);
                if (amulets + necklaces + fangs == 0) {
                    htmltext = "30039-04.htm";
                    break;
                }
                htmltext = "30039-07.htm";
                st.takeItems(752, -1);
                st.takeItems(1085, -1);
                st.takeItems(1086, -1);
                reward = 10 * amulets + 20 * (necklaces + fangs);
                if (amulets + necklaces + fangs >= 10)
                    reward += 1000;
                st.rewardItems(57, reward);
                if (player.isNewbie() && st.getInt("Reward") == 0) {
                    st.showQuestionMark(26);
                    st.set("Reward", "1");
                    if (player.isMageClass()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000);
                        break;
                    }
                    st.playTutorialVoice("tutorial_voice_026");
                    st.giveItems(5789, 6000);
                }
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
            case 20006:
            case 20130:
            case 20131:
                st.dropItems(752, 1, 0, 500000);
                break;
            case 20093:
            case 20096:
            case 20098:
                st.dropItems(1085, 1, 0, 500000);
                break;
            case 20342:
                st.dropItems(1086, 1, 0, 200000);
                break;
            case 20343:
                st.dropItems(1086, 1, 0, 400000);
                break;
            case 20132:
                st.dropItems(1086, 1, 0, 500000);
                break;
        }
        return null;
    }
}
