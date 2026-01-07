package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q273_InvadersOfTheHolyLand extends Quest {
    private static final String qn = "Q273_InvadersOfTheHolyLand";

    private static final int BLACK_SOULSTONE = 1475;

    private static final int RED_SOULSTONE = 1476;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    public Q273_InvadersOfTheHolyLand() {
        super(273, "Invaders of the Holy Land");
        setItemsIds(1475, 1476);
        addStartNpc(30566);
        addTalkId(30566);
        addKillId(20311, 20312, 20313);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q273_InvadersOfTheHolyLand");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30566-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30566-07.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int red, black, reward;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q273_InvadersOfTheHolyLand");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30566-00.htm";
                    break;
                }
                if (player.getLevel() < 6) {
                    htmltext = "30566-01.htm";
                    break;
                }
                htmltext = "30566-02.htm";
                break;
            case 1:
                red = st.getQuestItemsCount(1476);
                black = st.getQuestItemsCount(1475);
                if (red + black == 0) {
                    htmltext = "30566-04.htm";
                    break;
                }
                if (red == 0) {
                    htmltext = "30566-05.htm";
                } else {
                    htmltext = "30566-06.htm";
                }
                reward = black * 3 + red * 10 + ((black >= 10) ? ((red >= 1) ? 1800 : 1500) : 0);
                st.takeItems(1475, -1);
                st.takeItems(1476, -1);
                st.rewardItems(57, reward);
                if (player.isNewbie() && st.getInt("Reward") == 0) {
                    st.giveItems(5789, 6000);
                    st.playTutorialVoice("tutorial_voice_026");
                    st.set("Reward", "1");
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
        int npcId = npc.getNpcId();
        int probability = 77;
        if (npcId == 20311) {
            probability = 90;
        } else if (npcId == 20312) {
            probability = 87;
        }
        if (Rnd.get(100) <= probability) {
            st.dropItemsAlways(1475, 1, 0);
        } else {
            st.dropItemsAlways(1476, 1, 0);
        }
        return null;
    }
}
