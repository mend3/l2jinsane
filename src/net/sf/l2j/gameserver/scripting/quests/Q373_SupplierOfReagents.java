package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q373_SupplierOfReagents extends Quest {
    private static final String qn = "Q373_SupplierOfReagents";

    private static final String _ingredient = "ingredient";

    private static final String _catalyst = "catalyst";

    private static final int WESLEY = 30166;

    private static final int URN = 31149;

    private static final int CRENDION = 20813;

    private static final int HALLATE_MAID = 20822;

    private static final int HALLATE_GUARDIAN = 21061;

    private static final int PLATINUM_TRIBE_SHAMAN = 20828;

    private static final int PLATINUM_GUARDIAN_SHAMAN = 21066;

    private static final int LAVA_WYRM = 21111;

    private static final int HAMES_ORC_SHAMAN = 21115;

    private static final int MIXING_STONE = 5904;

    private static final int MIXING_MANUAL = 6317;

    private static final int REAGENT_POUCH_1 = 6007;

    private static final int REAGENT_POUCH_2 = 6008;

    private static final int REAGENT_POUCH_3 = 6009;

    private static final int REAGENT_BOX = 6010;

    private static final int WYRMS_BLOOD = 6011;

    private static final int LAVA_STONE = 6012;

    private static final int MOONSTONE_SHARD = 6013;

    private static final int ROTTEN_BONE = 6014;

    private static final int DEMONS_BLOOD = 6015;

    private static final int INFERNIUM_ORE = 6016;

    private static final int BLOOD_ROOT = 6017;

    private static final int VOLCANIC_ASH = 6018;

    private static final int QUICKSILVER = 6019;

    private static final int SULFUR = 6020;

    private static final int DEMONIC_ESSENCE = 6031;

    private static final int MIDNIGHT_OIL = 6030;

    private static final int DRACOPLASM = 6021;

    private static final int MAGMA_DUST = 6022;

    private static final int MOON_DUST = 6023;

    private static final int NECROPLASM = 6024;

    private static final int DEMONPLASM = 6025;

    private static final int INFERNO_DUST = 6026;

    private static final int FIRE_ESSENCE = 6028;

    private static final int LUNARGENT = 6029;

    private static final int DRACONIC_ESSENCE = 6027;

    private static final int ABYSS_OIL = 6032;

    private static final int HELLFIRE_OIL = 6033;

    private static final int NIGHTMARE_OIL = 6034;

    private static final int PURE_SILVER = 6320;

    private static final Map<Integer, int[]> DROPLIST = new HashMap<>();

    private static final int[][] FORMULAS = new int[][]{
            {10, 6011, 6017, 6021}, {10, 6012, 6018, 6022}, {10, 6013, 6018, 6023}, {10, 6014, 6017, 6024}, {10, 6015, 6017, 6025}, {10, 6016, 6018, 6026}, {10, 6021, 6019, 6027}, {10, 6022, 6020, 6028}, {10, 6023, 6019, 6029}, {10, 6024, 6019, 6030},
            {10, 6025, 6020, 6031}, {10, 6026, 6020, 6032}, {1, 6028, 6031, 6033}, {1, 6029, 6030, 6034}, {1, 6029, 6019, 6320}};

    private static final int[][] TEMPERATURES = new int[][]{{1, 100, 1}, {2, 45, 3}, {3, 15, 5}};

    public Q373_SupplierOfReagents() {
        super(373, "Supplier of Reagents");
        DROPLIST.put(21066, new int[]{6010, 442000, 0});
        DROPLIST.put(21115, new int[]{6009, 470000, 0});
        DROPLIST.put(20828, new int[]{6008, 6019, 680, 1000});
        DROPLIST.put(20822, new int[]{6007, 6018, 664, 844});
        DROPLIST.put(21061, new int[]{6015, 6013, 729, 833});
        DROPLIST.put(20813, new int[]{6014, 6019, 618, 1000});
        DROPLIST.put(21111, new int[]{6011, 6012, 505, 750});
        setItemsIds(5904, 6317);
        addStartNpc(30166);
        addTalkId(30166, 31149);
        addKillId(20813, 20822, 21061, 20828, 21066, 21111, 21115);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q373_SupplierOfReagents");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30166-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5904, 1);
            st.giveItems(6317, 1);
        } else if (event.equalsIgnoreCase("30166-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("31149-02.htm")) {
            if (!st.hasQuestItems(5904))
                htmltext = "31149-04.htm";
        } else if (event.startsWith("31149-03-")) {
            int regentId = Integer.parseInt(event.substring(9, 13));
            int[][] arrayOfInt;
            int i;
            byte b;
            for (arrayOfInt = FORMULAS, i = arrayOfInt.length, b = 0; b < i; ) {
                int[] formula = arrayOfInt[b];
                if (formula[1] != regentId) {
                    b++;
                    continue;
                }
                if (st.getQuestItemsCount(regentId) < formula[0])
                    break;
                st.set("ingredient", Integer.toString(regentId));
                return htmltext;
            }
            htmltext = "31149-04.htm";
        } else if (event.startsWith("31149-06-")) {
            int catalyst = Integer.parseInt(event.substring(9, 13));
            if (!st.hasQuestItems(catalyst))
                return "31149-04.htm";
            st.set("catalyst", Integer.toString(catalyst));
        } else if (event.startsWith("31149-12-")) {
            int regent = st.getInt("ingredient");
            int catalyst = st.getInt("catalyst");
            for (int[] formula : FORMULAS) {
                if (formula[1] == regent && formula[2] == catalyst) {
                    if (st.getQuestItemsCount(regent) < formula[0])
                        break;
                    if (!st.hasQuestItems(catalyst))
                        break;
                    st.takeItems(regent, formula[0]);
                    st.takeItems(catalyst, 1);
                    int tempIndex = Integer.parseInt(event.substring(9, 10));
                    int[][] arrayOfInt;
                    int i;
                    byte b;
                    for (arrayOfInt = TEMPERATURES, i = arrayOfInt.length, b = 0; b < i; ) {
                        int[] temperature = arrayOfInt[b];
                        if (temperature[0] != tempIndex) {
                            b++;
                            continue;
                        }
                        if (Rnd.get(100) < temperature[1]) {
                            st.giveItems(formula[3], temperature[2]);
                            return "31149-12-" + formula[3] + ".htm";
                        }
                        return "31149-11.htm";
                    }
                }
            }
            htmltext = "31149-13.htm";
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = Quest.getNoQuestMsg();
        QuestState st = player.getQuestState("Q373_SupplierOfReagents");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 57) ? "30166-01.htm" : "30166-02.htm";
                break;
            case 1:
                if (npc.getNpcId() == 30166) {
                    htmltext = "30166-05.htm";
                    break;
                }
                htmltext = "31149-01.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int[] drop = DROPLIST.get(npc.getNpcId());
        if (drop[2] == 0) {
            st.dropItems(drop[0], 1, 0, drop[1]);
        } else {
            int random = Rnd.get(1000);
            if (random < drop[3])
                st.dropItemsAlways((random < drop[2]) ? drop[0] : drop[1], 1, 0);
        }
        return null;
    }
}
