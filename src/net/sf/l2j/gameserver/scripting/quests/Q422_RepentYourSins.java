package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q422_RepentYourSins extends Quest {
    private static final String qn = "Q422_RepentYourSins";

    private static final int RATMAN_SCAVENGER_SKULL = 4326;

    private static final int TUREK_WAR_HOUND_TAIL = 4327;

    private static final int TYRANT_KINGPIN_HEART = 4328;

    private static final int TRISALIM_TARANTULA_VENOM_SAC = 4329;

    private static final int QITEM_PENITENT_MANACLES = 4330;

    private static final int MANUAL_OF_MANACLES = 4331;

    private static final int PENITENT_MANACLES = 4425;

    private static final int LEFT_PENITENT_MANACLES = 4426;

    private static final int SILVER_NUGGET = 1873;

    private static final int ADAMANTINE_NUGGET = 1877;

    private static final int BLACKSMITH_FRAME = 1892;

    private static final int COKES = 1879;

    private static final int STEEL = 1880;

    private static final int BLACK_JUDGE = 30981;

    private static final int KATARI = 30668;

    private static final int PIOTUR = 30597;

    private static final int CASIAN = 30612;

    private static final int JOAN = 30718;

    private static final int PUSHKIN = 30300;

    public Q422_RepentYourSins() {
        super(422, "Repent Your Sins");
        setItemsIds(4326, 4327, 4328, 4329, 4331, 4425, 4330);
        addStartNpc(30981);
        addTalkId(30981, 30668, 30597, 30612, 30718, 30300);
        addKillId(20039, 20494, 20193, 20561);
    }

    private static int findSinEaterLvl(Player player) {
        return player.getInventory().getItemByItemId(4425).getEnchantLevel();
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q422_RepentYourSins");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("Start")) {
            st.set("cond", "1");
            if (player.getLevel() <= 20) {
                htmltext = "30981-03.htm";
                st.set("cond", "2");
            } else if (player.getLevel() >= 20 && player.getLevel() <= 30) {
                htmltext = "30981-04.htm";
                st.set("cond", "3");
            } else if (player.getLevel() >= 30 && player.getLevel() <= 40) {
                htmltext = "30981-05.htm";
                st.set("cond", "4");
            } else {
                htmltext = "30981-06.htm";
                st.set("cond", "5");
            }
            st.setState((byte) 1);
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30981-11.htm")) {
            if (!st.hasQuestItems(4425)) {
                int cond = st.getInt("cond");
                if (cond == 15) {
                    st.set("cond", "16");
                    st.set("level", String.valueOf(player.getLevel()));
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(4330, -1);
                    st.giveItems(4425, 1);
                } else if (cond == 16) {
                    st.set("level", String.valueOf(player.getLevel()));
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(4426, -1);
                    st.giveItems(4425, 1);
                }
            }
        } else if (event.equalsIgnoreCase("30981-19.htm")) {
            if (st.hasQuestItems(4426)) {
                st.setState((byte) 1);
                st.set("cond", "16");
                st.playSound("ItemSound.quest_accept");
            }
        } else if (event.equalsIgnoreCase("Pk")) {
            Summon summon = player.getSummon();
            if (summon != null && summon.getNpcId() == 12564) {
                htmltext = "30981-16.htm";
            } else if (findSinEaterLvl(player) > st.getInt("level")) {
                st.takeItems(4425, 1);
                st.giveItems(4426, 1);
                int removePkAmount = Rnd.get(10) + 1;
                if (player.getPkKills() <= removePkAmount) {
                    htmltext = "30981-15.htm";
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(true);
                    player.setPkKills(0);
                    player.sendPacket(new UserInfo(player));
                } else {
                    htmltext = "30981-14.htm";
                    st.set("level", String.valueOf(player.getLevel()));
                    st.playSound("ItemSound.quest_middle");
                    player.setPkKills(player.getPkKills() - removePkAmount);
                    player.sendPacket(new UserInfo(player));
                }
            }
        } else if (event.equalsIgnoreCase("Quit")) {
            htmltext = "30981-20.htm";
            st.takeItems(4326, -1);
            st.takeItems(4327, -1);
            st.takeItems(4328, -1);
            st.takeItems(4329, -1);
            st.takeItems(4331, -1);
            st.takeItems(4425, -1);
            st.takeItems(4330, -1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getAlreadyCompletedMsg();
        QuestState st = player.getQuestState("Q422_RepentYourSins");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getPkKills() >= 1) {
                    htmltext = st.hasQuestItems(4426) ? "30981-18.htm" : "30981-02.htm";
                    break;
                }
                htmltext = "30981-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30981:
                        if (cond <= 9) {
                            htmltext = "30981-07.htm";
                            break;
                        }
                        if (cond > 9 && cond < 14) {
                            htmltext = "30981-08.htm";
                            st.set("cond", "14");
                            st.playSound("ItemSound.quest_middle");
                            st.giveItems(4331, 1);
                            break;
                        }
                        if (cond == 14) {
                            htmltext = "30981-09.htm";
                            break;
                        }
                        if (cond == 15) {
                            htmltext = "30981-10.htm";
                            break;
                        }
                        if (cond == 16) {
                            if (st.hasQuestItems(4425)) {
                                htmltext = (findSinEaterLvl(player) > st.getInt("level")) ? "30981-13.htm" : "30981-12.htm";
                                break;
                            }
                            htmltext = "30981-18.htm";
                        }
                        break;
                    case 30668:
                        if (cond == 2) {
                            htmltext = "30668-01.htm";
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 6) {
                            if (st.getQuestItemsCount(4326) < 10) {
                                htmltext = "30668-02.htm";
                                break;
                            }
                            htmltext = "30668-03.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4326, -1);
                            break;
                        }
                        if (cond == 10)
                            htmltext = "30668-04.htm";
                        break;
                    case 30597:
                        if (cond == 3) {
                            htmltext = "30597-01.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 7) {
                            if (st.getQuestItemsCount(4327) < 10) {
                                htmltext = "30597-02.htm";
                                break;
                            }
                            htmltext = "30597-03.htm";
                            st.set("cond", "11");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4327, -1);
                            break;
                        }
                        if (cond == 11)
                            htmltext = "30597-04.htm";
                        break;
                    case 30612:
                        if (cond == 4) {
                            htmltext = "30612-01.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 8) {
                            if (!st.hasQuestItems(4328)) {
                                htmltext = "30612-02.htm";
                                break;
                            }
                            htmltext = "30612-03.htm";
                            st.set("cond", "12");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4328, -1);
                            break;
                        }
                        if (cond == 12)
                            htmltext = "30612-04.htm";
                        break;
                    case 30718:
                        if (cond == 5) {
                            htmltext = "30718-01.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 9) {
                            if (st.getQuestItemsCount(4329) < 3) {
                                htmltext = "30718-02.htm";
                                break;
                            }
                            htmltext = "30718-03.htm";
                            st.set("cond", "13");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4329, -1);
                            break;
                        }
                        if (cond == 13)
                            htmltext = "30718-04.htm";
                        break;
                    case 30300:
                        if (cond == 14 && st.getQuestItemsCount(4331) == 1) {
                            if (st.getQuestItemsCount(1873) < 10 || st.getQuestItemsCount(1880) < 5 || st.getQuestItemsCount(1877) < 2 || st.getQuestItemsCount(1879) < 10 || st.getQuestItemsCount(1892) < 1) {
                                htmltext = "30300-02.htm";
                                break;
                            }
                            htmltext = "30300-01.htm";
                            st.set("cond", "15");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4331, 1);
                            st.takeItems(1873, 10);
                            st.takeItems(1877, 2);
                            st.takeItems(1879, 10);
                            st.takeItems(1880, 5);
                            st.takeItems(1892, 1);
                            st.giveItems(4330, 1);
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(4330, 4425, 4426))
                            htmltext = "30300-03.htm";
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
        switch (npc.getNpcId()) {
            case 20039:
                if (st.getInt("cond") == 6)
                    st.dropItemsAlways(4326, 1, 10);
                break;
            case 20494:
                if (st.getInt("cond") == 7)
                    st.dropItemsAlways(4327, 1, 10);
                break;
            case 20193:
                if (st.getInt("cond") == 8)
                    st.dropItemsAlways(4328, 1, 1);
                break;
            case 20561:
                if (st.getInt("cond") == 9)
                    st.dropItemsAlways(4329, 1, 3);
                break;
        }
        return null;
    }
}
