package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q231_TestOfTheMaestro extends Quest {
    private static final String qn = "Q231_TestOfTheMaestro";

    private static final int RECOMMENDATION_OF_BALANKI = 2864;

    private static final int RECOMMENDATION_OF_FILAUR = 2865;

    private static final int RECOMMENDATION_OF_ARIN = 2866;

    private static final int LETTER_OF_SOLDER_DETACHMENT = 2868;

    private static final int PAINT_OF_KAMURU = 2869;

    private static final int NECKLACE_OF_KAMURU = 2870;

    private static final int PAINT_OF_TELEPORT_DEVICE = 2871;

    private static final int TELEPORT_DEVICE = 2872;

    private static final int ARCHITECTURE_OF_KRUMA = 2873;

    private static final int REPORT_OF_KRUMA = 2874;

    private static final int INGREDIENTS_OF_ANTIDOTE = 2875;

    private static final int STINGER_WASP_NEEDLE = 2876;

    private static final int MARSH_SPIDER_WEB = 2877;

    private static final int BLOOD_OF_LEECH = 2878;

    private static final int BROKEN_TELEPORT_DEVICE = 2916;

    private static final int MARK_OF_MAESTRO = 2867;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int LOCKIRIN = 30531;

    private static final int SPIRON = 30532;

    private static final int BALANKI = 30533;

    private static final int KEEF = 30534;

    private static final int FILAUR = 30535;

    private static final int ARIN = 30536;

    private static final int TOMA = 30556;

    private static final int CROTO = 30671;

    private static final int DUBABAH = 30672;

    private static final int LORAIN = 30673;

    private static final int KING_BUGBEAR = 20150;

    private static final int GIANT_MIST_LEECH = 20225;

    private static final int STINGER_WASP = 20229;

    private static final int MARSH_SPIDER = 20233;

    private static final int EVIL_EYE_LORD = 27133;

    public Q231_TestOfTheMaestro() {
        super(231, "Test Of The Maestro");
        setItemsIds(2864, 2865, 2866, 2868, 2869, 2870, 2871, 2872, 2873, 2874,
                2875, 2876, 2877, 2878, 2916);
        addStartNpc(30531);
        addTalkId(30531, 30532, 30533, 30534, 30535, 30536, 30556, 30671, 30672, 30673);
        addKillId(20225, 20229, 20233, 27133);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q231_TestOfTheMaestro");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30531-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            if (!player.getMemos().getBool("secondClassChange39", false)) {
                htmltext = "30531-04a.htm";
                st.giveItems(7562, DF_REWARD_39.get(Integer.valueOf(player.getClassId().getId())));
                player.getMemos().set("secondClassChange39", true);
            }
        } else if (event.equalsIgnoreCase("30533-02.htm")) {
            st.set("bCond", "1");
        } else if (event.equalsIgnoreCase("30671-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(2869, 1);
        } else if (event.equalsIgnoreCase("30556-05.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(2871, 1);
            st.giveItems(2916, 1);
            player.teleportTo(140352, -194133, -3146, 0);
            startQuestTimer("spawn_bugbears", 5000L, null, player, false);
        } else if (event.equalsIgnoreCase("30673-04.htm")) {
            st.set("fCond", "2");
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(2878, -1);
            st.takeItems(2875, 1);
            st.takeItems(2877, -1);
            st.takeItems(2876, -1);
            st.giveItems(2874, 1);
        } else if (event.equalsIgnoreCase("spawn_bugbears")) {
            Attackable bugbear1 = (Attackable) addSpawn(20150, 140333, -194153, -3138, 0, false, 200000L, true);
            bugbear1.addDamageHate(player, 0, 999);
            bugbear1.getAI().setIntention(IntentionType.ATTACK, player);
            Attackable bugbear2 = (Attackable) addSpawn(20150, 140395, -194147, -3146, 0, false, 200000L, true);
            bugbear2.addDamageHate(player, 0, 999);
            bugbear2.getAI().setIntention(IntentionType.ATTACK, player);
            Attackable bugbear3 = (Attackable) addSpawn(20150, 140304, -194082, -3157, 0, false, 200000L, true);
            bugbear3.addDamageHate(player, 0, 999);
            bugbear3.getAI().setIntention(IntentionType.ATTACK, player);
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, bCond, aCond, fCond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q231_TestOfTheMaestro");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.ARTISAN) {
                    htmltext = "30531-01.htm";
                    break;
                }
                if (player.getLevel() < 39) {
                    htmltext = "30531-02.htm";
                    break;
                }
                htmltext = "30531-03.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30531:
                        cond = st.getInt("cond");
                        if (cond == 1) {
                            htmltext = "30531-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30531-06.htm";
                            st.takeItems(2866, 1);
                            st.takeItems(2864, 1);
                            st.takeItems(2865, 1);
                            st.giveItems(2867, 1);
                            st.rewardExpAndSp(46000L, 5900);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30532:
                        htmltext = "30532-01.htm";
                        break;
                    case 30534:
                        htmltext = "30534-01.htm";
                        break;
                    case 30533:
                        bCond = st.getInt("bCond");
                        if (bCond == 0) {
                            htmltext = "30533-01.htm";
                            break;
                        }
                        if (bCond == 1) {
                            htmltext = "30533-03.htm";
                            break;
                        }
                        if (bCond == 2) {
                            htmltext = "30533-04.htm";
                            st.set("bCond", "3");
                            st.takeItems(2868, 1);
                            st.giveItems(2864, 1);
                            if (st.hasQuestItems(2866, 2865)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (bCond == 3)
                            htmltext = "30533-05.htm";
                        break;
                    case 30671:
                        bCond = st.getInt("bCond");
                        if (bCond == 1) {
                            if (!st.hasQuestItems(2869)) {
                                htmltext = "30671-01.htm";
                                break;
                            }
                            if (!st.hasQuestItems(2870)) {
                                htmltext = "30671-03.htm";
                                break;
                            }
                            htmltext = "30671-04.htm";
                            st.set("bCond", "2");
                            st.playSound("ItemSound.quest_itemget");
                            st.takeItems(2870, 1);
                            st.takeItems(2869, 1);
                            st.giveItems(2868, 1);
                            break;
                        }
                        if (bCond > 1)
                            htmltext = "30671-05.htm";
                        break;
                    case 30672:
                        htmltext = "30672-01.htm";
                        break;
                    case 30536:
                        aCond = st.getInt("aCond");
                        if (aCond == 0) {
                            htmltext = "30536-01.htm";
                            st.set("aCond", "1");
                            st.giveItems(2871, 1);
                            break;
                        }
                        if (aCond == 1) {
                            htmltext = "30536-02.htm";
                            break;
                        }
                        if (aCond == 2) {
                            htmltext = "30536-03.htm";
                            st.set("aCond", "3");
                            st.takeItems(2872, -1);
                            st.giveItems(2866, 1);
                            if (st.hasQuestItems(2864, 2865)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (aCond == 3)
                            htmltext = "30536-04.htm";
                        break;
                    case 30556:
                        aCond = st.getInt("aCond");
                        if (aCond == 1) {
                            if (!st.hasQuestItems(2916)) {
                                htmltext = "30556-01.htm";
                                break;
                            }
                            if (!st.hasQuestItems(2872)) {
                                htmltext = "30556-06.htm";
                                st.set("aCond", "2");
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(2916, 1);
                                st.giveItems(2872, 5);
                            }
                            break;
                        }
                        if (aCond > 1)
                            htmltext = "30556-07.htm";
                        break;
                    case 30535:
                        fCond = st.getInt("fCond");
                        if (fCond == 0) {
                            htmltext = "30535-01.htm";
                            st.set("fCond", "1");
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(2873, 1);
                            break;
                        }
                        if (fCond == 1) {
                            htmltext = "30535-02.htm";
                            break;
                        }
                        if (fCond == 2) {
                            htmltext = "30535-03.htm";
                            st.set("fCond", "3");
                            st.takeItems(2874, 1);
                            st.giveItems(2865, 1);
                            if (st.hasQuestItems(2864, 2866)) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (fCond == 3)
                            htmltext = "30535-04.htm";
                        break;
                    case 30673:
                        fCond = st.getInt("fCond");
                        if (fCond == 1) {
                            if (!st.hasQuestItems(2874)) {
                                if (!st.hasQuestItems(2875)) {
                                    htmltext = "30673-01.htm";
                                    st.playSound("ItemSound.quest_itemget");
                                    st.takeItems(2873, 1);
                                    st.giveItems(2875, 1);
                                    break;
                                }
                                if (st.getQuestItemsCount(2876) < 10 || st.getQuestItemsCount(2877) < 10 || st.getQuestItemsCount(2878) < 10) {
                                    htmltext = "30673-02.htm";
                                    break;
                                }
                                htmltext = "30673-03.htm";
                            }
                            break;
                        }
                        if (fCond > 1)
                            htmltext = "30673-05.htm";
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20225:
                if (st.hasQuestItems(2875))
                    st.dropItemsAlways(2878, 1, 10);
                break;
            case 20229:
                if (st.hasQuestItems(2875))
                    st.dropItemsAlways(2876, 1, 10);
                break;
            case 20233:
                if (st.hasQuestItems(2875))
                    st.dropItemsAlways(2877, 1, 10);
                break;
            case 27133:
                if (st.hasQuestItems(2869))
                    st.dropItemsAlways(2870, 1, 1);
                break;
        }
        return null;
    }
}
