package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q330_AdeptOfTaste extends Quest {
    private static final String qn = "Q330_AdeptOfTaste";

    private static final int SONIA = 30062;

    private static final int GLYVKA = 30067;

    private static final int ROLLANT = 30069;

    private static final int JACOB = 30073;

    private static final int PANO = 30078;

    private static final int MIRIEN = 30461;

    private static final int JONAS = 30469;

    private static final int INGREDIENT_LIST = 1420;

    private static final int SONIA_BOTANY_BOOK = 1421;

    private static final int RED_MANDRAGORA_ROOT = 1422;

    private static final int WHITE_MANDRAGORA_ROOT = 1423;

    private static final int RED_MANDRAGORA_SAP = 1424;

    private static final int WHITE_MANDRAGORA_SAP = 1425;

    private static final int JACOB_INSECT_BOOK = 1426;

    private static final int NECTAR = 1427;

    private static final int ROYAL_JELLY = 1428;

    private static final int HONEY = 1429;

    private static final int GOLDEN_HONEY = 1430;

    private static final int PANO_CONTRACT = 1431;

    private static final int HOBGOBLIN_AMULET = 1432;

    private static final int DIONIAN_POTATO = 1433;

    private static final int GLYVKA_BOTANY_BOOK = 1434;

    private static final int GREEN_MARSH_MOSS = 1435;

    private static final int BROWN_MARSH_MOSS = 1436;

    private static final int GREEN_MOSS_BUNDLE = 1437;

    private static final int BROWN_MOSS_BUNDLE = 1438;

    private static final int ROLANT_CREATURE_BOOK = 1439;

    private static final int MONSTER_EYE_BODY = 1440;

    private static final int MONSTER_EYE_MEAT = 1441;

    private static final int JONAS_STEAK_DISH_1 = 1442;

    private static final int JONAS_STEAK_DISH_2 = 1443;

    private static final int JONAS_STEAK_DISH_3 = 1444;

    private static final int JONAS_STEAK_DISH_4 = 1445;

    private static final int JONAS_STEAK_DISH_5 = 1446;

    private static final int MIRIEN_REVIEW_1 = 1447;

    private static final int MIRIEN_REVIEW_2 = 1448;

    private static final int MIRIEN_REVIEW_3 = 1449;

    private static final int MIRIEN_REVIEW_4 = 1450;

    private static final int MIRIEN_REVIEW_5 = 1451;

    private static final int JONAS_SALAD_RECIPE = 1455;

    private static final int JONAS_SAUCE_RECIPE = 1456;

    private static final int JONAS_STEAK_RECIPE = 1457;

    private static final Map<Integer, int[]> CHANCES = new HashMap<>();

    public Q330_AdeptOfTaste() {
        super(330, "Adept of Taste");
        CHANCES.put(20204, new int[]{92, 100});
        CHANCES.put(20229, new int[]{80, 95});
        CHANCES.put(20223, new int[]{70, 77});
        CHANCES.put(20154, new int[]{70, 77});
        CHANCES.put(20155, new int[]{87, 96});
        CHANCES.put(20156, new int[]{77, 85});
        setItemsIds(1420, 1424, 1425, 1429, 1430, 1433, 1437, 1438, 1441, 1447,
                1448, 1449, 1450, 1451, 1442, 1443, 1444, 1445, 1446, 1421,
                1422, 1423, 1426, 1427, 1428, 1431, 1432, 1434, 1435, 1436,
                1439, 1440);
        addStartNpc(30469);
        addTalkId(30469, 30062, 30067, 30069, 30073, 30078, 30461);
        addKillId(20147, 20154, 20155, 20156, 20204, 20223, 20226, 20228, 20229, 20265,
                20266);
    }

    private static boolean hasAllIngredients(QuestState st) {
        return (st.hasQuestItems(1433, 1441) && st.hasAtLeastOneQuestItem(1425, 1424) && st.hasAtLeastOneQuestItem(1430, 1429) && st.hasAtLeastOneQuestItem(1438, 1437));
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q330_AdeptOfTaste");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30469-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1420, 1);
        } else if (event.equalsIgnoreCase("30062-05.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(1421, 1);
            st.takeItems(1422, -1);
            st.takeItems(1423, -1);
            st.giveItems(1424, 1);
        } else if (event.equalsIgnoreCase("30073-05.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(1426, 1);
            st.takeItems(1427, -1);
            st.takeItems(1428, -1);
            st.giveItems(1429, 1);
        } else if (event.equalsIgnoreCase("30067-05.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(1434, 1);
            st.takeItems(1435, -1);
            st.takeItems(1436, -1);
            st.giveItems(1437, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q330_AdeptOfTaste");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 24) ? "30469-01.htm" : "30469-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30469:
                        if (st.hasQuestItems(1420)) {
                            int dish;
                            if (!hasAllIngredients(st)) {
                                htmltext = "30469-04.htm";
                                break;
                            }
                            int specialIngredientsNumber = st.getQuestItemsCount(1425) + st.getQuestItemsCount(1430) + st.getQuestItemsCount(1438);
                            if (Rnd.nextBoolean()) {
                                htmltext = "30469-05t" + (specialIngredientsNumber + 2) + ".htm";
                                dish = 1443 + specialIngredientsNumber;
                            } else {
                                htmltext = "30469-05t" + (specialIngredientsNumber + 1) + ".htm";
                                dish = 1442 + specialIngredientsNumber;
                            }
                            st.playSound((dish == 1446) ? "ItemSound.quest_jackpot" : "ItemSound.quest_itemget");
                            st.takeItems(1420, 1);
                            st.takeItems(1424, 1);
                            st.takeItems(1425, 1);
                            st.takeItems(1429, 1);
                            st.takeItems(1430, 1);
                            st.takeItems(1433, 1);
                            st.takeItems(1437, 1);
                            st.takeItems(1438, 1);
                            st.takeItems(1441, 1);
                            st.giveItems(dish, 1);
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1442, 1443, 1444, 1445, 1446)) {
                            htmltext = "30469-06.htm";
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1447, 1448, 1449, 1450, 1451)) {
                            if (st.hasQuestItems(1447)) {
                                htmltext = "30469-06t1.htm";
                                st.takeItems(1447, 1);
                                st.rewardItems(57, 7500);
                                st.rewardExpAndSp(6000L, 0);
                            } else if (st.hasQuestItems(1448)) {
                                htmltext = "30469-06t2.htm";
                                st.takeItems(1448, 1);
                                st.rewardItems(57, 9000);
                                st.rewardExpAndSp(7000L, 0);
                            } else if (st.hasQuestItems(1449)) {
                                htmltext = "30469-06t3.htm";
                                st.takeItems(1449, 1);
                                st.rewardItems(57, 5800);
                                st.giveItems(1455, 1);
                                st.rewardExpAndSp(9000L, 0);
                            } else if (st.hasQuestItems(1450)) {
                                htmltext = "30469-06t4.htm";
                                st.takeItems(1450, 1);
                                st.rewardItems(57, 6800);
                                st.giveItems(1456, 1);
                                st.rewardExpAndSp(10500L, 0);
                            } else if (st.hasQuestItems(1451)) {
                                htmltext = "30469-06t5.htm";
                                st.takeItems(1451, 1);
                                st.rewardItems(57, 7800);
                                st.giveItems(1457, 1);
                                st.rewardExpAndSp(12000L, 0);
                            }
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30461:
                        if (st.hasQuestItems(1420)) {
                            htmltext = "30461-01.htm";
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1442, 1443, 1444, 1445, 1446)) {
                            st.playSound("ItemSound.quest_itemget");
                            if (st.hasQuestItems(1442)) {
                                htmltext = "30461-02t1.htm";
                                st.takeItems(1442, 1);
                                st.giveItems(1447, 1);
                                break;
                            }
                            if (st.hasQuestItems(1443)) {
                                htmltext = "30461-02t2.htm";
                                st.takeItems(1443, 1);
                                st.giveItems(1448, 1);
                                break;
                            }
                            if (st.hasQuestItems(1444)) {
                                htmltext = "30461-02t3.htm";
                                st.takeItems(1444, 1);
                                st.giveItems(1449, 1);
                                break;
                            }
                            if (st.hasQuestItems(1445)) {
                                htmltext = "30461-02t4.htm";
                                st.takeItems(1445, 1);
                                st.giveItems(1450, 1);
                                break;
                            }
                            if (st.hasQuestItems(1446)) {
                                htmltext = "30461-02t5.htm";
                                st.takeItems(1446, 1);
                                st.giveItems(1451, 1);
                            }
                            break;
                        }
                        if (st.hasAtLeastOneQuestItem(1447, 1448, 1449, 1450, 1451))
                            htmltext = "30461-04.htm";
                        break;
                    case 30062:
                        if (!st.hasQuestItems(1424) && !st.hasQuestItems(1425)) {
                            if (!st.hasQuestItems(1421)) {
                                htmltext = "30062-01.htm";
                                st.giveItems(1421, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.getQuestItemsCount(1422) < 40 || st.getQuestItemsCount(1423) < 40) {
                                htmltext = "30062-02.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(1423) >= 40) {
                                htmltext = "30062-06.htm";
                                st.takeItems(1421, 1);
                                st.takeItems(1422, -1);
                                st.takeItems(1423, -1);
                                st.giveItems(1425, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30062-03.htm";
                            break;
                        }
                        htmltext = "30062-07.htm";
                        break;
                    case 30073:
                        if (!st.hasQuestItems(1429) && !st.hasQuestItems(1430)) {
                            if (!st.hasQuestItems(1426)) {
                                htmltext = "30073-01.htm";
                                st.giveItems(1426, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.getQuestItemsCount(1427) < 20) {
                                htmltext = "30073-02.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(1428) < 10) {
                                htmltext = "30073-03.htm";
                                break;
                            }
                            htmltext = "30073-06.htm";
                            st.takeItems(1426, 1);
                            st.takeItems(1427, -1);
                            st.takeItems(1428, -1);
                            st.giveItems(1430, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30073-07.htm";
                        break;
                    case 30078:
                        if (!st.hasQuestItems(1433)) {
                            if (!st.hasQuestItems(1431)) {
                                htmltext = "30078-01.htm";
                                st.giveItems(1431, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.getQuestItemsCount(1432) < 30) {
                                htmltext = "30078-02.htm";
                                break;
                            }
                            htmltext = "30078-03.htm";
                            st.takeItems(1431, 1);
                            st.takeItems(1432, -1);
                            st.giveItems(1433, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30078-04.htm";
                        break;
                    case 30067:
                        if (!st.hasQuestItems(1437) && !st.hasQuestItems(1438)) {
                            if (!st.hasQuestItems(1434)) {
                                st.giveItems(1434, 1);
                                htmltext = "30067-01.htm";
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.getQuestItemsCount(1435) < 20 || st.getQuestItemsCount(1436) < 20) {
                                htmltext = "30067-02.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(1436) >= 20) {
                                htmltext = "30067-06.htm";
                                st.takeItems(1434, 1);
                                st.takeItems(1435, -1);
                                st.takeItems(1436, -1);
                                st.giveItems(1438, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            htmltext = "30067-03.htm";
                            break;
                        }
                        htmltext = "30067-07.htm";
                        break;
                    case 30069:
                        if (!st.hasQuestItems(1441)) {
                            if (!st.hasQuestItems(1439)) {
                                htmltext = "30069-01.htm";
                                st.giveItems(1439, 1);
                                st.playSound("ItemSound.quest_itemget");
                                break;
                            }
                            if (st.getQuestItemsCount(1440) < 30) {
                                htmltext = "30069-02.htm";
                                break;
                            }
                            htmltext = "30069-03.htm";
                            st.takeItems(1439, 1);
                            st.takeItems(1440, -1);
                            st.giveItems(1441, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        htmltext = "30069-04.htm";
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
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 20265:
                if (st.hasQuestItems(1439))
                    st.dropItems(1440, (Rnd.get(97) < 77) ? 2 : 3, 30, 970000);
                break;
            case 20266:
                if (st.hasQuestItems(1439))
                    st.dropItemsAlways(1440, (Rnd.get(10) < 7) ? 1 : 2, 30);
                break;
            case 20226:
                if (st.hasQuestItems(1434))
                    st.dropItems((Rnd.get(96) < 87) ? 1435 : 1436, 1, 20, 960000);
                break;
            case 20228:
                if (st.hasQuestItems(1434))
                    st.dropItemsAlways((Rnd.get(10) < 9) ? 1435 : 1436, 1, 20);
                break;
            case 20147:
                if (st.hasQuestItems(1431))
                    st.dropItemsAlways(1432, 1, 30);
                break;
            case 20204:
            case 20229:
                if (st.hasQuestItems(1426)) {
                    int random = Rnd.get(100);
                    int[] chances = CHANCES.get(npcId);
                    if (random < chances[0]) {
                        st.dropItemsAlways(1427, 1, 20);
                        break;
                    }
                    if (random < chances[1])
                        st.dropItemsAlways(1428, 1, 10);
                }
                break;
            case 20154:
            case 20155:
            case 20156:
            case 20223:
                if (st.hasQuestItems(1421)) {
                    int random = Rnd.get(100);
                    int[] chances = CHANCES.get(npcId);
                    if (random < chances[1])
                        st.dropItemsAlways((random < chances[0]) ? 1422 : 1423, 1, 40);
                }
                break;
        }
        return null;
    }
}
