package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q169_OffspringOfNightmares extends Quest {
    private static final String qn = "Q169_OffspringOfNightmares";

    private static final int CRACKED_SKULL = 1030;

    private static final int PERFECT_SKULL = 1031;

    private static final int BONE_GAITERS = 31;

    public Q169_OffspringOfNightmares() {
        super(169, "Offspring of Nightmares");
        setItemsIds(1030, 1031);
        addStartNpc(30145);
        addTalkId(30145);
        addKillId(20105, 20025);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q169_OffspringOfNightmares");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30145-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30145-08.htm")) {
            int reward = 17000 + st.getQuestItemsCount(1030) * 20;
            st.takeItems(1031, -1);
            st.takeItems(1030, -1);
            st.giveItems(31, 1);
            st.rewardItems(57, reward);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q169_OffspringOfNightmares");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30145-00.htm";
                    break;
                }
                if (player.getLevel() < 15) {
                    htmltext = "30145-02.htm";
                    break;
                }
                htmltext = "30145-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    if (st.hasQuestItems(1030)) {
                        htmltext = "30145-06.htm";
                        break;
                    }
                    htmltext = "30145-05.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "30145-07.htm";
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (st.getInt("cond") == 1 && st.dropItems(1031, 1, 1, 200000)) {
            st.set("cond", "2");
        } else {
            st.dropItems(1030, 1, 0, 500000);
        }
        return null;
    }
}
