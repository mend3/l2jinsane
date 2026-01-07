package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q106_ForgottenTruth extends Quest {
    private static final String qn = "Q106_ForgottenTruth";

    private static final int THIFIELL = 30358;

    private static final int KARTIA = 30133;

    private static final int ONYX_TALISMAN_1 = 984;

    private static final int ONYX_TALISMAN_2 = 985;

    private static final int ANCIENT_SCROLL = 986;

    private static final int ANCIENT_CLAY_TABLET = 987;

    private static final int KARTIA_TRANSLATION = 988;

    private static final int SPIRITSHOT_NO_GRADE = 2509;

    private static final int SOULSHOT_NO_GRADE = 1835;

    private static final int ELDRITCH_DAGGER = 989;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int LESSER_HEALING_POTION = 1060;

    public Q106_ForgottenTruth() {
        super(106, "Forgotten Truth");
        setItemsIds(984, 985, 986, 987, 988);
        addStartNpc(30358);
        addTalkId(30358, 30133);
        addKillId(27070);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState("Q106_ForgottenTruth");
        String htmltext = event;
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30358-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(984, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q106_ForgottenTruth");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30358-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30358-02.htm";
                    break;
                }
                htmltext = "30358-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30358:
                        if (cond == 1) {
                            htmltext = "30358-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30358-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30358-06.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30358-07.htm";
                            st.takeItems(988, 1);
                            st.giveItems(989, 1);
                            st.giveItems(1060, 100);
                            if (player.isMageClass()) {
                                st.giveItems(2509, 500);
                            } else {
                                st.giveItems(1835, 1000);
                            }
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
                    case 30133:
                        if (cond == 1) {
                            htmltext = "30133-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(984, 1);
                            st.giveItems(985, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30133-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30133-03.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(985, 1);
                            st.takeItems(986, 1);
                            st.takeItems(987, 1);
                            st.giveItems(988, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30133-04.htm";
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
        if (!st.hasQuestItems(986)) {
            st.dropItems(986, 1, 1, 200000);
        } else if (st.dropItems(987, 1, 1, 200000)) {
            st.set("cond", "3");
        }
        return null;
    }
}
