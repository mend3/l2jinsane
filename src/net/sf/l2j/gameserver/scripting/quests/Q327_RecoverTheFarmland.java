package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q327_RecoverTheFarmland extends Quest {
    private static final String qn = "Q327_RecoverTheFarmland";

    private static final int LEIKAN_LETTER = 5012;

    private static final int TUREK_DOGTAG = 1846;

    private static final int TUREK_MEDALLION = 1847;

    private static final int CLAY_URN_FRAGMENT = 1848;

    private static final int BRASS_TRINKET_PIECE = 1849;

    private static final int BRONZE_MIRROR_PIECE = 1850;

    private static final int JADE_NECKLACE_BEAD = 1851;

    private static final int ANCIENT_CLAY_URN = 1852;

    private static final int ANCIENT_BRASS_TIARA = 1853;

    private static final int ANCIENT_BRONZE_MIRROR = 1854;

    private static final int ANCIENT_JADE_NECKLACE = 1855;

    private static final int ADENA = 57;

    private static final int SOULSHOT_D = 1463;

    private static final int SPIRITSHOT_D = 2510;

    private static final int HEALING_POTION = 1061;

    private static final int HASTE_POTION = 734;

    private static final int POTION_OF_ALACRITY = 735;

    private static final int SCROLL_OF_ESCAPE = 736;

    private static final int SCROLL_OF_RESURRECTION = 737;

    private static final int LEIKAN = 30382;

    private static final int PIOTUR = 30597;

    private static final int IRIS = 30034;

    private static final int ASHA = 30313;

    private static final int NESTLE = 30314;

    private static final int TUREK_ORC_WARLORD = 20495;

    private static final int TUREK_ORC_ARCHER = 20496;

    private static final int TUREK_ORC_SKIRMISHER = 20497;

    private static final int TUREK_ORC_SUPPLIER = 20498;

    private static final int TUREK_ORC_FOOTMAN = 20499;

    private static final int TUREK_ORC_SENTINEL = 20500;

    private static final int TUREK_ORC_SHAMAN = 20501;

    private static final int[][] DROPLIST = new int[][]{{20496, 140000, 1846}, {20497, 70000, 1846}, {20498, 120000, 1846}, {20499, 100000, 1846}, {20500, 80000, 1846}, {20501, 90000, 1847}, {20495, 180000, 1847}};

    private static final Map<Integer, Integer> EXP_REWARD = new HashMap<>();

    public Q327_RecoverTheFarmland() {
        super(327, "Recover the Farmland");
        EXP_REWARD.put(Integer.valueOf(1852), Integer.valueOf(2766));
        EXP_REWARD.put(Integer.valueOf(1853), Integer.valueOf(3227));
        EXP_REWARD.put(Integer.valueOf(1854), Integer.valueOf(3227));
        EXP_REWARD.put(Integer.valueOf(1855), Integer.valueOf(3919));
        setItemsIds(5012);
        addStartNpc(30382, 30597);
        addTalkId(30382, 30597, 30034, 30313, 30314);
        addKillId(20495, 20496, 20497, 20498, 20499, 20500, 20501);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q327_RecoverTheFarmland");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30597-03.htm") && st.getInt("cond") < 1) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30597-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30382-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5012, 1);
        } else if (event.equalsIgnoreCase("30313-02.htm")) {
            if (st.getQuestItemsCount(1848) >= 5) {
                st.takeItems(1848, 5);
                if (Rnd.get(6) < 5) {
                    htmltext = "30313-03.htm";
                    st.rewardItems(1852, 1);
                } else {
                    htmltext = "30313-10.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30313-04.htm")) {
            if (st.getQuestItemsCount(1849) >= 5) {
                st.takeItems(1849, 5);
                if (Rnd.get(7) < 6) {
                    htmltext = "30313-05.htm";
                    st.rewardItems(1853, 1);
                } else {
                    htmltext = "30313-10.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30313-06.htm")) {
            if (st.getQuestItemsCount(1850) >= 5) {
                st.takeItems(1850, 5);
                if (Rnd.get(7) < 6) {
                    htmltext = "30313-07.htm";
                    st.rewardItems(1854, 1);
                } else {
                    htmltext = "30313-10.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30313-08.htm")) {
            if (st.getQuestItemsCount(1851) >= 5) {
                st.takeItems(1851, 5);
                if (Rnd.get(8) < 7) {
                    htmltext = "30313-09.htm";
                    st.rewardItems(1855, 1);
                } else {
                    htmltext = "30313-10.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30034-03.htm")) {
            int n = st.getQuestItemsCount(1848);
            if (n == 0) {
                htmltext = "30034-02.htm";
            } else {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1848, n);
                st.rewardExpAndSp((n * 307L), 0);
            }
        } else if (event.equalsIgnoreCase("30034-04.htm")) {
            int n = st.getQuestItemsCount(1849);
            if (n == 0) {
                htmltext = "30034-02.htm";
            } else {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1849, n);
                st.rewardExpAndSp((n * 368L), 0);
            }
        } else if (event.equalsIgnoreCase("30034-05.htm")) {
            int n = st.getQuestItemsCount(1850);
            if (n == 0) {
                htmltext = "30034-02.htm";
            } else {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1850, n);
                st.rewardExpAndSp((n * 368L), 0);
            }
        } else if (event.equalsIgnoreCase("30034-06.htm")) {
            int n = st.getQuestItemsCount(1851);
            if (n == 0) {
                htmltext = "30034-02.htm";
            } else {
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1851, n);
                st.rewardExpAndSp((n * 430L), 0);
            }
        } else if (event.equalsIgnoreCase("30034-07.htm")) {
            boolean isRewarded = false;
            for (int i = 1852; i < 1856; i++) {
                int n = st.getQuestItemsCount(i);
                if (n > 0) {
                    st.takeItems(i, n);
                    st.rewardExpAndSp(((long) n * EXP_REWARD.get(Integer.valueOf(i))), 0);
                    isRewarded = true;
                }
            }
            if (!isRewarded) {
                htmltext = "30034-02.htm";
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30314-03.htm")) {
            if (!st.hasQuestItems(1852)) {
                htmltext = "30314-07.htm";
            } else {
                st.takeItems(1852, 1);
                st.rewardItems(1463, 70 + Rnd.get(41));
            }
        } else if (event.equalsIgnoreCase("30314-04.htm")) {
            if (!st.hasQuestItems(1853)) {
                htmltext = "30314-07.htm";
            } else {
                st.takeItems(1853, 1);
                int rnd = Rnd.get(100);
                if (rnd < 40) {
                    st.rewardItems(1061, 1);
                } else if (rnd < 84) {
                    st.rewardItems(734, 1);
                } else {
                    st.rewardItems(735, 1);
                }
            }
        } else if (event.equalsIgnoreCase("30314-05.htm")) {
            if (!st.hasQuestItems(1854)) {
                htmltext = "30314-07.htm";
            } else {
                st.takeItems(1854, 1);
                st.rewardItems((Rnd.get(100) < 59) ? 736 : 737, 1);
            }
        } else if (event.equalsIgnoreCase("30314-06.htm")) {
            if (!st.hasQuestItems(1855)) {
                htmltext = "30314-07.htm";
            } else {
                st.takeItems(1855, 1);
                st.rewardItems(2510, 50 + Rnd.get(41));
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q327_RecoverTheFarmland");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "" + npc.getNpcId() + npc.getNpcId();
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30597:
                        if (!st.hasQuestItems(5012)) {
                            if (st.hasAtLeastOneQuestItem(1846, 1847)) {
                                htmltext = "30597-05.htm";
                                if (cond < 4) {
                                    st.set("cond", "4");
                                    st.playSound("ItemSound.quest_middle");
                                }
                                int dogtag = st.getQuestItemsCount(1846);
                                int medallion = st.getQuestItemsCount(1847);
                                st.takeItems(1846, -1);
                                st.takeItems(1847, -1);
                                st.rewardItems(57, dogtag * 40 + medallion * 50 + ((dogtag + medallion >= 10) ? 619 : 0));
                                break;
                            }
                            htmltext = "30597-04.htm";
                            break;
                        }
                        htmltext = "30597-03a.htm";
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(5012, 1);
                        break;
                    case 30382:
                        if (cond == 2) {
                            htmltext = "30382-04.htm";
                            break;
                        }
                        if (cond == 3 || cond == 4) {
                            htmltext = "30382-05.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30382-05.htm";
                        break;
                }
                htmltext = npc.getNpcId() + "-01.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        for (int[] npcData : DROPLIST) {
            if (npcData[0] == npc.getNpcId()) {
                st.dropItemsAlways(npcData[2], 1, -1);
                st.dropItems(Rnd.get(1848, 1851), 1, 0, npcData[1]);
                break;
            }
        }
        return null;
    }
}
