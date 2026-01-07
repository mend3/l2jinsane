package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q111_ElrokianHuntersProof extends Quest {
    private static final String qn = "Q111_ElrokianHuntersProof";

    private static final int MARQUEZ = 32113;

    private static final int MUSHIKA = 32114;

    private static final int ASAMAH = 32115;

    private static final int KIRIKASHIN = 32116;

    private static final int FRAGMENT = 8768;

    private static final int EXPEDITION_LETTER = 8769;

    private static final int CLAW = 8770;

    private static final int BONE = 8771;

    private static final int SKIN = 8772;

    private static final int PRACTICE_TRAP = 8773;

    public Q111_ElrokianHuntersProof() {
        super(111, "Elrokian Hunter's Proof");
        setItemsIds(8768, 8769, 8770, 8771, 8772, 8773);
        addStartNpc(32113);
        addTalkId(32113, 32114, 32115, 32116);
        addKillId(22196, 22197, 22198, 22218, 22200, 22201, 22202, 22219, 22208, 22209,
                22210, 22221, 22203, 22204, 22205, 22220);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q111_ElrokianHuntersProof");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("32113-002.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32115-002.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32113-009.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32113-018.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8768, -1);
            st.giveItems(8769, 1);
        } else if (event.equalsIgnoreCase("32116-003.htm")) {
            st.set("cond", "7");
            st.playSound("EtcSound.elcroki_song_full");
        } else if (event.equalsIgnoreCase("32116-005.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32115-004.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32115-006.htm")) {
            st.set("cond", "10");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32116-007.htm")) {
            st.takeItems(8773, 1);
            st.giveItems(8763, 1);
            st.giveItems(8764, 100);
            st.rewardItems(57, 1022636);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q111_ElrokianHuntersProof");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "32113-000.htm" : "32113-001.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 32113:
                        if (cond == 1 || cond == 2) {
                            htmltext = "32113-002.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32113-003.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "32113-009.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "32113-010.htm";
                        break;
                    case 32114:
                        if (cond == 1) {
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                        }
                        htmltext = "32114-001.htm";
                        break;
                    case 32115:
                        if (cond == 2) {
                            htmltext = "32115-001.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "32115-002.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "32115-003.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "32115-004.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "32115-006.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "32115-007.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(8771, -1);
                            st.takeItems(8770, -1);
                            st.takeItems(8772, -1);
                            st.giveItems(8773, 1);
                        }
                        break;
                    case 32116:
                        if (cond < 6) {
                            htmltext = "32116-008.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "32116-001.htm";
                            st.takeItems(8769, 1);
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "32116-004.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "32116-006.htm";
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
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 22196:
            case 22197:
            case 22198:
            case 22218:
                if (st.getInt("cond") == 4 && st.dropItems(8768, 1, 50, 250000))
                    st.set("cond", "5");
                break;
            case 22200:
            case 22201:
            case 22202:
            case 22219:
                if (st.getInt("cond") == 10 && st.dropItems(8770, 1, 10, 650000) &&
                        st.getQuestItemsCount(8771) >= 10 && st.getQuestItemsCount(8772) >= 10)
                    st.set("cond", "11");
                break;
            case 22208:
            case 22209:
            case 22210:
            case 22221:
                if (st.getInt("cond") == 10 && st.dropItems(8772, 1, 10, 650000) &&
                        st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8771) >= 10)
                    st.set("cond", "11");
                break;
            case 22203:
            case 22204:
            case 22205:
            case 22220:
                if (st.getInt("cond") == 10 && st.dropItems(8771, 1, 10, 650000) &&
                        st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8772) >= 10)
                    st.set("cond", "11");
                break;
        }
        return null;
    }
}
