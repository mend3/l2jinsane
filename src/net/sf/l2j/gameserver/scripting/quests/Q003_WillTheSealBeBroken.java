package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q003_WillTheSealBeBroken extends Quest {
    private static final String qn = "Q003_WillTheSealBeBroken";

    private static final int ONYX_BEAST_EYE = 1081;

    private static final int TAINT_STONE = 1082;

    private static final int SUCCUBUS_BLOOD = 1083;

    private static final int SCROLL_ENCHANT_ARMOR_D = 956;

    public Q003_WillTheSealBeBroken() {
        super(3, "Will the Seal be Broken?");
        setItemsIds(1081, 1082, 1083);
        addStartNpc(30141);
        addTalkId(30141);
        addKillId(20031, 20041, 20046, 20048, 20052, 20057);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q003_WillTheSealBeBroken");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30141-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q003_WillTheSealBeBroken");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30141-00.htm";
                    break;
                }
                if (player.getLevel() < 16) {
                    htmltext = "30141-01.htm";
                    break;
                }
                htmltext = "30141-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30141-04.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30141-06.htm";
                    st.takeItems(1081, 1);
                    st.takeItems(1083, 1);
                    st.takeItems(1082, 1);
                    st.giveItems(956, 1);
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20031:
                if (st.dropItemsAlways(1081, 1, 1) && st.hasQuestItems(1082, 1083))
                    st.set("cond", "2");
                break;
            case 20041:
            case 20046:
                if (st.dropItemsAlways(1082, 1, 1) && st.hasQuestItems(1081, 1083))
                    st.set("cond", "2");
                break;
            case 20048:
            case 20052:
            case 20057:
                if (st.dropItemsAlways(1083, 1, 1) && st.hasQuestItems(1081, 1082))
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
