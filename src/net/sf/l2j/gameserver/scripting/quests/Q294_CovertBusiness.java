package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q294_CovertBusiness extends Quest {
    private static final String qn = "Q294_CovertBusiness";

    private static final int BAT_FANG = 1491;

    private static final int RING_OF_RACCOON = 1508;

    public Q294_CovertBusiness() {
        super(294, "Covert Business");
        setItemsIds(1491);
        addStartNpc(30534);
        addTalkId(30534);
        addKillId(20370, 20480);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q294_CovertBusiness");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30534-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q294_CovertBusiness");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DWARF) {
                    htmltext = "30534-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30534-01.htm";
                    break;
                }
                htmltext = "30534-02.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30534-04.htm";
                    break;
                }
                htmltext = "30534-05.htm";
                st.takeItems(1491, -1);
                st.giveItems(1508, 1);
                st.rewardExpAndSp(0L, 600);
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
        int count = 1;
        int chance = Rnd.get(10);
        boolean isBarded = (npc.getNpcId() == 20370);
        if (chance < 3) {
            count++;
        } else if (chance < (isBarded ? 5 : 6)) {
            count += 2;
        } else if (isBarded && chance < 7) {
            count += 3;
        }
        if (st.dropItemsAlways(1491, count, 100))
            st.set("cond", "2");
        return null;
    }
}
