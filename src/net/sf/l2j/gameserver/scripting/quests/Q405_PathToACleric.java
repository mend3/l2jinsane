package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q405_PathToACleric extends Quest {
    private static final String qn = "Q405_PathToACleric";

    private static final int LETTER_OF_ORDER_1 = 1191;

    private static final int LETTER_OF_ORDER_2 = 1192;

    private static final int LIONEL_BOOK = 1193;

    private static final int BOOK_OF_VIVYAN = 1194;

    private static final int BOOK_OF_SIMPLON = 1195;

    private static final int BOOK_OF_PRAGA = 1196;

    private static final int CERTIFICATE_OF_GALLINT = 1197;

    private static final int PENDANT_OF_MOTHER = 1198;

    private static final int NECKLACE_OF_MOTHER = 1199;

    private static final int LIONEL_COVENANT = 1200;

    private static final int GALLINT = 30017;

    private static final int ZIGAUNT = 30022;

    private static final int VIVYAN = 30030;

    private static final int PRAGA = 30333;

    private static final int SIMPLON = 30253;

    private static final int LIONEL = 30408;

    private static final int MARK_OF_FATE = 1201;

    public Q405_PathToACleric() {
        super(405, "Path to a Cleric");
        setItemsIds(1191, 1195, 1196, 1194, 1199, 1198, 1192, 1193, 1197, 1200);
        addStartNpc(30022);
        addTalkId(30022, 30253, 30333, 30030, 30408, 30017);
        addKillId(20029, 20026);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q405_PathToACleric");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30022-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1191, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q405_PathToACleric");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.HUMAN_MYSTIC) {
                    htmltext = (player.getClassId() == ClassId.CLERIC) ? "30022-02a.htm" : "30022-02.htm";
                    break;
                }
                if (player.getLevel() < 19) {
                    htmltext = "30022-03.htm";
                    break;
                }
                if (st.hasQuestItems(1201)) {
                    htmltext = "30022-04.htm";
                    break;
                }
                htmltext = "30022-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30022:
                        if (cond == 1) {
                            htmltext = "30022-06.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30022-08.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1196, 1);
                            st.takeItems(1194, 1);
                            st.takeItems(1195, 3);
                            st.takeItems(1191, 1);
                            st.giveItems(1192, 1);
                            break;
                        }
                        if (cond > 2 && cond < 6) {
                            htmltext = "30022-07.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30022-09.htm";
                            st.takeItems(1192, 1);
                            st.takeItems(1200, 1);
                            st.giveItems(1201, 1);
                            st.rewardExpAndSp(3200L, 5610);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30253:
                        if (cond == 1 && !st.hasQuestItems(1195)) {
                            htmltext = "30253-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(1195, 3);
                            break;
                        }
                        if (cond > 1 || st.hasQuestItems(1195))
                            htmltext = "30253-02.htm";
                        break;
                    case 30333:
                        if (cond == 1) {
                            if (!st.hasQuestItems(1196) && !st.hasQuestItems(1199) && st.hasQuestItems(1195)) {
                                htmltext = "30333-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.giveItems(1199, 1);
                                break;
                            }
                            if (!st.hasQuestItems(1198)) {
                                htmltext = "30333-02.htm";
                                break;
                            }
                            if (st.hasQuestItems(1198)) {
                                htmltext = "30333-03.htm";
                                st.takeItems(1199, 1);
                                st.takeItems(1198, 1);
                                st.giveItems(1196, 1);
                                if (st.hasQuestItems(1194)) {
                                    st.set("cond", "2");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                            }
                            break;
                        }
                        if (cond > 1 || st.hasQuestItems(1196))
                            htmltext = "30333-04.htm";
                        break;
                    case 30030:
                        if (cond == 1 && !st.hasQuestItems(1194) && st.hasQuestItems(1195)) {
                            htmltext = "30030-01.htm";
                            st.giveItems(1194, 1);
                            if (st.hasQuestItems(1196)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond > 1 || st.hasQuestItems(1194))
                            htmltext = "30030-02.htm";
                        break;
                    case 30408:
                        if (cond < 3) {
                            htmltext = "30408-02.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30408-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(1193, 1);
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30408-03.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30408-04.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1197, 1);
                            st.giveItems(1200, 1);
                            break;
                        }
                        if (cond == 6)
                            htmltext = "30408-05.htm";
                        break;
                    case 30017:
                        if (cond == 4) {
                            htmltext = "30017-01.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1193, 1);
                            st.giveItems(1197, 1);
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30017-02.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.hasQuestItems(1199) && !st.hasQuestItems(1198)) {
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1198, 1);
        }
        return null;
    }
}
