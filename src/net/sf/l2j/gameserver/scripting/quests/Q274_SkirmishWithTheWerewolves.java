package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q274_SkirmishWithTheWerewolves extends Quest {
    private static final String qn = "Q274_SkirmishWithTheWerewolves";

    private static final int NECKLACE_OF_VALOR = 1507;

    private static final int NECKLACE_OF_COURAGE = 1506;

    private static final int MARAKU_WEREWOLF_HEAD = 1477;

    private static final int MARAKU_WOLFMEN_TOTEM = 1501;

    public Q274_SkirmishWithTheWerewolves() {
        super(274, "Skirmish with the Werewolves");
        setItemsIds(1477, 1501);
        addStartNpc(30569);
        addTalkId(30569);
        addKillId(20363, 20364);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState("Q274_SkirmishWithTheWerewolves");
        String htmltext = event;
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30569-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int amount;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q274_SkirmishWithTheWerewolves");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30569-00.htm";
                    break;
                }
                if (player.getLevel() < 9) {
                    htmltext = "30569-01.htm";
                    break;
                }
                if (st.hasAtLeastOneQuestItem(1506, 1507)) {
                    htmltext = "30569-02.htm";
                    break;
                }
                htmltext = "30569-07.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30569-04.htm";
                    break;
                }
                htmltext = "30569-05.htm";
                amount = 3500 + st.getQuestItemsCount(1501) * 600;
                st.takeItems(1477, -1);
                st.takeItems(1501, -1);
                st.rewardItems(57, amount);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItemsAlways(1477, 1, 40))
            st.set("cond", "2");
        if (Rnd.get(100) < 6)
            st.giveItems(1501, 1);
        return null;
    }
}
