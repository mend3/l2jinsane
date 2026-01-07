package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q638_SeekersOfTheHolyGrail extends Quest {
    private static final String qn = "Q638_SeekersOfTheHolyGrail";

    private static final int INNOCENTIN = 31328;

    private static final int PAGAN_TOTEM = 8068;

    public Q638_SeekersOfTheHolyGrail() {
        super(638, "Seekers of the Holy Grail");
        setItemsIds(8068);
        addStartNpc(31328);
        addTalkId(31328);
        for (int i = 22138; i < 22175; i++) {
            addKillId(i);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q638_SeekersOfTheHolyGrail");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31328-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31328-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q638_SeekersOfTheHolyGrail");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31328-00.htm" : "31328-01.htm";
                break;
            case 1:
                if (st.getQuestItemsCount(8068) >= 2000) {
                    htmltext = "31328-03.htm";
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(8068, 2000);
                    int chance = Rnd.get(3);
                    if (chance == 0) {
                        st.rewardItems(959, 1);
                        break;
                    }
                    if (chance == 1) {
                        st.rewardItems(960, 1);
                        break;
                    }
                    st.rewardItems(57, 3576000);
                    break;
                }
                htmltext = "31328-04.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItemsAlways(8068, 1, 0);
        return null;
    }
}
