package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q242_PossessorOfAPreciousSoul extends Quest {
    private static final String qn = "Q242_PossessorOfAPreciousSoul";

    private static final int VIRGIL = 31742;

    private static final int KASSANDRA = 31743;

    private static final int OGMAR = 31744;

    private static final int MYSTERIOUS_KNIGHT = 31751;

    private static final int ANGEL_CORPSE = 31752;

    private static final int KALIS = 30759;

    private static final int MATILD = 30738;

    private static final int CORNERSTONE = 31748;

    private static final int FALLEN_UNICORN = 31746;

    private static final int PURE_UNICORN = 31747;

    private static final int RESTRAINER_OF_GLORY = 27317;

    private static final int VIRGIL_LETTER = 7677;

    private static final int GOLDEN_HAIR = 7590;

    private static final int SORCERY_INGREDIENT = 7596;

    private static final int ORB_OF_BINDING = 7595;

    private static final int CARADINE_LETTER = 7678;

    private static boolean _unicorn = false;

    public Q242_PossessorOfAPreciousSoul() {
        super(242, "Possessor of a Precious Soul - 2");
        setItemsIds(7590, 7596, 7595);
        addStartNpc(31742);
        addTalkId(31742, 31743, 31744, 31751, 31752, 30759, 30738, 31748, 31746, 31747);
        addKillId(27317);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q242_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31743-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31744-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31751-02.htm")) {
            st.set("cond", "4");
            st.set("angel", "0");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30759-02.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30759-05.htm")) {
            if (st.hasQuestItems(7596)) {
                st.set("orb", "0");
                st.set("cornerstone", "0");
                st.set("cond", "9");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7590, 1);
                st.takeItems(7596, 1);
            } else {
                st.set("cond", "7");
                htmltext = "30759-02.htm";
            }
        } else if (event.equalsIgnoreCase("30738-02.htm")) {
            st.set("cond", "8");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(7596, 1);
        } else if (event.equalsIgnoreCase("31748-03.htm")) {
            if (st.hasQuestItems(7595)) {
                npc.deleteMe();
                st.takeItems(7595, 1);
                int cornerstones = st.getInt("cornerstone");
                cornerstones++;
                if (cornerstones == 4) {
                    st.unset("orb");
                    st.unset("cornerstone");
                    st.set("cond", "10");
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.set("cornerstone", Integer.toString(cornerstones));
                }
            } else {
                htmltext = null;
            }
        } else {
            if (event.equalsIgnoreCase("spu")) {
                addSpawn(31747, 85884, -76588, -3470, 0, false, 0L, true);
                return null;
            }
            if (event.equalsIgnoreCase("dspu")) {
                npc.getSpawn().setRespawnState(false);
                npc.deleteMe();
                startQuestTimer("sfu", 2000L, null, player, false);
                return null;
            }
            if (event.equalsIgnoreCase("sfu")) {
                npc = addSpawn(31746, 85884, -76588, -3470, 0, false, 0L, true);
                npc.getSpawn().setRespawnState(true);
                return null;
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q242_PossessorOfAPreciousSoul");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (st.hasQuestItems(7677)) {
                    if (!player.isSubClassActive() || player.getLevel() < 60) {
                        htmltext = "31742-02.htm";
                        break;
                    }
                    htmltext = "31742-03.htm";
                    st.setState((byte) 1);
                    st.set("cond", "1");
                    st.playSound("ItemSound.quest_accept");
                    st.takeItems(7677, 1);
                }
                break;
            case 1:
                if (!player.isSubClassActive())
                    break;
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31742:
                        if (cond == 1) {
                            htmltext = "31742-04.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31742-05.htm";
                        break;
                    case 31743:
                        if (cond == 1) {
                            htmltext = "31743-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31743-06.htm";
                            break;
                        }
                        if (cond == 11) {
                            htmltext = "31743-07.htm";
                            st.giveItems(7678, 1);
                            st.rewardExpAndSp(455764L, 0);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 31744:
                        if (cond == 2) {
                            htmltext = "31744-01.htm";
                            break;
                        }
                        if (cond == 3)
                            htmltext = "31744-03.htm";
                        break;
                    case 31751:
                        if (cond == 3) {
                            htmltext = "31751-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "31751-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            if (st.hasQuestItems(7590)) {
                                htmltext = "31751-04.htm";
                                st.set("cond", "6");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "31751-03.htm";
                            st.set("cond", "4");
                            break;
                        }
                        if (cond == 6)
                            htmltext = "31751-05.htm";
                        break;
                    case 31752:
                        if (cond == 4) {
                            npc.deleteMe();
                            int hair = st.getInt("angel");
                            hair++;
                            if (hair == 4) {
                                htmltext = "31752-02.htm";
                                st.unset("angel");
                                st.set("cond", "5");
                                st.playSound("ItemSound.quest_middle");
                                st.giveItems(7590, 1);
                                break;
                            }
                            st.set("angel", Integer.toString(hair));
                            htmltext = "31752-01.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "31752-01.htm";
                        break;
                    case 30759:
                        if (cond == 6) {
                            htmltext = "30759-01.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30759-03.htm";
                            break;
                        }
                        if (cond == 8) {
                            if (st.hasQuestItems(7596)) {
                                htmltext = "30759-04.htm";
                                break;
                            }
                            htmltext = "30759-03.htm";
                            st.set("cond", "7");
                            break;
                        }
                        if (cond == 9)
                            htmltext = "30759-06.htm";
                        break;
                    case 30738:
                        if (cond == 7) {
                            htmltext = "30738-01.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30738-03.htm";
                        break;
                    case 31748:
                        if (cond == 9) {
                            if (st.hasQuestItems(7595)) {
                                htmltext = "31748-02.htm";
                                break;
                            }
                            htmltext = "31748-01.htm";
                        }
                        break;
                    case 31746:
                        if (cond == 9) {
                            htmltext = "31746-01.htm";
                            break;
                        }
                        if (cond == 10) {
                            if (!_unicorn) {
                                _unicorn = true;
                                npc.getSpawn().setRespawnState(false);
                                npc.deleteMe();
                                startQuestTimer("spu", 3000L, npc, player, false);
                            }
                            htmltext = "31746-02.htm";
                        }
                        break;
                    case 31747:
                        if (cond == 10) {
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            if (_unicorn) {
                                _unicorn = false;
                                startQuestTimer("dspu", 3000L, npc, player, false);
                            }
                            htmltext = "31747-01.htm";
                            break;
                        }
                        if (cond == 11)
                            htmltext = "31747-02.htm";
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "9");
        if (st == null || !player.isSubClassActive())
            return null;
        int orbs = st.getInt("orb");
        if (orbs < 4) {
            orbs++;
            st.set("orb", Integer.toString(orbs));
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(7595, 1);
        }
        return null;
    }
}
