package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q374_WhisperOfDreams_Part1 extends Quest {
    private static final String qn = "Q374_WhisperOfDreams_Part1";

    private static final int MANAKIA = 30515;

    private static final int TORAI = 30557;

    private static final int CAVE_BEAST = 20620;

    private static final int DEATH_WAVE = 20621;

    private static final int CAVE_BEAST_TOOTH = 5884;

    private static final int DEATH_WAVE_LIGHT = 5885;

    private static final int SEALED_MYSTERIOUS_STONE = 5886;

    private static final int MYSTERIOUS_STONE = 5887;

    private static final int[][] REWARDS = new int[][]{{5486, 3, 2950}, {5487, 2, 18050}, {5488, 2, 18050}, {5485, 4, 10450}, {5489, 6, 15550}};

    public Q374_WhisperOfDreams_Part1() {
        super(374, "Whisper of Dreams, Part 1");
        setItemsIds(5885, 5884, 5886, 5887);
        addStartNpc(30515);
        addTalkId(30515, 30557);
        addKillId(20620, 20621);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q374_WhisperOfDreams_Part1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30515-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("condStone", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.startsWith("30515-06-")) {
            if (st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65) {
                htmltext = "30515-06.htm";
                st.playSound("ItemSound.quest_middle");
                int[] reward = REWARDS[Integer.parseInt(event.substring(9, 10))];
                st.takeItems(5884, -1);
                st.takeItems(5885, -1);
                st.rewardItems(57, reward[2]);
                st.giveItems(reward[0], reward[1]);
            } else {
                htmltext = "30515-07.htm";
            }
        } else if (event.equalsIgnoreCase("30515-08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30557-02.htm")) {
            if (st.getInt("cond") == 2 && st.hasQuestItems(5886)) {
                st.set("cond", "3");
                st.takeItems(5886, -1);
                st.giveItems(5887, 1);
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "30557-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q374_WhisperOfDreams_Part1");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 56) ? "30515-01.htm" : "30515-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30515:
                        if (!st.hasQuestItems(5886)) {
                            if (st.getQuestItemsCount(5884) >= 65 && st.getQuestItemsCount(5885) >= 65) {
                                htmltext = "30515-05.htm";
                                break;
                            }
                            htmltext = "30515-04.htm";
                            break;
                        }
                        if (cond == 1) {
                            htmltext = "30515-09.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        htmltext = "30515-10.htm";
                        break;
                    case 30557:
                        if (cond == 2 && st.hasQuestItems(5886))
                            htmltext = "30557-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems((npc.getNpcId() == 20620) ? 5884 : 5885, 1, 65, 500000);
        st = getRandomPartyMember(player, npc, "condStone", "1");
        if (st == null)
            return null;
        if (st.dropItems(5886, 1, 1, 1000))
            st.unset("condStone");
        return null;
    }
}
