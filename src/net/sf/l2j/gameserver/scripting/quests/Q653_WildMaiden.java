package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q653_WildMaiden extends Quest {
    private static final String qn = "Q653_WildMaiden";

    private static final int SUKI = 32013;

    private static final int GALIBREDO = 30181;

    private static final int SCROLL_OF_ESCAPE = 736;

    private static final SpawnLocation[] SPAWNS = new SpawnLocation[]{new SpawnLocation(66578, 72351, -3731, 0), new SpawnLocation(77189, 73610, -3708, 2555), new SpawnLocation(71809, 67377, -3675, 29130), new SpawnLocation(69166, 88825, -3447, 43886)};

    private int _currentPosition = 0;

    public Q653_WildMaiden() {
        super(653, "Wild Maiden");
        addStartNpc(32013);
        addTalkId(32013, 30181);
        addSpawn(32013, 66578, 72351, -3731, 0, false, 0L, false);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q653_WildMaiden");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32013-03.htm")) {
            if (st.hasQuestItems(736)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(736, 1);
                npc.broadcastPacket(new MagicSkillUse(npc, npc, 2013, 1, 3500, 0));
                startQuestTimer("apparition_npc", 4000L, npc, player, false);
            } else {
                htmltext = "32013-03a.htm";
                st.exitQuest(true);
            }
        } else if (event.equalsIgnoreCase("apparition_npc")) {
            int chance = Rnd.get(4);
            while (chance == this._currentPosition)
                chance = Rnd.get(4);
            this._currentPosition = chance;
            npc.deleteMe();
            addSpawn(32013, SPAWNS[chance], false, 0L, false);
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q653_WildMaiden");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 36) ? "32013-01.htm" : "32013-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30181:
                        htmltext = "30181-01.htm";
                        st.rewardItems(57, 2883);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    case 32013:
                        htmltext = "32013-04a.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
