package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.type.SummonAI;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;

public final class RequestActionUse extends L2GameClientPacket {
    private static final int[] PASSIVE_SUMMONS = new int[]{
            12564, 12621, 14702, 14703, 14704, 14705, 14706, 14707, 14708, 14709,
            14710, 14711, 14712, 14713, 14714, 14715, 14716, 14717, 14718, 14719,
            14720, 14721, 14722, 14723, 14724, 14725, 14726, 14727, 14728, 14729,
            14730, 14731, 14732, 14733, 14734, 14735, 14736};

    private static final int SIN_EATER_ID = 12564;

    private static final String[] SIN_EATER_ACTIONS_STRINGS = new String[]{"special skill? Abuses in this kind of place, can turn blood Knots...!", "Hey! Brother! What do you anticipate to me?", "shouts ha! Flap! Flap! Response?", ", has not hit...!"};

    private int _actionId;

    private boolean _ctrlPressed;

    private boolean _shiftPressed;

    protected void readImpl() {
        this._actionId = readD();
        this._ctrlPressed = (readD() == 1);
        this._shiftPressed = (readC() == 1);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if ((player.isFakeDeath() && this._actionId != 0) || player.isDead() || player.isOutOfControl()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Summon summon = player.getSummon();
        WorldObject target = player.getTarget();
        switch (this._actionId) {
            case 0:
                player.tryToSitOrStand(target, player.isSitting());
            case 1:
                if (player.isMounted())
                    return;
                if (player.isRunning()) {
                    player.setWalking();
                } else {
                    player.setRunning();
                }
            case 10:
                player.tryOpenPrivateSellStore(false);
            case 28:
                player.tryOpenPrivateBuyStore();
            case 15:
            case 21:
                if (summon == null)
                    return;
                if (summon.getFollowStatus() && MathUtil.calculateDistance(player, summon, true) > 2000.0D)
                    return;
                if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                    return;
                }
                ((SummonAI) summon.getAI()).notifyFollowStatusChange();
            case 16:
            case 22:
                if (!(target instanceof Creature) || summon == null || summon == target || player == target)
                    return;
                if (ArraysUtil.contains(PASSIVE_SUMMONS, summon.getNpcId()))
                    return;
                if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                    return;
                }
                if (summon.isAttackingDisabled()) {
                    if (summon.getAttackEndTime() <= System.currentTimeMillis())
                        return;
                    summon.getAI().setIntention(IntentionType.ATTACK, target);
                }
                if (summon instanceof Pet && summon.getLevel() - player.getLevel() > 20) {
                    player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
                    return;
                }
                if (player.isInOlympiadMode() && !player.isOlympiadStart())
                    return;
                summon.setTarget(target);
                if (!target.isAutoAttackable(player) && !this._ctrlPressed && !(target instanceof net.sf.l2j.gameserver.model.actor.instance.Folk)) {
                    summon.setFollowStatus(false);
                    summon.getAI().setIntention(IntentionType.FOLLOW, target);
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    return;
                }
                if (target instanceof Door) {
                    if (target.isAutoAttackable(player) && summon.getNpcId() != 14839)
                        summon.getAI().setIntention(IntentionType.ATTACK, target);
                } else if (summon.getNpcId() != 14737) {
                    if (Creature.isInsidePeaceZone(summon, target)) {
                        summon.setFollowStatus(false);
                        summon.getAI().setIntention(IntentionType.FOLLOW, target);
                    } else {
                        summon.getAI().setIntention(IntentionType.ATTACK, target);
                    }
                }
            case 17:
            case 23:
                if (summon == null)
                    return;
                if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                    return;
                }
                summon.getAI().setIntention(IntentionType.ACTIVE, null);
            case 19:
                if (summon == null || !(summon instanceof Pet))
                    return;
                if (summon.isDead()) {
                    player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
                } else if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                } else if (summon.isAttackingNow() || summon.isInCombat()) {
                    player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
                } else if (((Pet) summon).checkUnsummonState()) {
                    player.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
                } else {
                    summon.unSummon(player);
                }
            case 38:
                player.mountPlayer(summon);
            case 32:
                return;
            case 36:
                useSkill(4259, target);
            case 37:
                player.tryOpenWorkshop(true);
            case 39:
                useSkill(4138, target);
            case 41:
                if (!(target instanceof Door)) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    return;
                }
                useSkill(4230, target);
            case 42:
                useSkill(4378, player);
            case 43:
                useSkill(4137, target);
            case 44:
                useSkill(4139, target);
            case 45:
                useSkill(4025, player);
            case 46:
                useSkill(4261, target);
            case 47:
                useSkill(4260, target);
            case 48:
                useSkill(4068, target);
            case 51:
                player.tryOpenWorkshop(false);
            case 52:
                if (summon == null || !(summon instanceof net.sf.l2j.gameserver.model.actor.instance.Servitor))
                    return;
                if (summon.isDead()) {
                    player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
                } else if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                } else if (summon.isAttackingNow() || summon.isInCombat()) {
                    player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
                } else {
                    summon.unSummon(player);
                }
            case 53:
            case 54:
                if (target == null || summon == null || summon == target)
                    return;
                if (summon.isOutOfControl()) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
                    return;
                }
                summon.setFollowStatus(false);
                summon.getAI().setIntention(IntentionType.MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
            case 61:
                player.tryOpenPrivateSellStore(true);
            case 1000:
                if (!(target instanceof Door)) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    return;
                }
                useSkill(4079, target);
            case 1001:
                if (useSkill(4139, summon) && summon.getNpcId() == 12564 && Rnd.get(100) < 10)
                    summon.broadcastPacket(new NpcSay(summon.getObjectId(), 0, summon.getNpcId(), (String) Rnd.get((Object[]) SIN_EATER_ACTIONS_STRINGS)));
            case 1003:
                useSkill(4710, target);
            case 1004:
                useSkill(4711, player);
            case 1005:
                useSkill(4712, target);
            case 1006:
                useSkill(4713, player);
            case 1007:
                useSkill(4699, player);
            case 1008:
                useSkill(4700, player);
            case 1009:
                useSkill(4701, target);
            case 1010:
                useSkill(4702, player);
            case 1011:
                useSkill(4703, player);
            case 1012:
                useSkill(4704, target);
            case 1013:
                useSkill(4705, target);
            case 1014:
                useSkill(4706, player);
            case 1015:
                useSkill(4707, target);
            case 1016:
                useSkill(4709, target);
            case 1017:
                useSkill(4708, target);
            case 1031:
                useSkill(5135, target);
            case 1032:
                useSkill(5136, target);
            case 1033:
                useSkill(5137, target);
            case 1034:
                useSkill(5138, target);
            case 1035:
                useSkill(5139, target);
            case 1036:
                useSkill(5142, target);
            case 1037:
                useSkill(5141, target);
            case 1038:
                useSkill(5140, target);
            case 1039:
                if (target instanceof Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    return;
                }
                useSkill(5110, target);
            case 1040:
                if (target instanceof Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    return;
                }
                useSkill(5111, target);
        }
        LOGGER.warn("Unhandled action type {} detected for {}.", this._actionId, player.getName());
    }

    private boolean useSkill(int skillId, WorldObject target) {
        Player player = getClient().getPlayer();
        if (player == null || player.isInStoreMode())
            return false;
        Summon summon = player.getSummon();
        if (summon == null)
            return false;
        if (summon instanceof Pet && summon.getLevel() - player.getLevel() > 20) {
            player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
            return false;
        }
        if (summon.isOutOfControl()) {
            player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
            return false;
        }
        L2Skill skill = summon.getSkill(skillId);
        if (skill == null)
            return false;
        if (skill.isOffensive() && player == target)
            return false;
        summon.setTarget(target);
        return summon.useMagic(skill, this._ctrlPressed, this._shiftPressed);
    }
}
