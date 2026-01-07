package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q247_PossessorOfAPreciousSoul extends Quest {
    private static final String qn = "Q247_PossessorOfAPreciousSoul";

    private static final int CARADINE = 31740;

    private static final int LADY_OF_THE_LAKE = 31745;

    private static final int CARADINE_LETTER = 7679;

    private static final int NOBLESS_TIARA = 7694;

    public Q247_PossessorOfAPreciousSoul() {
        super(247, "Possessor of a Precious Soul - 4");
        addStartNpc(31740);
        addTalkId(31740, 31745);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q247_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31740-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.takeItems(7679, 1);
        } else if (event.equalsIgnoreCase("31740-05.htm")) {
            st.set("cond", "2");
            player.teleportTo(143209, 43968, -3038, 0);
        } else if (event.equalsIgnoreCase("31745-05.htm")) {
            player.setNoble(true, true);
            st.giveItems(7694, 1);
            st.rewardExpAndSp(93836L, 0);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q247_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (st.hasQuestItems(7679))
                    htmltext = (!player.isSubClassActive() || player.getLevel() < 75) ? "31740-02.htm" : "31740-01.htm";
                break;
            case 1:
                if (!player.isSubClassActive())
                    break;
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31740:
                        if (cond == 1) {
                            htmltext = "31740-04.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31740-06.htm";
                        break;
                    case 31745:
                        if (cond == 2)
                            htmltext = (player.getLevel() < 75) ? "31745-06.htm" : "31745-01.htm";
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
