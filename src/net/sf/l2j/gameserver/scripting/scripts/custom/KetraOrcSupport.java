package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.WarehouseWithdrawList;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class KetraOrcSupport extends Quest {
    private static final String qn = "KetraOrcSupport";

    private static final int KADUN = 31370;

    private static final int WAHKAN = 31371;

    private static final int ASEFA = 31372;

    private static final int ATAN = 31373;

    private static final int JAFF = 31374;

    private static final int JUMARA = 31375;

    private static final int KURFA = 31376;

    private static final int HORN = 7186;

    private static final int[] KETRAS = new int[]{
            21324, 21325, 21327, 21328, 21329, 21331, 21332, 21334, 21335, 21336,
            21338, 21339, 21340, 21342, 21343, 21344, 21345, 21346, 21347, 21348,
            21349};

    private static final int[][] BUFF = new int[][]{{4359, 2}, {4360, 2}, {4345, 3}, {4355, 3}, {4352, 3}, {4354, 3}, {4356, 6}, {4357, 6}};

    private static final String[] ketraMissions = new String[]{"Q605_AllianceWithKetraOrcs", "Q606_WarWithVarkaSilenos", "Q607_ProveYourCourage", "Q608_SlayTheEnemyCommander", "Q609_MagicalPowerOfWater_Part1", "Q610_MagicalPowerOfWater_Part2"};

    public KetraOrcSupport() {
        super(-1, "custom");
        addFirstTalkId(31370, 31371, 31372, 31373, 31374, 31375, 31376);
        addTalkId(31372, 31374, 31376);
        addStartNpc(31374, 31376);
        addKillId(KETRAS);
        addSkillSeeId(KETRAS);
    }

    private static void testKetraDemote(Player player) {
        if (player.isAlliedWithKetra()) {
            player.setAllianceWithVarkaKetra(0);
            PcInventory inventory = player.getInventory();
            for (int i = 7215; i >= 7211; i--) {
                ItemInstance item = inventory.getItemByItemId(i);
                if (item != null) {
                    player.destroyItemByItemId("Quest", i, item.getCount(), player, true);
                    if (i != 7211)
                        player.addItem("Quest", i - 1, 1, player, true);
                    break;
                }
            }
            for (String mission : ketraMissions) {
                QuestState pst = player.getQuestState(mission);
                if (pst != null)
                    pst.exitQuest(true);
            }
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState(getName());
        if (st == null)
            return htmltext;
        if (StringUtil.isDigit(event)) {
            int[] buffInfo = BUFF[Integer.parseInt(event)];
            if (st.getQuestItemsCount(7186) >= buffInfo[1]) {
                htmltext = "31372-4.htm";
                st.takeItems(7186, buffInfo[1]);
                npc.setTarget(player);
                npc.doCast(SkillTable.getInstance().getInfo(buffInfo[0], 1));
                npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
            }
        } else if (event.equals("Withdraw")) {
            if (player.getWarehouse().getSize() == 0) {
                htmltext = "31374-0.htm";
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.setActiveWarehouse(player.getWarehouse());
                player.sendPacket(new WarehouseWithdrawList(player, 1));
            }
        } else if (event.equals("Teleport")) {
            htmltext = switch (player.getAllianceWithVarkaKetra()) {
                case 4 -> "31376-4.htm";
                case 5 -> "31376-5.htm";
                default -> htmltext;
            };
        }
        return htmltext;
    }

    public String onFirstTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("KetraOrcSupport");
        if (st == null)
            st = newQuestState(player);
        int allianceLevel = player.getAllianceWithVarkaKetra();
        switch (npc.getNpcId()) {
            case 31370:
                if (allianceLevel > 0) {
                    htmltext = "31370-friend.htm";
                    break;
                }
                htmltext = "31370-no.htm";
                break;
            case 31371:
                if (allianceLevel > 0) {
                    htmltext = "31371-friend.htm";
                    break;
                }
                htmltext = "31371-no.htm";
                break;
            case 31372:
                st.setState((byte) 1);
                if (allianceLevel < 1) {
                    htmltext = "31372-3.htm";
                    break;
                }
                if (allianceLevel < 3 && allianceLevel > 0) {
                    htmltext = "31372-1.htm";
                    break;
                }
                if (allianceLevel > 2) {
                    if (st.hasQuestItems(7186)) {
                        htmltext = "31372-4.htm";
                        break;
                    }
                    htmltext = "31372-2.htm";
                }
                break;
            case 31373:
                if (player.getKarma() >= 1) {
                    htmltext = "31373-pk.htm";
                    break;
                }
                if (allianceLevel <= 0) {
                    htmltext = "31373-no.htm";
                    break;
                }
                if (allianceLevel == 1 || allianceLevel == 2) {
                    htmltext = "31373-1.htm";
                    break;
                }
                htmltext = "31373-2.htm";
                break;
            case 31374:
                htmltext = switch (allianceLevel) {
                    case 1 -> "31374-1.htm";
                    case 2, 3 -> "31374-2.htm";
                    default -> htmltext;
                };
                if (allianceLevel <= 0) {
                    htmltext = "31374-no.htm";
                    break;
                }
                if (player.getWarehouse().getSize() == 0) {
                    htmltext = "31374-3.htm";
                    break;
                }
                htmltext = "31374-4.htm";
                break;
            case 31375:
                htmltext = switch (allianceLevel) {
                    case 2 -> "31375-1.htm";
                    case 3, 4 -> "31375-2.htm";
                    case 5 -> "31375-3.htm";
                    default -> htmltext;
                };
                htmltext = "31375-no.htm";
                break;
            case 31376:
                if (allianceLevel <= 0) {
                    htmltext = "31376-no.htm";
                    break;
                }
                if (allianceLevel > 0 && allianceLevel < 4) {
                    htmltext = "31376-1.htm";
                    break;
                }
                if (allianceLevel == 4) {
                    htmltext = "31376-2.htm";
                    break;
                }
                htmltext = "31376-3.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null) {
            Party party = player.getParty();
            if (party != null) {
                for (Player partyMember : party.getMembers())
                    testKetraDemote(partyMember);
            } else {
                testKetraDemote(player);
            }
        }
        return null;
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (caster.isAlliedWithKetra())
            switch (skill.getSkillType()) {
                case BUFF:
                case HEAL:
                case HEAL_PERCENT:
                case HEAL_STATIC:
                case BALANCE_LIFE:
                case HOT:
                    for (Creature target : (Creature[]) targets) {
                        if (target != null && !target.isDead() && target != caster)
                            if (target instanceof net.sf.l2j.gameserver.model.actor.Playable) {
                                Player player = target.getActingPlayer();
                                if (!player.isAlliedWithKetra())
                                    if (((Attackable) npc).getAggroList().containsKey(player)) {
                                        WorldObject oldTarget = npc.getTarget();
                                        npc.setTarget((isPet && player.getSummon() != null) ? (WorldObject) caster.getSummon() : (WorldObject) caster);
                                        npc.doCast(SkillTable.FrequentSkill.VARKA_KETRA_PETRIFICATION.getSkill());
                                        npc.setTarget(oldTarget);
                                        break;
                                    }
                            }
                    }
                    break;
            }
        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }
}
