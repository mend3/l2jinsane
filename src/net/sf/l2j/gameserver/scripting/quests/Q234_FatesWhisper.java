package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q234_FatesWhisper extends Quest {
    private static final String qn = "Q234_FatesWhisper";

    private static final int REIRIA_SOUL_ORB = 4666;

    private static final int KERMON_INFERNIUM_SCEPTER = 4667;

    private static final int GOLKONDA_INFERNIUM_SCEPTER = 4668;

    private static final int HALLATE_INFERNIUM_SCEPTER = 4669;

    private static final int INFERNIUM_VARNISH = 4672;

    private static final int REORIN_HAMMER = 4670;

    private static final int REORIN_MOLD = 4671;

    private static final int PIPETTE_KNIFE = 4665;

    private static final int RED_PIPETTE_KNIFE = 4673;

    private static final int CRYSTAL_B = 1460;

    private static final int STAR_OF_DESTINY = 5011;

    private static final Map<Integer, Integer> CHEST_SPAWN = new HashMap<>();

    private static final Map<Integer, String> WEAPONS = new HashMap<>();

    public Q234_FatesWhisper() {
        super(234, "Fate's Whispers");
        CHEST_SPAWN.put(Integer.valueOf(25035), Integer.valueOf(31027));
        CHEST_SPAWN.put(Integer.valueOf(25054), Integer.valueOf(31028));
        CHEST_SPAWN.put(Integer.valueOf(25126), Integer.valueOf(31029));
        CHEST_SPAWN.put(Integer.valueOf(25220), Integer.valueOf(31030));
        WEAPONS.put(Integer.valueOf(79), "Sword of Damascus");
        WEAPONS.put(Integer.valueOf(97), "Lance");
        WEAPONS.put(Integer.valueOf(171), "Deadman's Glory");
        WEAPONS.put(Integer.valueOf(175), "Art of Battle Axe");
        WEAPONS.put(Integer.valueOf(210), "Staff of Evil Spirits");
        WEAPONS.put(Integer.valueOf(234), "Demon Dagger");
        WEAPONS.put(Integer.valueOf(268), "Bellion Cestus");
        WEAPONS.put(Integer.valueOf(287), "Bow of Peril");
        WEAPONS.put(Integer.valueOf(2626), "Samurai Dual-sword");
        WEAPONS.put(Integer.valueOf(7883), "Guardian Sword");
        WEAPONS.put(Integer.valueOf(7889), "Wizard's Tear");
        WEAPONS.put(Integer.valueOf(7893), "Kaim Vanul's Bones");
        WEAPONS.put(Integer.valueOf(7901), "Star Buster");
        setItemsIds(4665, 4673);
        addStartNpc(31002);
        addTalkId(31002, 30182, 30847, 30178, 30833, 31028, 31029, 31030, 31027);
        addKillId(25035, 25054, 25126, 25220);
        addAttackId(29020);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q234_FatesWhisper");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31002-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30182-01c.htm")) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(4672, 1);
        } else if (event.equalsIgnoreCase("30178-01a.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30833-01b.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4665, 1);
        } else if (event.startsWith("selectBGrade_")) {
            if (st.getInt("bypass") == 1)
                return null;
            String bGradeId = event.replace("selectBGrade_", "");
            st.set("weaponId", bGradeId);
            htmltext = getHtmlText("31002-13.htm").replace("%weaponname%", WEAPONS.get(Integer.valueOf(st.getInt("weaponId"))));
        } else if (event.startsWith("confirmWeapon")) {
            st.set("bypass", "1");
            htmltext = getHtmlText("31002-14.htm").replace("%weaponname%", WEAPONS.get(Integer.valueOf(st.getInt("weaponId"))));
        } else if (event.startsWith("selectAGrade_")) {
            if (st.getInt("bypass") == 1) {
                int itemId = st.getInt("weaponId");
                if (st.hasQuestItems(itemId)) {
                    int aGradeItemId = Integer.parseInt(event.replace("selectAGrade_", ""));
                    htmltext = getHtmlText("31002-12.htm").replace("%weaponname%", ItemTable.getInstance().getTemplate(aGradeItemId).getName());
                    st.takeItems(itemId, 1);
                    st.giveItems(aGradeItemId, 1);
                    st.giveItems(5011, 1);
                    player.broadcastPacket(new SocialAction(player, 3));
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
                } else {
                    htmltext = getHtmlText("31002-15.htm").replace("%weaponname%", WEAPONS.get(Integer.valueOf(itemId)));
                }
            } else {
                htmltext = "31002-16.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond, itemId;
        QuestState st = player.getQuestState("Q234_FatesWhisper");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 75) ? "31002-01.htm" : "31002-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31002:
                        if (cond == 1) {
                            if (!st.hasQuestItems(4666)) {
                                htmltext = "31002-04b.htm";
                                break;
                            }
                            htmltext = "31002-05.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4666, 1);
                            break;
                        }
                        if (cond == 2) {
                            if (!st.hasQuestItems(4667) || !st.hasQuestItems(4668) || !st.hasQuestItems(4669)) {
                                htmltext = "31002-05c.htm";
                                break;
                            }
                            htmltext = "31002-06.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4668, 1);
                            st.takeItems(4669, 1);
                            st.takeItems(4667, 1);
                            break;
                        }
                        if (cond == 3) {
                            if (!st.hasQuestItems(4672)) {
                                htmltext = "31002-06b.htm";
                                break;
                            }
                            htmltext = "31002-07.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4672, 1);
                            break;
                        }
                        if (cond == 4) {
                            if (!st.hasQuestItems(4670)) {
                                htmltext = "31002-07b.htm";
                                break;
                            }
                            htmltext = "31002-08.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4670, 1);
                            break;
                        }
                        if (cond > 4 && cond < 8) {
                            htmltext = "31002-08b.htm";
                            break;
                        }
                        if (cond == 8) {
                            htmltext = "31002-09.htm";
                            st.set("cond", "9");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4671, 1);
                            break;
                        }
                        if (cond == 9) {
                            if (st.getQuestItemsCount(1460) < 984) {
                                htmltext = "31002-09b.htm";
                                break;
                            }
                            htmltext = "31002-BGradeList.htm";
                            st.set("cond", "10");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1460, 984);
                            break;
                        }
                        if (cond == 10) {
                            if (st.getInt("bypass") == 1) {
                                int i = st.getInt("weaponId");
                                htmltext = getHtmlText(st.hasQuestItems(i) ? "31002-AGradeList.htm" : "31002-15.htm").replace("%weaponname%", WEAPONS.get(Integer.valueOf(i)));
                                break;
                            }
                            htmltext = "31002-BGradeList.htm";
                        }
                        break;
                    case 30182:
                        if (cond == 3)
                            htmltext = !st.hasQuestItems(4672) ? "30182-01.htm" : "30182-02.htm";
                        break;
                    case 30847:
                        if (cond == 4 && !st.hasQuestItems(4670)) {
                            htmltext = "30847-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(4670, 1);
                            break;
                        }
                        if (cond >= 4 && st.hasQuestItems(4670))
                            htmltext = "30847-02.htm";
                        break;
                    case 30178:
                        if (cond == 5) {
                            htmltext = "30178-01.htm";
                            break;
                        }
                        if (cond > 5)
                            htmltext = "30178-02.htm";
                        break;
                    case 30833:
                        if (cond == 6) {
                            htmltext = "30833-01.htm";
                            break;
                        }
                        if (cond == 7) {
                            if (st.hasQuestItems(4665) && !st.hasQuestItems(4673)) {
                                htmltext = "30833-02.htm";
                                break;
                            }
                            htmltext = "30833-03.htm";
                            st.set("cond", "8");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(4673, 1);
                            st.giveItems(4671, 1);
                            break;
                        }
                        if (cond > 7)
                            htmltext = "30833-04.htm";
                        break;
                    case 31027:
                        if (cond == 1 && !st.hasQuestItems(4666)) {
                            htmltext = "31027-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(4666, 1);
                            break;
                        }
                        htmltext = "31027-02.htm";
                        break;
                    case 31028:
                    case 31029:
                    case 31030:
                        itemId = npc.getNpcId() - 26361;
                        if (cond == 2 && !st.hasQuestItems(itemId)) {
                            htmltext = npc.getNpcId() + "-01.htm";
                            st.playSound("ItemSound.quest_itemget");
                            st.giveItems(itemId, 1);
                            break;
                        }
                        htmltext = npc.getNpcId() + "-02.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "7");
        if (st == null)
            return null;
        if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemId() == 4665 && !st.hasQuestItems(4673)) {
            st.playSound("ItemSound.quest_itemget");
            st.takeItems(4665, 1);
            st.giveItems(4673, 1);
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        addSpawn(CHEST_SPAWN.get(Integer.valueOf(npc.getNpcId())), npc, true, 120000L, false);
        return null;
    }
}
