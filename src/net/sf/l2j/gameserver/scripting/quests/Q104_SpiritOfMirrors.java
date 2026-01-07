package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q104_SpiritOfMirrors extends Quest {
    private static final String qn = "Q104_SpiritOfMirrors";

    private static final int GALLINS_OAK_WAND = 748;

    private static final int WAND_SPIRITBOUND_1 = 1135;

    private static final int WAND_SPIRITBOUND_2 = 1136;

    private static final int WAND_SPIRITBOUND_3 = 1137;

    private static final int SPIRITSHOT_NO_GRADE = 2509;

    private static final int SOULSHOT_NO_GRADE = 1835;

    private static final int WAND_OF_ADEPT = 747;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int LESSER_HEALING_POT = 1060;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int GALLINT = 30017;

    private static final int ARNOLD = 30041;

    private static final int JOHNSTONE = 30043;

    private static final int KENYOS = 30045;

    public Q104_SpiritOfMirrors() {
        super(104, "Spirit of Mirrors");
        setItemsIds(748, 1135, 1136, 1137);
        addStartNpc(30017);
        addTalkId(30017, 30041, 30043, 30045);
        addKillId(27003, 27004, 27005);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q104_SpiritOfMirrors");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30017-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(748, 1);
            st.giveItems(748, 1);
            st.giveItems(748, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q104_SpiritOfMirrors");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.HUMAN) {
                    htmltext = "30017-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30017-01.htm";
                    break;
                }
                htmltext = "30017-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30017:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30017-04.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30017-05.htm";
                            st.takeItems(1135, -1);
                            st.takeItems(1136, -1);
                            st.takeItems(1137, -1);
                            st.giveItems(747, 1);
                            st.rewardItems(1060, 100);
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
                                    st.giveItems(5789, 7000);
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
                    case 30041:
                    case 30043:
                    case 30045:
                        htmltext = npc.getNpcId() + "-01.htm";
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
        if (st.getItemEquipped(7) == 748)
            switch (npc.getNpcId()) {
                case 27003:
                    if (!st.hasQuestItems(1135)) {
                        st.takeItems(748, 1);
                        st.giveItems(1135, 1);
                        if (st.hasQuestItems(1136, 1137)) {
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                case 27004:
                    if (!st.hasQuestItems(1136)) {
                        st.takeItems(748, 1);
                        st.giveItems(1136, 1);
                        if (st.hasQuestItems(1135, 1137)) {
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
                case 27005:
                    if (!st.hasQuestItems(1137)) {
                        st.takeItems(748, 1);
                        st.giveItems(1137, 1);
                        if (st.hasQuestItems(1135, 1136)) {
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        st.playSound("ItemSound.quest_itemget");
                    }
                    break;
            }
        return null;
    }
}
