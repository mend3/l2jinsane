package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q107_MercilessPunishment extends Quest {
    private static final String qn = "Q107_MercilessPunishment";

    private static final int HATOS = 30568;

    private static final int PARUGON = 30580;

    private static final int HATOS_ORDER_1 = 1553;

    private static final int HATOS_ORDER_2 = 1554;

    private static final int HATOS_ORDER_3 = 1555;

    private static final int LETTER_TO_HUMAN = 1557;

    private static final int LETTER_TO_DARKELF = 1556;

    private static final int LETTER_TO_ELF = 1558;

    private static final int BUTCHER_SWORD = 1510;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int LESSER_HEALING_POTION = 1060;

    public Q107_MercilessPunishment() {
        super(107, "Merciless Punishment");
        setItemsIds(1553, 1554, 1555, 1557, 1556, 1558);
        addStartNpc(30568);
        addTalkId(30568, 30580);
        addKillId(27041);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState("Q107_MercilessPunishment");
        String htmltext = event;
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30568-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1553, 1);
        } else if (event.equalsIgnoreCase("30568-06.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30568-07.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1553, 1);
            st.giveItems(1554, 1);
        } else if (event.equalsIgnoreCase("30568-09.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1554, 1);
            st.giveItems(1555, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q107_MercilessPunishment");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ORC) {
                    htmltext = "30568-00.htm";
                    break;
                }
                if (player.getLevel() < 12) {
                    htmltext = "30568-01.htm";
                    break;
                }
                htmltext = "30568-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30568:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30568-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30568-05.htm";
                            break;
                        }
                        if (cond == 4 || cond == 6) {
                            htmltext = "30568-09.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30568-08.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30568-10.htm";
                            st.takeItems(1555, -1);
                            st.takeItems(1556, -1);
                            st.takeItems(1557, -1);
                            st.takeItems(1558, -1);
                            st.giveItems(1510, 1);
                            st.giveItems(1060, 100);
                            if (player.isNewbie()) {
                                st.showQuestionMark(26);
                                if (player.isMageClass()) {
                                    st.playTutorialVoice("tutorial_voice_027");
                                    st.giveItems(5790, 3000);
                                } else {
                                    st.playTutorialVoice("tutorial_voice_026");
                                    st.giveItems(5789, 6000);
                                }
                            }
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
                    case 30580:
                        htmltext = "30580-01.htm";
                        if (cond == 1) {
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        if (cond == 2) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1557, 1);
        } else if (cond == 4) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1556, 1);
        } else if (cond == 6) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1558, 1);
        }
        return null;
    }
}
