package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q316_DestroyPlagueCarriers extends Quest {
    private static final String qn = "Q316_DestroyPlagueCarriers";

    private static final int WERERAT_FANG = 1042;

    private static final int VAROOL_FOULCLAW_FANG = 1043;

    private static final int SUKAR_WERERAT = 20040;

    private static final int SUKAR_WERERAT_LEADER = 20047;

    private static final int VAROOL_FOULCLAW = 27020;

    public Q316_DestroyPlagueCarriers() {
        super(316, "Destroy Plague Carriers");
        setItemsIds(1042, 1043);
        addStartNpc(30155);
        addTalkId(30155);
        addKillId(20040, 20047, 27020);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q316_DestroyPlagueCarriers");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30155-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30155-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int ratFangs, varoolFangs;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q316_DestroyPlagueCarriers");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30155-00.htm";
                    break;
                }
                if (player.getLevel() < 18) {
                    htmltext = "30155-02.htm";
                    break;
                }
                htmltext = "30155-03.htm";
                break;
            case 1:
                ratFangs = st.getQuestItemsCount(1042);
                varoolFangs = st.getQuestItemsCount(1043);
                if (ratFangs + varoolFangs == 0) {
                    htmltext = "30155-05.htm";
                    break;
                }
                htmltext = "30155-07.htm";
                st.takeItems(1042, -1);
                st.takeItems(1043, -1);
                st.rewardItems(57, ratFangs * 30 + varoolFangs * 10000 + ((ratFangs > 10) ? 5000 : 0));
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
            case 20040:
            case 20047:
                st.dropItems(1042, 1, 0, 400000);
                break;
            case 27020:
                st.dropItems(1043, 1, 1, 200000);
                break;
        }
        return null;
    }
}
