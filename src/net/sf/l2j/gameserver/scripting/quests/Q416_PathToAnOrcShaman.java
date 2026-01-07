package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q416_PathToAnOrcShaman extends Quest {
    private static final String qn = "Q416_PathToAnOrcShaman";

    private static final int FIRE_CHARM = 1616;

    private static final int KASHA_BEAR_PELT = 1617;

    private static final int KASHA_BLADE_SPIDER_HUSK = 1618;

    private static final int FIERY_EGG_1 = 1619;

    private static final int HESTUI_MASK = 1620;

    private static final int FIERY_EGG_2 = 1621;

    private static final int TOTEM_SPIRIT_CLAW = 1622;

    private static final int TATARU_LETTER = 1623;

    private static final int FLAME_CHARM = 1624;

    private static final int GRIZZLY_BLOOD = 1625;

    private static final int BLOOD_CAULDRON = 1626;

    private static final int SPIRIT_NET = 1627;

    private static final int BOUND_DURKA_SPIRIT = 1628;

    private static final int DURKA_PARASITE = 1629;

    private static final int TOTEM_SPIRIT_BLOOD = 1630;

    private static final int MASK_OF_MEDIUM = 1631;

    private static final int TATARU_ZU_HESTUI = 30585;

    private static final int UMOS = 30502;

    private static final int HESTUI_TOTEM_SPIRIT = 30592;

    private static final int DUDA_MARA_TOTEM_SPIRIT = 30593;

    private static final int MOIRA = 31979;

    private static final int TOTEM_SPIRIT_OF_GANDI = 32057;

    private static final int DEAD_LEOPARD_CARCASS = 32090;

    private static final int VENOMOUS_SPIDER = 20038;

    private static final int ARACHNID_TRACKER = 20043;

    private static final int GRIZZLY_BEAR = 20335;

    private static final int SCARLET_SALAMANDER = 20415;

    private static final int KASHA_BLADE_SPIDER = 20478;

    private static final int KASHA_BEAR = 20479;

    private static final int DURKA_SPIRIT = 27056;

    private static final int BLACK_LEOPARD = 27319;

    public Q416_PathToAnOrcShaman() {
        super(416, "Path To An Orc Shaman");
        setItemsIds(1616, 1617, 1618, 1619, 1620, 1621, 1622, 1623, 1624, 1625,
                1626, 1627, 1628, 1629, 1630);
        addStartNpc(30585);
        addTalkId(30585, 30502, 30592, 30593, 31979, 32057, 32090);
        addKillId(20038, 20043, 20335, 20415, 20478, 20479, 27056, 27319);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q416_PathToAnOrcShaman");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30585-05.htm")) {
            if (player.getClassId() != ClassId.ORC_MYSTIC) {
                htmltext = (player.getClassId() == ClassId.ORC_SHAMAN) ? "30585-02a.htm" : "30585-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30585-03.htm";
            } else if (st.hasQuestItems(1631)) {
                htmltext = "30585-04.htm";
            }
        } else if (event.equalsIgnoreCase("30585-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1616, 1);
        } else if (event.equalsIgnoreCase("30585-11b.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1622, 1);
            st.giveItems(1623, 1);
        } else if (event.equalsIgnoreCase("30585-11c.htm")) {
            st.set("cond", "12");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1622, 1);
        } else if (event.equalsIgnoreCase("30592-03.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1620, 1);
            st.takeItems(1621, 1);
            st.giveItems(1622, 1);
        } else if (event.equalsIgnoreCase("30593-03.htm")) {
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1626, 1);
            st.giveItems(1627, 1);
        } else if (event.equalsIgnoreCase("32057-02.htm")) {
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32057-05.htm")) {
            st.set("cond", "21");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("32090-04.htm")) {
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30502-07.htm")) {
            st.takeItems(1630, -1);
            st.giveItems(1631, 1);
            st.rewardExpAndSp(3200L, 2600);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q416_PathToAnOrcShaman");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30585-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30585:
                        if (cond == 1) {
                            htmltext = "30585-07.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30585-08.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1619, 1);
                            st.takeItems(1616, 1);
                            st.takeItems(1617, 1);
                            st.takeItems(1618, 1);
                            st.giveItems(1621, 1);
                            st.giveItems(1620, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30585-09.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30585-10.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30585-12.htm";
                            break;
                        }
                        if (cond > 5 && cond < 12) {
                            htmltext = "30585-13.htm";
                            break;
                        }
                        if (cond == 12)
                            htmltext = "30585-11c.htm";
                        break;
                    case 30592:
                        if (cond == 3) {
                            htmltext = "30592-01.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30592-04.htm";
                            break;
                        }
                        if (cond > 4 && cond < 12)
                            htmltext = "30592-05.htm";
                        break;
                    case 30502:
                        if (cond == 5) {
                            htmltext = "30502-01.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1623, 1);
                            st.giveItems(1624, 1);
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30502-02.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30502-03.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1624, 1);
                            st.takeItems(1625, 3);
                            st.giveItems(1626, 1);
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "30502-04.htm";
                            break;
                        }
                        if (cond == 9 || cond == 10) {
                            htmltext = "30502-05.htm";
                            break;
                        }
                        if (cond == 11)
                            htmltext = "30502-06.htm";
                        break;
                    case 31979:
                        if (cond == 12) {
                            htmltext = "31979-01.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond > 12 && cond < 21) {
                            htmltext = "31979-02.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "31979-03.htm";
                            st.giveItems(1631, 1);
                            st.rewardExpAndSp(3200L, 3250);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 32057:
                        if (cond == 13) {
                            htmltext = "32057-01.htm";
                            break;
                        }
                        if (cond > 13 && cond < 20) {
                            htmltext = "32057-03.htm";
                            break;
                        }
                        if (cond == 20)
                            htmltext = "32057-04.htm";
                        break;
                    case 30593:
                        if (cond == 8) {
                            htmltext = "30593-01.htm";
                            break;
                        }
                        if (cond == 9) {
                            htmltext = "30593-04.htm";
                            break;
                        }
                        if (cond == 10) {
                            htmltext = "30593-05.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1628, 1);
                            st.giveItems(1630, 1);
                            break;
                        }
                        if (cond == 11)
                            htmltext = "30593-06.htm";
                        break;
                    case 32090:
                        if (cond == 14) {
                            htmltext = "32090-01a.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "32090-01.htm";
                            st.set("cond", "16");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 16) {
                            htmltext = "32090-01b.htm";
                            break;
                        }
                        if (cond == 17) {
                            htmltext = "32090-02.htm";
                            break;
                        }
                        if (cond == 18) {
                            htmltext = "32090-05.htm";
                            break;
                        }
                        if (cond == 19) {
                            htmltext = "32090-06.htm";
                            st.set("cond", "20");
                            st.playSound("ItemSound.quest_middle");
                        }
                        break;
                }
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
        switch (npc.getNpcId()) {
            case 20479:
                if (cond == 1 && !st.hasQuestItems(1617)) {
                    st.giveItems(1617, 1);
                    if (st.hasQuestItems(1619, 1618)) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20478:
                if (cond == 1 && !st.hasQuestItems(1618)) {
                    st.giveItems(1618, 1);
                    if (st.hasQuestItems(1617, 1619)) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20415:
                if (cond == 1 && !st.hasQuestItems(1619)) {
                    st.giveItems(1619, 1);
                    if (st.hasQuestItems(1617, 1618)) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
                break;
            case 20335:
                if (cond == 6 && st.dropItemsAlways(1625, 1, 3))
                    st.set("cond", "7");
                break;
            case 20038:
            case 20043:
                if (cond == 9) {
                    int count = st.getQuestItemsCount(1629);
                    int rnd = Rnd.get(10);
                    if ((count == 5 && rnd < 1) || ((count == 6 || count == 7) && rnd < 2) || count >= 8) {
                        st.playSound("Itemsound.quest_before_battle");
                        st.takeItems(1629, -1);
                        addSpawn(27056, npc, false, 120000L, true);
                        break;
                    }
                    st.dropItemsAlways(1629, 1, 0);
                }
                break;
            case 27056:
                if (cond == 9) {
                    st.set("cond", "10");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1629, -1);
                    st.takeItems(1627, 1);
                    st.giveItems(1628, 1);
                }
                break;
            case 27319:
                if (cond == 14) {
                    if (st.getInt("leopard") > 0) {
                        st.set("cond", "15");
                        st.playSound("ItemSound.quest_middle");
                        if (Rnd.get(3) < 2)
                            npc.broadcastNpcSay("My dear friend of " + player.getName() + ", who has gone on ahead of me!");
                        break;
                    }
                    st.set("leopard", "1");
                    break;
                }
                if (cond == 16) {
                    st.set("cond", "17");
                    st.playSound("ItemSound.quest_middle");
                    if (Rnd.get(3) < 2)
                        npc.broadcastNpcSay("Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, " + player.getName() + "!");
                    break;
                }
                if (cond == 18) {
                    st.set("cond", "19");
                    st.playSound("ItemSound.quest_middle");
                }
                break;
        }
        return null;
    }
}
