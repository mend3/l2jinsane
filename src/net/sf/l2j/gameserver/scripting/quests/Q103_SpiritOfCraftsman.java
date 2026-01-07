package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q103_SpiritOfCraftsman extends Quest {
    private static final String qn = "Q103_SpiritOfCraftsman";

    private static final int KARROD_LETTER = 968;

    private static final int CECKTINON_VOUCHER_1 = 969;

    private static final int CECKTINON_VOUCHER_2 = 970;

    private static final int SOUL_CATCHER = 971;

    private static final int PRESERVING_OIL = 972;

    private static final int ZOMBIE_HEAD = 973;

    private static final int STEELBENDER_HEAD = 974;

    private static final int BONE_FRAGMENT = 1107;

    private static final int SPIRITSHOT_NO_GRADE = 2509;

    private static final int SOULSHOT_NO_GRADE = 1835;

    private static final int BLOODSABER = 975;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int LESSER_HEALING_POT = 1060;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    private static final int KARROD = 30307;

    private static final int CECKTINON = 30132;

    private static final int HARNE = 30144;

    public Q103_SpiritOfCraftsman() {
        super(103, "Spirit of Craftsman");
        setItemsIds(968, 969, 970, 1107, 971, 972, 973, 974);
        addStartNpc(30307);
        addTalkId(30307, 30132, 30144);
        addKillId(20015, 20020, 20455, 20517, 20518);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q103_SpiritOfCraftsman");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30307-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(968, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q103_SpiritOfCraftsman");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DARK_ELF) {
                    htmltext = "30307-00.htm";
                    break;
                }
                if (player.getLevel() < 11) {
                    htmltext = "30307-02.htm";
                    break;
                }
                htmltext = "30307-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30307:
                        if (cond < 8) {
                            htmltext = "30307-06.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30307-07.htm";
                            st.takeItems(974, 1);
                            st.giveItems(975, 1);
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
                    case 30132:
                        if (cond == 1) {
                            htmltext = "30132-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(968, 1);
                            st.giveItems(969, 1);
                            break;
                        }
                        if (cond > 1 && cond < 5) {
                            htmltext = "30132-02.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30132-03.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(971, 1);
                            st.giveItems(972, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30132-04.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30132-05.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(973, 1);
                            st.giveItems(974, 1);
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30132-06.htm";
                        break;
                    case 30144:
                        if (cond == 2) {
                            htmltext = "30144-01.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(969, 1);
                            st.giveItems(970, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30144-02.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30144-03.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(970, 1);
                            st.takeItems(1107, 10);
                            st.giveItems(971, 1);
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30144-04.htm";
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
        switch (npc.getNpcId()) {
            case 20455:
            case 20517:
            case 20518:
                if (st.getInt("cond") == 3 && st.dropItems(1107, 1, 10, 300000))
                    st.set("cond", "4");
                break;
            case 20015:
            case 20020:
                if (st.getInt("cond") == 6 && st.dropItems(973, 1, 1, 300000)) {
                    st.set("cond", "7");
                    st.takeItems(972, 1);
                }
                break;
        }
        return null;
    }
}
