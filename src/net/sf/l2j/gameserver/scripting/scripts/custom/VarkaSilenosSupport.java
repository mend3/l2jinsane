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

public class VarkaSilenosSupport extends Quest {
    private static final String qn = "VarkaSilenosSupport";

    private static final int ASHAS = 31377;

    private static final int NARAN = 31378;

    private static final int UDAN = 31379;

    private static final int DIYABU = 31380;

    private static final int HAGOS = 31381;

    private static final int SHIKON = 31382;

    private static final int TERANU = 31383;

    private static final int SEED = 7187;

    private static final int[] VARKAS = new int[]{
            21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362,
            21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374,
            21375};

    private static final int[][] BUFF = new int[][]{{4359, 2}, {4360, 2}, {4345, 3}, {4355, 3}, {4352, 3}, {4354, 3}, {4356, 6}, {4357, 6}};

    private static final String[] varkaMissions = new String[]{"Q611_AllianceWithVarkaSilenos", "Q612_WarWithKetraOrcs", "Q613_ProveYourCourage", "Q614_SlayTheEnemyCommander", "Q615_MagicalPowerOfFire_Part1", "Q616_MagicalPowerOfFire_Part2"};

    public VarkaSilenosSupport() {
        super(-1, "custom");
        addFirstTalkId(31377, 31378, 31379, 31380, 31381, 31382, 31383);
        addTalkId(31379, 31381, 31383);
        addStartNpc(31381, 31383);
        addKillId(VARKAS);
        addSkillSeeId(VARKAS);
    }

    private static void testVarkaDemote(Player player) {
        if (player.isAlliedWithVarka()) {
            player.setAllianceWithVarkaKetra(0);
            PcInventory inventory = player.getInventory();
            for (int i = 7225; i >= 7221; i--) {
                ItemInstance item = inventory.getItemByItemId(i);
                if (item != null) {
                    player.destroyItemByItemId("Quest", i, item.getCount(), player, true);
                    if (i != 7221)
                        player.addItem("Quest", i - 1, 1, player, true);
                    break;
                }
            }
            for (String mission : varkaMissions) {
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
            if (st.getQuestItemsCount(7187) >= buffInfo[1]) {
                htmltext = "31379-4.htm";
                st.takeItems(7187, buffInfo[1]);
                npc.setTarget(player);
                npc.doCast(SkillTable.getInstance().getInfo(buffInfo[0], 1));
                npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
            }
        } else if (event.equals("Withdraw")) {
            if (player.getWarehouse().getSize() == 0) {
                htmltext = "31381-0.htm";
            } else {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                player.setActiveWarehouse(player.getWarehouse());
                player.sendPacket(new WarehouseWithdrawList(player, 1));
            }
        } else if (event.equals("Teleport")) {
            htmltext = switch (player.getAllianceWithVarkaKetra()) {
                case -4 -> "31383-4.htm";
                case -5 -> "31383-5.htm";
                default -> htmltext;
            };
        }
        return htmltext;
    }

    public String onFirstTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("VarkaSilenosSupport");
        if (st == null)
            st = newQuestState(player);
        int allianceLevel = player.getAllianceWithVarkaKetra();
        switch (npc.getNpcId()) {
            case 31377:
                if (allianceLevel < 0) {
                    htmltext = "31377-friend.htm";
                    break;
                }
                htmltext = "31377-no.htm";
                break;
            case 31378:
                if (allianceLevel < 0) {
                    htmltext = "31378-friend.htm";
                    break;
                }
                htmltext = "31378-no.htm";
                break;
            case 31379:
                st.setState((byte) 1);
                if (allianceLevel > -1) {
                    htmltext = "31379-3.htm";
                    break;
                }
                if (allianceLevel > -3 && allianceLevel < 0) {
                    htmltext = "31379-1.htm";
                    break;
                }
                if (allianceLevel < -2) {
                    if (st.hasQuestItems(7187)) {
                        htmltext = "31379-4.htm";
                        break;
                    }
                    htmltext = "31379-2.htm";
                }
                break;
            case 31380:
                if (player.getKarma() >= 1) {
                    htmltext = "31380-pk.htm";
                    break;
                }
                if (allianceLevel >= 0) {
                    htmltext = "31380-no.htm";
                    break;
                }
                if (allianceLevel == -1 || allianceLevel == -2) {
                    htmltext = "31380-1.htm";
                    break;
                }
                htmltext = "31380-2.htm";
                break;
            case 31381:
                htmltext = switch (allianceLevel) {
                    case -1 -> "31381-1.htm";
                    case -3, -2 -> "31381-2.htm";
                    default -> htmltext;
                };
                if (allianceLevel >= 0) {
                    htmltext = "31381-no.htm";
                    break;
                }
                if (player.getWarehouse().getSize() == 0) {
                    htmltext = "31381-3.htm";
                    break;
                }
                htmltext = "31381-4.htm";
                break;
            case 31382:
                htmltext = switch (allianceLevel) {
                    case -2 -> "31382-1.htm";
                    case -4, -3 -> "31382-2.htm";
                    case -5 -> "31382-3.htm";
                    default -> htmltext;
                };
                htmltext = "31382-no.htm";
                break;
            case 31383:
                if (allianceLevel >= 0) {
                    htmltext = "31383-no.htm";
                    break;
                }
                if (allianceLevel < 0 && allianceLevel > -4) {
                    htmltext = "31383-1.htm";
                    break;
                }
                if (allianceLevel == -4) {
                    htmltext = "31383-2.htm";
                    break;
                }
                htmltext = "31383-3.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null) {
            Party party = player.getParty();
            if (party != null) {
                for (Player member : party.getMembers())
                    testVarkaDemote(member);
            } else {
                testVarkaDemote(player);
            }
        }
        return null;
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (caster.isAlliedWithVarka())
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
                                if (!player.isAlliedWithVarka())
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
