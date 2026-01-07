package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q102_SeaOfSporesFever extends Quest {
    private static final String qn = "Q102_SeaOfSporesFever";

    private static final int ALBERIUS_LETTER = 964;

    private static final int EVERGREEN_AMULET = 965;

    private static final int DRYAD_TEARS = 966;

    private static final int ALBERIUS_LIST = 746;

    private static final int COBENDELL_MEDICINE_1 = 1130;

    private static final int COBENDELL_MEDICINE_2 = 1131;

    private static final int COBENDELL_MEDICINE_3 = 1132;

    private static final int COBENDELL_MEDICINE_4 = 1133;

    private static final int COBENDELL_MEDICINE_5 = 1134;

    private static final int SPIRITSHOT_NO_GRADE = 2509;

    private static final int SOULSHOT_NO_GRADE = 1835;

    private static final int SWORD_OF_SENTINEL = 743;

    private static final int STAFF_OF_SENTINEL = 744;

    private static final int LESSER_HEALING_POT = 1060;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int ALBERIUS = 30284;

    private static final int COBENDELL = 30156;

    private static final int BERROS = 30217;

    private static final int VELTRESS = 30219;

    private static final int RAYEN = 30221;

    private static final int GARTRANDELL = 30285;

    public Q102_SeaOfSporesFever() {
        super(102, "Sea of Spores Fever");
        setItemsIds(964, 965, 966, 1130, 1131, 1132, 1133, 1134, 746);
        addStartNpc(30284);
        addTalkId(30284, 30156, 30217, 30221, 30285, 30219);
        addKillId(20013, 20019);
    }

    private static void checkItem(QuestState st, int itemId) {
        if (st.hasQuestItems(itemId)) {
            st.takeItems(itemId, 1);
            int medicinesLeft = st.getInt("medicines") - 1;
            if (medicinesLeft == 0) {
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
            } else {
                st.set("medicines", String.valueOf(medicinesLeft));
            }
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q102_SeaOfSporesFever");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30284-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(964, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q102_SeaOfSporesFever");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30284-00.htm";
                    break;
                }
                if (player.getLevel() < 12) {
                    htmltext = "30284-08.htm";
                    break;
                }
                htmltext = "30284-07.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30284:
                        if (cond == 1) {
                            htmltext = "30284-03.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30284-09.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30284-04.htm";
                            st.set("cond", "5");
                            st.set("medicines", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1130, 1);
                            st.giveItems(746, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30284-05.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30284-06.htm";
                            st.takeItems(746, 1);
                            if (player.isMageClass()) {
                                st.giveItems(744, 1);
                                st.rewardItems(2509, 500);
                            } else {
                                st.giveItems(743, 1);
                                st.rewardItems(1835, 1000);
                            }
                            st.giveItems(1060, 100);
                            st.giveItems(4412, 10);
                            st.giveItems(4413, 10);
                            st.giveItems(4414, 10);
                            st.giveItems(4415, 10);
                            st.giveItems(4416, 10);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30156:
                        if (cond == 1) {
                            htmltext = "30156-03.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(964, 1);
                            st.giveItems(965, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30156-04.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30156-07.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30156-05.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(966, -1);
                            st.takeItems(965, 1);
                            st.giveItems(1130, 1);
                            st.giveItems(1131, 1);
                            st.giveItems(1132, 1);
                            st.giveItems(1133, 1);
                            st.giveItems(1134, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30156-06.htm";
                        break;
                    case 30217:
                        if (cond == 5) {
                            htmltext = "30217-01.htm";
                            checkItem(st, 1131);
                        }
                        break;
                    case 30219:
                        if (cond == 5) {
                            htmltext = "30219-01.htm";
                            checkItem(st, 1132);
                        }
                        break;
                    case 30221:
                        if (cond == 5) {
                            htmltext = "30221-01.htm";
                            checkItem(st, 1133);
                        }
                        break;
                    case 30285:
                        if (cond == 5) {
                            htmltext = "30285-01.htm";
                            checkItem(st, 1134);
                        }
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "2");
        if (st == null)
            return null;
        if (st.dropItems(966, 1, 10, 300000))
            st.set("cond", "3");
        return null;
    }
}
