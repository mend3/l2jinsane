package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q651_RunawayYouth extends Quest {
    private static final String qn = "Q651_RunawayYouth";

    private static final int IVAN = 32014;

    private static final int BATIDAE = 31989;

    private static final int SCROLL_OF_ESCAPE = 736;

    private static final SpawnLocation[] SPAWNS = new SpawnLocation[]{new SpawnLocation(118600, -161235, -1119, 0), new SpawnLocation(108380, -150268, -2376, 0), new SpawnLocation(123254, -148126, -3425, 0)};

    private int _currentPosition = 0;

    public Q651_RunawayYouth() {
        super(651, "Runaway Youth");
        addStartNpc(32014);
        addTalkId(32014, 31989);
        addSpawn(32014, 118600, -161235, -1119, 0, false, 0L, false);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q651_RunawayYouth");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32014-04.htm")) {
            if (st.hasQuestItems(736)) {
                htmltext = "32014-03.htm";
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(736, 1);
                npc.broadcastPacket(new MagicSkillUse(npc, npc, 2013, 1, 3500, 0));
                startQuestTimer("apparition_npc", 4000L, npc, player, false);
            } else {
                st.exitQuest(true);
            }
        } else if (event.equalsIgnoreCase("apparition_npc")) {
            int chance = Rnd.get(3);
            while (chance == this._currentPosition)
                chance = Rnd.get(3);
            this._currentPosition = chance;
            npc.deleteMe();
            addSpawn(32014, SPAWNS[chance], false, 0L, false);
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q651_RunawayYouth");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 26) ? "32014-01.htm" : "32014-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 31989:
                        htmltext = "31989-01.htm";
                        st.rewardItems(57, 2883);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    case 32014:
                        htmltext = "32014-04a.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
