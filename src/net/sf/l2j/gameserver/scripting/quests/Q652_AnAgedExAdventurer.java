package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q652_AnAgedExAdventurer extends Quest {
    private static final String qn = "Q652_AnAgedExAdventurer";

    private static final int TANTAN = 32012;

    private static final int SARA = 30180;

    private static final int SOULSHOT_C = 1464;

    private static final int ENCHANT_ARMOR_D = 956;

    private static final SpawnLocation[] SPAWNS = new SpawnLocation[]{new SpawnLocation(78355, -1325, -3659, 0), new SpawnLocation(79890, -6132, -2922, 0), new SpawnLocation(90012, -7217, -3085, 0), new SpawnLocation(94500, -10129, -3290, 0), new SpawnLocation(96534, -1237, -3677, 0)};

    private int _currentPosition = 0;

    public Q652_AnAgedExAdventurer() {
        super(652, "An Aged Ex-Adventurer");
        addStartNpc(32012);
        addTalkId(32012, 30180);
        addSpawn(32012, 78355, -1325, -3659, 0, false, 0L, false);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q652_AnAgedExAdventurer");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32012-02.htm")) {
            if (st.getQuestItemsCount(1464) >= 100) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(1464, 100);
                npc.getAI().setIntention(IntentionType.MOVE_TO, new Location(85326, 7869, -3620));
                startQuestTimer("apparition_npc", 6000L, npc, player, false);
            } else {
                htmltext = "32012-02a.htm";
                st.exitQuest(true);
            }
        } else if (event.equalsIgnoreCase("apparition_npc")) {
            int chance = Rnd.get(5);
            while (chance == this._currentPosition)
                chance = Rnd.get(5);
            this._currentPosition = chance;
            npc.deleteMe();
            addSpawn(32012, SPAWNS[chance], false, 0L, false);
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q652_AnAgedExAdventurer");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 46) ? "32012-00.htm" : "32012-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30180:
                        if (Rnd.get(100) < 50) {
                            htmltext = "30180-01.htm";
                            st.rewardItems(57, 5026);
                            st.giveItems(956, 1);
                        } else {
                            htmltext = "30180-02.htm";
                            st.rewardItems(57, 10000);
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    case 32012:
                        htmltext = "32012-04a.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }
}
