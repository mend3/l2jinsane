package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q246_PossessorOfAPreciousSoul extends Quest {
    private static final String qn = "Q246_PossessorOfAPreciousSoul";

    private static final int CARADINE = 31740;

    private static final int OSSIAN = 31741;

    private static final int LADD = 30721;

    private static final int WATERBINDER = 7591;

    private static final int EVERGREEN = 7592;

    private static final int RAIN_SONG = 7593;

    private static final int RELIC_BOX = 7594;

    private static final int CARADINE_LETTER_1 = 7678;

    private static final int CARADINE_LETTER_2 = 7679;

    private static final int PILGRIM_OF_SPLENDOR = 21541;

    private static final int JUDGE_OF_SPLENDOR = 21544;

    private static final int BARAKIEL = 25325;

    public Q246_PossessorOfAPreciousSoul() {
        super(246, "Possessor of a Precious Soul - 3");
        setItemsIds(7591, 7592, 7593, 7594);
        addStartNpc(31740);
        addTalkId(31740, 31741, 30721);
        addKillId(21541, 21544, 25325);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q246_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31740-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.takeItems(7678, 1);
        } else if (event.equalsIgnoreCase("31741-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31741-05.htm")) {
            if (st.hasQuestItems(7591, 7592)) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7591, 1);
                st.takeItems(7592, 1);
            } else {
                htmltext = null;
            }
        } else if (event.equalsIgnoreCase("31741-08.htm")) {
            if (st.hasQuestItems(7593)) {
                st.set("cond", "6");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7593, 1);
                st.giveItems(7594, 1);
            } else {
                htmltext = null;
            }
        } else if (event.equalsIgnoreCase("30721-02.htm")) {
            if (st.hasQuestItems(7594)) {
                st.takeItems(7594, 1);
                st.giveItems(7679, 1);
                st.rewardExpAndSp(719843L, 0);
                player.broadcastPacket(new SocialAction(player, 3));
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q246_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (st.hasQuestItems(7678))
                    htmltext = (!player.isSubClassActive() || player.getLevel() < 65) ? "31740-02.htm" : "31740-01.htm";
                break;
            case 1:
                if (!player.isSubClassActive())
                    break;
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31740:
                        if (cond == 1)
                            htmltext = "31740-05.htm";
                        break;
                    case 31741:
                        if (cond == 1) {
                            htmltext = "31741-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31741-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            if (st.hasQuestItems(7591, 7592))
                                htmltext = "31741-04.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31741-06.htm";
                            break;
                        }
                        if (cond == 5) {
                            if (st.hasQuestItems(7593))
                                htmltext = "31741-07.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = "31741-09.htm";
                        break;
                    case 30721:
                        if (cond == 6 && st.hasQuestItems(7594))
                            htmltext = "30721-01.htm";
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
        int npcId = npc.getNpcId();
        if (npcId == 25325) {
            for (QuestState st : getPartyMembers(player, npc, "cond", "4")) {
                if (!st.getPlayer().isSubClassActive())
                    continue;
                if (!st.hasQuestItems(7593)) {
                    st.set("cond", "5");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(7593, 1);
                }
            }
        } else {
            if (!player.isSubClassActive())
                return null;
            QuestState st = checkPlayerCondition(player, npc, "cond", "2");
            if (st == null)
                return null;
            if (Rnd.get(10) < 2) {
                int neklaceOrRing = (npcId == 21541) ? 7591 : 7592;
                if (!st.hasQuestItems(neklaceOrRing)) {
                    st.giveItems(neklaceOrRing, 1);
                    if (!st.hasQuestItems((npcId == 21541) ? 7592 : 7591)) {
                        st.playSound("ItemSound.quest_itemget");
                    } else {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                    }
                }
            }
        }
        return null;
    }
}
