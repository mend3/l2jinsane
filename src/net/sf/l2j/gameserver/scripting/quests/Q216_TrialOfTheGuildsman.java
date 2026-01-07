package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q216_TrialOfTheGuildsman extends Quest {
    private static final String qn = "Q216_TrialOfTheGuildsman";

    private static final int RECIPE_JOURNEYMAN_RING = 3024;

    private static final int RECIPE_AMBER_BEAD = 3025;

    private static final int VALKON_RECOMMENDATION = 3120;

    private static final int MANDRAGORA_BERRY = 3121;

    private static final int ALTRAN_INSTRUCTIONS = 3122;

    private static final int ALTRAN_RECOMMENDATION_1 = 3123;

    private static final int ALTRAN_RECOMMENDATION_2 = 3124;

    private static final int NORMAN_INSTRUCTIONS = 3125;

    private static final int NORMAN_RECEIPT = 3126;

    private static final int DUNING_INSTRUCTIONS = 3127;

    private static final int DUNING_KEY = 3128;

    private static final int NORMAN_LIST = 3129;

    private static final int GRAY_BONE_POWDER = 3130;

    private static final int GRANITE_WHETSTONE = 3131;

    private static final int RED_PIGMENT = 3132;

    private static final int BRAIDED_YARN = 3133;

    private static final int JOURNEYMAN_GEM = 3134;

    private static final int PINTER_INSTRUCTIONS = 3135;

    private static final int AMBER_BEAD = 3136;

    private static final int AMBER_LUMP = 3137;

    private static final int JOURNEYMAN_DECO_BEADS = 3138;

    private static final int JOURNEYMAN_RING = 3139;

    private static final int MARK_OF_GUILDSMAN = 3119;

    private static final int DIMENSIONAL_DIAMOND = 7562;

    private static final int VALKON = 30103;

    private static final int NORMAN = 30210;

    private static final int ALTRAN = 30283;

    private static final int PINTER = 30298;

    private static final int DUNING = 30688;

    private static final int ANT = 20079;

    private static final int ANT_CAPTAIN = 20080;

    private static final int GRANITE_GOLEM = 20083;

    private static final int MANDRAGORA_SPROUT = 20154;

    private static final int MANDRAGORA_SAPLING = 20155;

    private static final int MANDRAGORA_BLOSSOM = 20156;

    private static final int SILENOS = 20168;

    private static final int STRAIN = 20200;

    private static final int GHOUL = 20201;

    private static final int DEAD_SEEKER = 20202;

    private static final int BREKA_ORC_SHAMAN = 20269;

    private static final int BREKA_ORC_OVERLORD = 20270;

    private static final int BREKA_ORC_WARRIOR = 20271;

    public Q216_TrialOfTheGuildsman() {
        super(216, "Trial of the Guildsman");
        setItemsIds(3024, 3025, 3120, 3121, 3122, 3123, 3124, 3125, 3126, 3127,
                3128, 3129, 3130, 3131, 3132, 3133, 3134, 3135, 3136, 3137,
                3138, 3139);
        addStartNpc(30103);
        addTalkId(30103, 30210, 30283, 30298, 30688);
        addKillId(20079, 20080, 20083, 20154, 20155, 20156, 20168, 20200, 20201, 20202,
                20269, 20270, 20271);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q216_TrialOfTheGuildsman");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30103-06.htm")) {
            if (st.getQuestItemsCount(57) >= 2000) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(57, 2000);
                st.giveItems(3120, 1);
                if (!player.getMemos().getBool("secondClassChange35", false)) {
                    htmltext = "30103-06d.htm";
                    st.giveItems(7562, DF_REWARD_35.get(Integer.valueOf(player.getClassId().getId())));
                    player.getMemos().set("secondClassChange35", true);
                }
            } else {
                htmltext = "30103-05a.htm";
            }
        } else if (event.equalsIgnoreCase("30103-06c.htm") || event.equalsIgnoreCase("30103-07c.htm")) {
            if (st.getInt("cond") < 3) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
            }
        } else if (event.equalsIgnoreCase("30103-09a.htm") || event.equalsIgnoreCase("30103-09b.htm")) {
            st.takeItems(3122, 1);
            st.takeItems(3139, -1);
            st.giveItems(3119, 1);
            st.rewardExpAndSp(80993L, 12250);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30210-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3123, 1);
            st.giveItems(3125, 1);
            st.giveItems(3126, 1);
        } else if (event.equalsIgnoreCase("30210-10.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3129, 1);
        } else if (event.equalsIgnoreCase("30283-03.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(3121, 1);
            st.takeItems(3120, 1);
            st.giveItems(3122, 1);
            st.giveItems(3123, 1);
            st.giveItems(3124, 1);
            st.giveItems(3024, 1);
        } else if (event.equalsIgnoreCase("30298-04.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3124, 1);
            st.giveItems(3135, 1);
            if (player.getClassId() == ClassId.ARTISAN) {
                htmltext = "30298-05.htm";
                st.giveItems(3025, 1);
            }
        } else if (event.equalsIgnoreCase("30688-02.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(3126, 1);
            st.giveItems(3127, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q216_TrialOfTheGuildsman");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getClassId() != ClassId.SCAVENGER && player.getClassId() != ClassId.ARTISAN) {
                    htmltext = "30103-01.htm";
                    break;
                }
                if (player.getLevel() < 35) {
                    htmltext = "30103-02.htm";
                    break;
                }
                htmltext = "30103-03.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30103:
                        if (cond == 1) {
                            htmltext = "30103-06c.htm";
                            break;
                        }
                        if (cond < 5) {
                            htmltext = "30103-07.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30103-08.htm";
                            break;
                        }
                        if (cond == 6)
                            htmltext = (st.getQuestItemsCount(3139) == 7) ? "30103-09.htm" : "30103-08.htm";
                        break;
                    case 30283:
                        if (cond < 4) {
                            htmltext = "30283-01.htm";
                            if (cond == 1) {
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                            }
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30283-02.htm";
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30283-04.htm";
                        break;
                    case 30210:
                        if (cond == 5) {
                            if (st.hasQuestItems(3123)) {
                                htmltext = "30210-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(3126)) {
                                htmltext = "30210-05.htm";
                                break;
                            }
                            if (st.hasQuestItems(3127)) {
                                htmltext = "30210-06.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(3128) == 30) {
                                htmltext = "30210-07.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3128, -1);
                                break;
                            }
                            if (st.hasQuestItems(3129)) {
                                if (st.getQuestItemsCount(3130) == 70 && st.getQuestItemsCount(3131) == 70 && st.getQuestItemsCount(3132) == 70 && st.getQuestItemsCount(3133) == 70) {
                                    htmltext = "30210-12.htm";
                                    st.takeItems(3125, 1);
                                    st.takeItems(3129, 1);
                                    st.takeItems(3133, -1);
                                    st.takeItems(3131, -1);
                                    st.takeItems(3130, -1);
                                    st.takeItems(3132, -1);
                                    st.giveItems(3134, 7);
                                    if (st.getQuestItemsCount(3138) == 7) {
                                        st.set("cond", "6");
                                        st.playSound("ItemSound.quest_middle");
                                        break;
                                    }
                                    st.playSound("ItemSound.quest_itemget");
                                    break;
                                }
                                htmltext = "30210-11.htm";
                            }
                        }
                        break;
                    case 30688:
                        if (cond == 5) {
                            if (st.hasQuestItems(3126)) {
                                htmltext = "30688-01.htm";
                                break;
                            }
                            if (st.hasQuestItems(3127)) {
                                if (st.getQuestItemsCount(3128) < 30) {
                                    htmltext = "30688-03.htm";
                                    break;
                                }
                                htmltext = "30688-04.htm";
                                st.playSound("ItemSound.quest_itemget");
                                st.takeItems(3127, 1);
                                break;
                            }
                            htmltext = "30688-05.htm";
                        }
                        break;
                    case 30298:
                        if (cond == 5) {
                            if (st.hasQuestItems(3124)) {
                                htmltext = (player.getLevel() < 36) ? "30298-01.htm" : "30298-02.htm";
                                break;
                            }
                            if (st.hasQuestItems(3135)) {
                                if (st.getQuestItemsCount(3136) < 70) {
                                    htmltext = "30298-06.htm";
                                    break;
                                }
                                htmltext = "30298-07.htm";
                                st.takeItems(3136, -1);
                                st.takeItems(3135, 1);
                                st.giveItems(3138, 7);
                                if (st.getQuestItemsCount(3134) == 7) {
                                    st.set("cond", "6");
                                    st.playSound("ItemSound.quest_middle");
                                    break;
                                }
                                st.playSound("ItemSound.quest_itemget");
                            }
                            break;
                        }
                        if (st.hasQuestItems(3138))
                            htmltext = "30298-08.htm";
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
            case 20154:
            case 20155:
            case 20156:
                if (st.getInt("cond") == 3 && st.dropItemsAlways(3121, 1, 1))
                    st.set("cond", "4");
                break;
            case 20269:
            case 20270:
            case 20271:
                if (st.hasQuestItems(3127))
                    st.dropItemsAlways(3128, 1, 30);
                break;
            case 20200:
            case 20201:
                if (st.hasQuestItems(3129))
                    st.dropItemsAlways(3130, 5, 70);
                break;
            case 20083:
                if (st.hasQuestItems(3129))
                    st.dropItemsAlways(3131, 7, 70);
                break;
            case 20202:
                if (st.hasQuestItems(3129))
                    st.dropItemsAlways(3132, 7, 70);
                break;
            case 20168:
                if (st.hasQuestItems(3129))
                    st.dropItemsAlways(3133, 10, 70);
                break;
            case 20079:
            case 20080:
                if (st.hasQuestItems(3135))
                    if (st.dropItemsAlways(3136, (player.getClassId() == ClassId.SCAVENGER && ((Monster) npc).getSpoilerId() == player.getObjectId()) ? 10 : 5, 70) &&
                            player.getClassId() == ClassId.ARTISAN && Rnd.nextBoolean())
                        st.giveItems(3137, 1);
                break;
        }
        return null;
    }
}
