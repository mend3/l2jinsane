package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q265_ChainsOfSlavery extends Quest {
    private static final String qn = "Q265_ChainsOfSlavery";

    private static final int SHACKLE = 1368;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    public Q265_ChainsOfSlavery() {
        super(265, "Chains of Slavery");
        setItemsIds(1368);
        addStartNpc(30357);
        addTalkId(30357);
        addKillId(20004, 20005);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q265_ChainsOfSlavery");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30357-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30357-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int shackles, reward;
        QuestState st = player.getQuestState("Q265_ChainsOfSlavery");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30357-00.htm";
                    break;
                }
                if (player.getLevel() < 6) {
                    htmltext = "30357-01.htm";
                    break;
                }
                htmltext = "30357-02.htm";
                break;
            case 1:
                shackles = st.getQuestItemsCount(1368);
                if (shackles == 0) {
                    htmltext = "30357-04.htm";
                    break;
                }
                reward = 12 * shackles;
                if (shackles > 10)
                    reward += 500;
                htmltext = "30357-05.htm";
                st.takeItems(1368, -1);
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
        st.dropItems(1368, 1, 0, (npc.getNpcId() == 20004) ? 500000 : 600000);
        return null;
    }
}
