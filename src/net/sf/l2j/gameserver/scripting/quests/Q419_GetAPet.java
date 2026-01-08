package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Q419_GetAPet extends Quest {
    private static final String qn = "Q419_GetAPet";

    private static final int ANIMAL_LOVER_LIST = 3417;

    private static final int ANIMAL_SLAYER_LIST_1 = 3418;

    private static final int ANIMAL_SLAYER_LIST_2 = 3419;

    private static final int ANIMAL_SLAYER_LIST_3 = 3420;

    private static final int ANIMAL_SLAYER_LIST_4 = 3421;

    private static final int ANIMAL_SLAYER_LIST_5 = 3422;

    private static final int BLOODY_FANG = 3423;

    private static final int BLOODY_CLAW = 3424;

    private static final int BLOODY_NAIL = 3425;

    private static final int BLOODY_KASHA_FANG = 3426;

    private static final int BLOODY_TARANTULA_NAIL = 3427;

    private static final int WOLF_COLLAR = 2375;

    private static final int MARTIN = 30731;

    private static final int BELLA = 30256;

    private static final int METTY = 30072;

    private static final int ELLIE = 30091;

    private static final Map<Integer, int[]> DROPLIST = new HashMap<>();

    public Q419_GetAPet() {
        super(419, "Get a Pet");
        DROPLIST.put(20103, new int[]{3423, 600000});
        DROPLIST.put(20106, new int[]{3423, 750000});
        DROPLIST.put(20108, new int[]{3423, 1000000});
        DROPLIST.put(20460, new int[]{3424, 600000});
        DROPLIST.put(20308, new int[]{3424, 750000});
        DROPLIST.put(20466, new int[]{3424, 1000000});
        DROPLIST.put(20025, new int[]{3425, 600000});
        DROPLIST.put(20105, new int[]{3425, 750000});
        DROPLIST.put(20034, new int[]{3425, 1000000});
        DROPLIST.put(20474, new int[]{3426, 600000});
        DROPLIST.put(20476, new int[]{3426, 750000});
        DROPLIST.put(20478, new int[]{3426, 1000000});
        DROPLIST.put(20403, new int[]{3427, 750000});
        DROPLIST.put(20508, new int[]{3427, 1000000});
        setItemsIds(3417, 3418, 3419, 3420, 3421, 3422, 3423, 3424, 3425, 3426,
                3427);
        addStartNpc(30731);
        addTalkId(30731, 30256, 30091, 30072);
        for (int npcId : DROPLIST.keySet()) {
            addKillId(npcId);
        }
    }

    private static String checkQuestions(QuestState st) {
        int answers = st.getInt("correct") + st.getInt("wrong");
        if (answers < 10) {
            String[] questions = st.get("quiz").split(" ");
            int index = Rnd.get(questions.length - 1);
            String question = questions[index];
            if (questions.length > 10 - answers) {
                questions[index] = questions[questions.length - 1];
                st.set("quiz", String.join(" ", Arrays.<CharSequence>copyOf(questions, questions.length - 1)));
            }
            return "30731-" + question + ".htm";
        }
        if (st.getInt("wrong") > 0) {
            st.unset("progress");
            st.unset("answers");
            st.unset("quiz");
            st.unset("wrong");
            st.unset("correct");
            return "30731-14.htm";
        }
        st.takeItems(3417, 1);
        st.giveItems(2375, 1);
        st.playSound("ItemSound.quest_finish");
        st.exitQuest(true);
        return "30731-15.htm";
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q419_GetAPet");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("task")) {
            int race = player.getRace().ordinal();
            htmltext = "30731-0" + race + 4 + ".htm";
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3418 + race, 1);
        } else if (event.equalsIgnoreCase("30731-12.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3418, 1);
            st.takeItems(3419, 1);
            st.takeItems(3420, 1);
            st.takeItems(3421, 1);
            st.takeItems(3422, 1);
            st.takeItems(3423, -1);
            st.takeItems(3424, -1);
            st.takeItems(3425, -1);
            st.takeItems(3426, -1);
            st.takeItems(3427, -1);
            st.giveItems(3417, 1);
        } else if (event.equalsIgnoreCase("30256-03.htm")) {
            st.set("progress", String.valueOf(st.getInt("progress") | 0x1));
            if (st.getInt("progress") == 7)
                st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30072-02.htm")) {
            st.set("progress", String.valueOf(st.getInt("progress") | 0x2));
            if (st.getInt("progress") == 7)
                st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30091-02.htm")) {
            st.set("progress", String.valueOf(st.getInt("progress") | 0x4));
            if (st.getInt("progress") == 7)
                st.playSound("ItemSound.quest_middle");
        } else {
            if (event.equalsIgnoreCase("test")) {
                st.set("answers", "0");
                st.set("quiz", "20 21 22 23 24 25 26 27 28 29 30 31 32 33");
                return checkQuestions(st);
            }
            if (event.equalsIgnoreCase("wrong")) {
                st.set("wrong", String.valueOf(st.getInt("wrong") + 1));
                return checkQuestions(st);
            }
            if (event.equalsIgnoreCase("right")) {
                st.set("correct", String.valueOf(st.getInt("correct") + 1));
                return checkQuestions(st);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q419_GetAPet");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30731-01.htm" : "30731-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30731:
                        if (st.hasAtLeastOneQuestItem(3418, 3419, 3420, 3421, 3422)) {
                            int proofs = st.getQuestItemsCount(3423) + st.getQuestItemsCount(3424) + st.getQuestItemsCount(3425) + st.getQuestItemsCount(3426) + st.getQuestItemsCount(3427);
                            if (proofs == 0) {
                                htmltext = "30731-09.htm";
                                break;
                            }
                            if (proofs < 50) {
                                htmltext = "30731-10.htm";
                                break;
                            }
                            htmltext = "30731-11.htm";
                            break;
                        }
                        if (st.getInt("progress") == 7) {
                            htmltext = "30731-13.htm";
                            break;
                        }
                        htmltext = "30731-16.htm";
                        break;
                    case 30256:
                        htmltext = "30256-01.htm";
                        break;
                    case 30072:
                        htmltext = "30072-01.htm";
                        break;
                    case 30091:
                        htmltext = "30091-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int[] drop = DROPLIST.get(npc.getNpcId());
        if (st.hasQuestItems(drop[0] - 5))
            st.dropItems(drop[0], 1, 50, drop[1]);
        return null;
    }
}
