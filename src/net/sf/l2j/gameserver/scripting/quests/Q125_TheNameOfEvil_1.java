package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public final class Q125_TheNameOfEvil_1 extends Quest {
    public static final String qn = "Q125_TheNameOfEvil_1";

    private static final int MUSHIKA = 32114;

    private static final int KARAKAWEI = 32117;

    private static final int ULU_KAIMU = 32119;

    private static final int BALU_KAIMU = 32120;

    private static final int CHUTA_KAIMU = 32121;

    private static final int ORNITHOMIMUS_CLAW = 8779;

    private static final int DEINONYCHUS_BONE = 8780;

    private static final int EPITAPH_OF_WISDOM = 8781;

    private static final int GAZKH_FRAGMENT = 8782;

    private static final int[] ORNITHOMIMUS = new int[]{22200, 22201, 22202, 22219, 22224, 22742, 22744};

    private static final int[] DEINONYCHUS = new int[]{16067, 22203, 22204, 22205, 22220, 22225, 22743, 22745};

    public Q125_TheNameOfEvil_1() {
        super(125, "The Name of Evil - 1");
        setItemsIds(8779, 8780, 8781, 8782);
        addStartNpc(32114);
        addTalkId(32114, 32117, 32119, 32120, 32121);
        for (int i : ORNITHOMIMUS) {
            addKillId(i);
        }
        for (int i : DEINONYCHUS) {
            addKillId(i);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q125_TheNameOfEvil_1");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32114-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32114-09.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8782, 1);
        } else if (event.equalsIgnoreCase("32117-08.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32117-14.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32119-14.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32120-15.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32121-16.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8782, -1);
            st.giveItems(8781, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState first;
        int cond;
        QuestState st = player.getQuestState("Q125_TheNameOfEvil_1");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                first = player.getQuestState("Q124_MeetingTheElroki");
                if (first != null && first.isCompleted() && player.getLevel() >= 76) {
                    htmltext = "32114-01.htm";
                    break;
                }
                htmltext = "32114-00.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 32114:
                        if (cond == 1) {
                            htmltext = "32114-07.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "32114-10.htm";
                            break;
                        }
                        if (cond > 2 && cond < 8) {
                            htmltext = "32114-11.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "32114-12.htm";
                            st.takeItems(8781, -1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 32117:
                        if (cond == 2) {
                            htmltext = "32117-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32117-09.htm";
                            break;
                        }
                        if (cond == 4) {
                            if (st.getQuestItemsCount(8779) >= 2 && st.getQuestItemsCount(8780) >= 2) {
                                htmltext = "32117-10.htm";
                                st.takeItems(8779, -1);
                                st.takeItems(8780, -1);
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "32117-09.htm";
                            st.set("cond", "3");
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "32117-15.htm";
                            break;
                        }
                        if (cond == 6 || cond == 7) {
                            htmltext = "32117-16.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "32117-17.htm";
                        break;
                    case 32119:
                        if (cond == 5) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
                            htmltext = "32119-01.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "32119-14.htm";
                        break;
                    case 32120:
                        if (cond == 6) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
                            htmltext = "32120-01.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "32120-16.htm";
                        break;
                    case 32121:
                        if (cond == 7) {
                            npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
                            htmltext = "32121-01.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "32121-17.htm";
                        break;
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "3");
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        if (ArraysUtil.contains(ORNITHOMIMUS, npcId)) {
            if (st.dropItems(8779, 1, 2, 50000) &&
                    st.getQuestItemsCount(8780) == 2)
                st.set("cond", "4");
        } else if (ArraysUtil.contains(DEINONYCHUS, npcId)) {
            if (st.dropItems(8780, 1, 2, 50000) &&
                    st.getQuestItemsCount(8779) == 2)
                st.set("cond", "4");
        }
        return null;
    }
}
