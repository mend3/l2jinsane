package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.manager.FourSepulchersManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q620_FourGoblets extends Quest {
    private static final String qn = "Q620_FourGoblets";

    private static final int GHOST_OF_WIGOTH_1 = 31452;

    private static final int NAMELESS_SPIRIT = 31453;

    private static final int GHOST_OF_WIGOTH_2 = 31454;

    private static final int GHOST_CHAMBERLAIN_1 = 31919;

    private static final int GHOST_CHAMBERLAIN_2 = 31920;

    private static final int CONQUERORS_SEPULCHER_MANAGER = 31921;

    private static final int EMPERORS_SEPULCHER_MANAGER = 31922;

    private static final int GREAT_SAGES_SEPULCHER_MANAGER = 31923;

    private static final int JUDGES_SEPULCHER_MANAGER = 31924;

    private static final int RELIC = 7254;

    private static final int SEALED_BOX = 7255;

    private static final int GOBLET_OF_ALECTIA = 7256;

    private static final int GOBLET_OF_TISHAS = 7257;

    private static final int GOBLET_OF_MEKARA = 7258;

    private static final int GOBLET_OF_MORIGUL = 7259;

    private static final int USED_GRAVE_PASS = 7261;

    private static final int ANTIQUE_BROOCH = 7262;

    private static final int[] RCP_REWARDS = new int[]{6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

    public Q620_FourGoblets() {
        super(620, "Four Goblets");
        setItemsIds(7255, 7261, 7256, 7257, 7258, 7259);
        addStartNpc(31453, 31921, 31922, 31923, 31924, 31919, 31920);
        addTalkId(31453, 31921, 31922, 31923, 31924, 31919, 31920, 31452, 31454);
        for (int id = 18120; id <= 18256; id++) {
            addKillId(id);
        }
    }

    private static boolean calculateBoxReward(QuestState st) {
        boolean reward = false;
        int rnd = Rnd.get(5);
        if (rnd == 0) {
            st.giveItems(57, 10000);
            reward = true;
        } else if (rnd == 1) {
            if (Rnd.get(1000) < 848) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 43) {
                    st.giveItems(1884, 42);
                } else if (i < 66) {
                    st.giveItems(1895, 36);
                } else if (i < 184) {
                    st.giveItems(1876, 4);
                } else if (i < 250) {
                    st.giveItems(1881, 6);
                } else if (i < 287) {
                    st.giveItems(5549, 8);
                } else if (i < 484) {
                    st.giveItems(1874, 1);
                } else if (i < 681) {
                    st.giveItems(1889, 1);
                } else if (i < 799) {
                    st.giveItems(1877, 1);
                } else if (i < 902) {
                    st.giveItems(1894, 1);
                } else {
                    st.giveItems(4043, 1);
                }
            }
            if (Rnd.get(1000) < 323) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 335) {
                    st.giveItems(1888, 1);
                } else if (i < 556) {
                    st.giveItems(4040, 1);
                } else if (i < 725) {
                    st.giveItems(1890, 1);
                } else if (i < 872) {
                    st.giveItems(5550, 1);
                } else if (i < 962) {
                    st.giveItems(1893, 1);
                } else if (i < 986) {
                    st.giveItems(4046, 1);
                } else {
                    st.giveItems(4048, 1);
                }
            }
        } else if (rnd == 2) {
            if (Rnd.get(1000) < 847) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 148) {
                    st.giveItems(1878, 8);
                } else if (i < 175) {
                    st.giveItems(1882, 24);
                } else if (i < 273) {
                    st.giveItems(1879, 4);
                } else if (i < 322) {
                    st.giveItems(1880, 6);
                } else if (i < 357) {
                    st.giveItems(1885, 6);
                } else if (i < 554) {
                    st.giveItems(1875, 1);
                } else if (i < 685) {
                    st.giveItems(1883, 1);
                } else if (i < 803) {
                    st.giveItems(5220, 1);
                } else if (i < 901) {
                    st.giveItems(4039, 1);
                } else {
                    st.giveItems(4044, 1);
                }
            }
            if (Rnd.get(1000) < 251) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 350) {
                    st.giveItems(1887, 1);
                } else if (i < 587) {
                    st.giveItems(4042, 1);
                } else if (i < 798) {
                    st.giveItems(1886, 1);
                } else if (i < 922) {
                    st.giveItems(4041, 1);
                } else if (i < 966) {
                    st.giveItems(1892, 1);
                } else if (i < 996) {
                    st.giveItems(1891, 1);
                } else {
                    st.giveItems(4047, 1);
                }
            }
        } else if (rnd == 3) {
            if (Rnd.get(1000) < 31) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 223) {
                    st.giveItems(730, 1);
                } else if (i < 893) {
                    st.giveItems(948, 1);
                } else {
                    st.giveItems(960, 1);
                }
            }
            if (Rnd.get(1000) < 5) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 202) {
                    st.giveItems(729, 1);
                } else if (i < 928) {
                    st.giveItems(947, 1);
                } else {
                    st.giveItems(959, 1);
                }
            }
        } else if (rnd == 4) {
            if (Rnd.get(1000) < 329) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 88) {
                    st.giveItems(6698, 1);
                } else if (i < 185) {
                    st.giveItems(6699, 1);
                } else if (i < 238) {
                    st.giveItems(6700, 1);
                } else if (i < 262) {
                    st.giveItems(6701, 1);
                } else if (i < 292) {
                    st.giveItems(6702, 1);
                } else if (i < 356) {
                    st.giveItems(6703, 1);
                } else if (i < 420) {
                    st.giveItems(6704, 1);
                } else if (i < 482) {
                    st.giveItems(6705, 1);
                } else if (i < 554) {
                    st.giveItems(6706, 1);
                } else if (i < 576) {
                    st.giveItems(6707, 1);
                } else if (i < 640) {
                    st.giveItems(6708, 1);
                } else if (i < 704) {
                    st.giveItems(6709, 1);
                } else if (i < 777) {
                    st.giveItems(6710, 1);
                } else if (i < 799) {
                    st.giveItems(6711, 1);
                } else if (i < 863) {
                    st.giveItems(6712, 1);
                } else if (i < 927) {
                    st.giveItems(6713, 1);
                } else {
                    st.giveItems(6714, 1);
                }
            }
            if (Rnd.get(1000) < 54) {
                reward = true;
                int i = Rnd.get(1000);
                if (i < 100) {
                    st.giveItems(6688, 1);
                } else if (i < 198) {
                    st.giveItems(6689, 1);
                } else if (i < 298) {
                    st.giveItems(6690, 1);
                } else if (i < 398) {
                    st.giveItems(6691, 1);
                } else if (i < 499) {
                    st.giveItems(7579, 1);
                } else if (i < 601) {
                    st.giveItems(6693, 1);
                } else if (i < 703) {
                    st.giveItems(6694, 1);
                } else if (i < 801) {
                    st.giveItems(6695, 1);
                } else if (i < 902) {
                    st.giveItems(6696, 1);
                } else {
                    st.giveItems(6697, 1);
                }
            }
        }
        return reward;
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q620_FourGoblets");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31452-05.htm")) {
            if (Rnd.nextBoolean())
                htmltext = Rnd.nextBoolean() ? "31452-03.htm" : "31452-04.htm";
        } else if (event.equalsIgnoreCase("31452-06.htm")) {
            player.teleportTo(169590, -90218, -2914, 0);
        } else if (event.equalsIgnoreCase("31453-13.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31453-16.htm")) {
            if (st.hasQuestItems(7256, 7257, 7258, 7259)) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7256, -1);
                st.takeItems(7257, -1);
                st.takeItems(7258, -1);
                st.takeItems(7259, -1);
                st.giveItems(7262, 1);
            } else {
                htmltext = "31453-14.htm";
            }
        } else if (event.equalsIgnoreCase("31453-13.htm")) {
            if (st.getInt("cond") == 2)
                htmltext = "31453-19.htm";
        } else if (event.equalsIgnoreCase("31453-18.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("boxes")) {
            if (st.hasQuestItems(7255)) {
                st.takeItems(7255, 1);
                if (!calculateBoxReward(st)) {
                    htmltext = Rnd.nextBoolean() ? "31454-09.htm" : "31454-10.htm";
                } else {
                    htmltext = "31454-08.htm";
                }
            }
        } else if (event.equalsIgnoreCase("tele_4sep")) {
            if (st.hasQuestItems(7262)) {
                player.teleportTo(178298, -84574, -7216, 0);
                return null;
            }
            if (st.hasQuestItems(7261)) {
                st.takeItems(7261, 1);
                player.teleportTo(178298, -84574, -7216, 0);
                return null;
            }
            htmltext = npc.getNpcId() + "-00.htm";
        } else if (event.equalsIgnoreCase("tele_it")) {
            if (st.hasQuestItems(7262)) {
                player.teleportTo(186942, -75602, -2834, 0);
                return null;
            }
            if (st.hasQuestItems(7261)) {
                st.takeItems(7261, 1);
                player.teleportTo(186942, -75602, -2834, 0);
                return null;
            }
            htmltext = npc.getNpcId() + "-00.htm";
        } else if (event.equalsIgnoreCase("31919-06.htm")) {
            if (st.hasQuestItems(7255)) {
                st.takeItems(7255, 1);
                if (!calculateBoxReward(st)) {
                    htmltext = Rnd.nextBoolean() ? "31919-04.htm" : "31919-05.htm";
                } else {
                    htmltext = "31919-03.htm";
                }
            }
        } else if (StringUtil.isDigit(event)) {
            int id = Integer.parseInt(event);
            if (ArraysUtil.contains(RCP_REWARDS, id) && st.getQuestItemsCount(7254) >= 1000) {
                st.takeItems(7254, 1000);
                st.giveItems(id, 1);
            }
            htmltext = "31454-12.htm";
        } else if (event.equalsIgnoreCase("Enter")) {
            FourSepulchersManager.getInstance().tryEntry(npc, player);
            return null;
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q620_FourGoblets");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        int npcId = npc.getNpcId();
        int id = st.getState();
        int cond = st.getInt("cond");
        if (id == 0)
            st.set("cond", "0");
        if (npcId == 31452) {
            if (cond == 1) {
                htmltext = "31452-01.htm";
            } else if (cond == 2) {
                htmltext = "31452-02.htm";
            }
        } else if (npcId == 31453) {
            if (cond == 0) {
                htmltext = (player.getLevel() >= 74) ? "31453-01.htm" : "31453-12.htm";
            } else if (cond == 1) {
                htmltext = st.hasQuestItems(7256, 7257, 7258, 7259) ? "31453-15.htm" : "31453-14.htm";
            } else if (cond == 2) {
                htmltext = "31453-17.htm";
            }
        } else if (npcId == 31454) {
            int index = 0;
            if (st.hasQuestItems(7256, 7257, 7258, 7259))
                index = 4;
            boolean gotSealBoxes = st.hasQuestItems(7255);
            boolean gotEnoughRelics = (st.getQuestItemsCount(7254) >= 1000);
            if (gotSealBoxes && gotEnoughRelics) {
                index += 3;
            } else if (!gotSealBoxes && gotEnoughRelics) {
                index += 2;
            } else if (gotSealBoxes) {
                index++;
            }
            htmltext = "31454-0" + index + ".htm";
        } else {
            htmltext = npcId + "-01.htm";
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(7255, 1, 0, 300000);
        return null;
    }
}
