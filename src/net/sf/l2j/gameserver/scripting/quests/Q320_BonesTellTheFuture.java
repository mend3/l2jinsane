package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q320_BonesTellTheFuture extends Quest {
    private static final String qn = "Q320_BonesTellTheFuture";

    private final int BONE_FRAGMENT = 809;

    public Q320_BonesTellTheFuture() {
        super(320, "Bones Tell the Future");
        setItemsIds(809);
        addStartNpc(30359);
        addTalkId(30359);
        addKillId(20517, 20518);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q320_BonesTellTheFuture");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30359-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q320_BonesTellTheFuture");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30359-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30359-02.htm";
                    break;
                }
                htmltext = "30359-03.htm";
                break;
            case 1:
                if (st.getInt("cond") == 1) {
                    htmltext = "30359-05.htm";
                    break;
                }
                htmltext = "30359-06.htm";
                st.takeItems(809, -1);
                st.rewardItems(57, 8470);
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
        if (st.dropItems(809, 1, 10, (npc.getNpcId() == 20517) ? 180000 : 200000))
            st.set("cond", "2");
        return null;
    }
}
