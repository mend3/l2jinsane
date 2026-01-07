package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q642_APowerfulPrimevalCreature extends Quest {
    private static final String qn = "Q642_APowerfulPrimevalCreature";

    private static final int DINOSAUR_TISSUE = 8774;

    private static final int DINOSAUR_EGG = 8775;

    private static final int ANCIENT_EGG = 18344;

    private static final int[] REWARDS = new int[]{
            8690, 8692, 8694, 8696, 8698, 8700, 8702, 8704, 8706, 8708,
            8710};

    public Q642_APowerfulPrimevalCreature() {
        super(642, "A Powerful Primeval Creature");
        setItemsIds(8774, 8775);
        addStartNpc(32105);
        addTalkId(32105);
        addKillId(22196, 22197, 22198, 22199, 22200, 22201, 22202, 22203, 22204, 22205,
                22218, 22219, 22220, 22223, 22224, 22225, 18344);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q642_APowerfulPrimevalCreature");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32105-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32105-08.htm")) {
            if (st.getQuestItemsCount(8774) >= 150 && st.hasQuestItems(8775))
                htmltext = "32105-06.htm";
        } else if (event.equalsIgnoreCase("32105-07.htm")) {
            int tissues = st.getQuestItemsCount(8774);
            if (tissues > 0) {
                st.takeItems(8774, -1);
                st.rewardItems(57, tissues * 5000);
            } else {
                htmltext = "32105-08.htm";
            }
        } else if (event.contains("event_")) {
            if (st.getQuestItemsCount(8774) >= 150 && st.hasQuestItems(8775)) {
                htmltext = "32105-07.htm";
                st.takeItems(8774, 150);
                st.takeItems(8775, 1);
                st.rewardItems(57, 44000);
                st.giveItems(REWARDS[Integer.parseInt(event.split("_")[1])], 1);
            } else {
                htmltext = "32105-08.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q642_APowerfulPrimevalCreature");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "32105-00.htm" : "32105-01.htm";
                break;
            case 1:
                htmltext = !st.hasQuestItems(8774) ? "32105-08.htm" : "32105-05.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        if (npc.getNpcId() == 18344) {
            if (Rnd.get(100) < 1) {
                st.giveItems(8775, 1);
                if (st.getQuestItemsCount(8774) >= 150) {
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (Rnd.get(100) < 33) {
            st.rewardItems(8774, 1);
            if (st.getQuestItemsCount(8774) >= 150 && st.hasQuestItems(8775)) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
