package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q292_BrigandsSweep extends Quest {
    private static final String qn = "Q292_BrigandsSweep";

    private static final int SPIRON = 30532;

    private static final int BALANKI = 30533;

    private static final int GOBLIN_NECKLACE = 1483;

    private static final int GOBLIN_PENDANT = 1484;

    private static final int GOBLIN_LORD_PENDANT = 1485;

    private static final int SUSPICIOUS_MEMO = 1486;

    private static final int SUSPICIOUS_CONTRACT = 1487;

    private static final int GOBLIN_BRIGAND = 20322;

    private static final int GOBLIN_BRIGAND_LEADER = 20323;

    private static final int GOBLIN_BRIGAND_LIEUTENANT = 20324;

    private static final int GOBLIN_SNOOPER = 20327;

    private static final int GOBLIN_LORD = 20528;

    public Q292_BrigandsSweep() {
        super(292, "Brigands Sweep");
        setItemsIds(1483, 1484, 1485, 1486, 1487);
        addStartNpc(30532);
        addTalkId(30532, 30533);
        addKillId(20322, 20323, 20324, 20327, 20528);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q292_BrigandsSweep");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30532-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30532-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int goblinNecklaces, goblinPendants, goblinLordPendants, suspiciousMemos, countAll;
        boolean hasContract;
        QuestState st = player.getQuestState("Q292_BrigandsSweep");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DWARF) {
                    htmltext = "30532-00.htm";
                    break;
                }
                if (player.getLevel() < 5) {
                    htmltext = "30532-01.htm";
                    break;
                }
                htmltext = "30532-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30532:
                        goblinNecklaces = st.getQuestItemsCount(1483);
                        goblinPendants = st.getQuestItemsCount(1484);
                        goblinLordPendants = st.getQuestItemsCount(1485);
                        suspiciousMemos = st.getQuestItemsCount(1486);
                        countAll = goblinNecklaces + goblinPendants + goblinLordPendants;
                        hasContract = st.hasQuestItems(1487);
                        if (countAll == 0) {
                            htmltext = "30532-04.htm";
                            break;
                        }
                        if (hasContract) {
                            htmltext = "30532-10.htm";
                        } else if (suspiciousMemos > 0) {
                            if (suspiciousMemos > 1) {
                                htmltext = "30532-09.htm";
                            } else {
                                htmltext = "30532-08.htm";
                            }
                        } else {
                            htmltext = "30532-05.htm";
                        }
                        st.takeItems(1483, -1);
                        st.takeItems(1484, -1);
                        st.takeItems(1485, -1);
                        if (hasContract) {
                            st.set("cond", "1");
                            st.takeItems(1487, -1);
                        }
                        st.rewardItems(57, 12 * goblinNecklaces + 36 * goblinPendants + 33 * goblinLordPendants + ((countAll >= 10) ? 1000 : 0) + (hasContract ? 1120 : 0));
                        break;
                    case 30533:
                        if (!st.hasQuestItems(1487)) {
                            htmltext = "30533-01.htm";
                            break;
                        }
                        htmltext = "30533-02.htm";
                        st.set("cond", "1");
                        st.takeItems(1487, -1);
                        st.rewardItems(57, 1500);
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
        int chance = Rnd.get(10);
        if (chance > 5) {
            switch (npc.getNpcId()) {
                case 20322:
                case 20324:
                case 20327:
                    st.dropItemsAlways(1483, 1, 0);
                    break;
                case 20323:
                    st.dropItemsAlways(1484, 1, 0);
                    break;
                case 20528:
                    st.dropItemsAlways(1485, 1, 0);
                    break;
            }
        } else if (chance > 4 && st.getInt("cond") == 1 && st.dropItemsAlways(1486, 1, 3)) {
            st.set("cond", "2");
            st.takeItems(1486, -1);
            st.giveItems(1487, 1);
        }
        return null;
    }
}
