package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q101_SwordOfSolidarity extends Quest {
    private static final String qn = "Q101_SwordOfSolidarity";

    private static final int ROIEN = 30008;

    private static final int ALTRAN = 30283;

    private static final int BROKEN_SWORD_HANDLE = 739;

    private static final int BROKEN_BLADE_BOTTOM = 740;

    private static final int BROKEN_BLADE_TOP = 741;

    private static final int ROIENS_LETTER = 796;

    private static final int DIR_TO_RUINS = 937;

    private static final int ALTRANS_NOTE = 742;

    private static final int SWORD_OF_SOLIDARITY = 738;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int LESSER_HEALING_POT = 1060;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    public Q101_SwordOfSolidarity() {
        super(101, "Sword of Solidarity");
        setItemsIds(739, 740, 741);
        addStartNpc(30008);
        addTalkId(30008, 30283);
        addKillId(20361, 20362);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q101_SwordOfSolidarity");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30008-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(796, 1);
        } else if (event.equalsIgnoreCase("30283-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(796, 1);
            st.giveItems(937, 1);
        } else if (event.equalsIgnoreCase("30283-06.htm")) {
            st.takeItems(739, 1);
            st.giveItems(738, 1);
            st.giveItems(1060, 100);
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
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q101_SwordOfSolidarity");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.HUMAN) {
                    htmltext = "30008-01a.htm";
                    break;
                }
                if (player.getLevel() < 9) {
                    htmltext = "30008-01.htm";
                    break;
                }
                htmltext = "30008-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30008:
                        if (cond == 1) {
                            htmltext = "30008-04.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30008-03a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30008-06.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30008-05.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(742, 1);
                            st.giveItems(739, 1);
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30008-05a.htm";
                        break;
                    case 30283:
                        if (cond == 1) {
                            htmltext = "30283-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30283-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30283-04.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(937, 1);
                            st.takeItems(741, 1);
                            st.takeItems(740, 1);
                            st.giveItems(742, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30283-04a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30283-05.htm";
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
        if (!st.hasQuestItems(741)) {
            st.dropItems(741, 1, 1, 200000);
        } else if (st.dropItems(740, 1, 1, 200000)) {
            st.set("cond", "3");
        }
        return null;
    }
}
