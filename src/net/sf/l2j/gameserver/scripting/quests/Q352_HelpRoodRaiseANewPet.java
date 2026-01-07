package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q352_HelpRoodRaiseANewPet extends Quest {
    private static final String qn = "Q352_HelpRoodRaiseANewPet";

    private static final int LIENRIK_EGG_1 = 5860;

    private static final int LIENRIK_EGG_2 = 5861;

    public Q352_HelpRoodRaiseANewPet() {
        super(352, "Help Rood Raise A New Pet!");
        setItemsIds(5860, 5861);
        addStartNpc(31067);
        addTalkId(31067);
        addKillId(20786, 20787, 21644, 21645);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q352_HelpRoodRaiseANewPet");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31067-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31067-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int eggs1, eggs2, reward;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q352_HelpRoodRaiseANewPet");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 39) ? "31067-00.htm" : "31067-01.htm";
                break;
            case 1:
                eggs1 = st.getQuestItemsCount(5860);
                eggs2 = st.getQuestItemsCount(5861);
                if (eggs1 + eggs2 == 0) {
                    htmltext = "31067-05.htm";
                    break;
                }
                reward = 2000;
                if (eggs1 > 0 && eggs2 == 0) {
                    htmltext = "31067-06.htm";
                    reward += eggs1 * 34;
                    st.takeItems(5860, -1);
                    st.rewardItems(57, reward);
                    break;
                }
                if (eggs1 == 0 && eggs2 > 0) {
                    htmltext = "31067-08.htm";
                    reward += eggs2 * 1025;
                    st.takeItems(5861, -1);
                    st.rewardItems(57, reward);
                    break;
                }
                if (eggs1 > 0 && eggs2 > 0) {
                    htmltext = "31067-08.htm";
                    reward += eggs1 * 34 + eggs2 * 1025 + 2000;
                    st.takeItems(5860, -1);
                    st.takeItems(5861, -1);
                    st.rewardItems(57, reward);
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
        int random = Rnd.get(100);
        int chance = (npcId == 20786 || npcId == 21644) ? 44 : 58;
        if (random < chance) {
            st.dropItemsAlways(5860, 1, 0);
        } else if (random < chance + 4) {
            st.dropItemsAlways(5861, 1, 0);
        }
        return null;
    }
}
