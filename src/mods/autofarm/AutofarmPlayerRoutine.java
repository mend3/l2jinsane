package mods.autofarm;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.VoicedAutoFarm;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutofarmPlayerRoutine {
    private final Player player;

    private ScheduledFuture<?> _task;

    private Creature committedTarget = null;

    public AutofarmPlayerRoutine(Player player) {
        this.player = player;
    }

    private static boolean isSpoil(Integer skillId) {
        return (skillId == 254 || skillId == 302);
    }

    public static List<Monster> getKnownMonstersInRadius(Player player, int radius, Function<Monster, Boolean> condition) {
        WorldRegion region = player.getRegion();
        if (region == null)
            return Collections.emptyList();
        List<Monster> result = new ArrayList<>();
        for (WorldRegion reg : region.getSurroundingRegions()) {
            for (WorldObject obj : reg.getObjects()) {
                if (!(obj instanceof Monster) || !MathUtil.checkIfInRange(radius, player, obj, true) || !(Boolean) condition.apply((Monster) obj))
                    continue;
                result.add((Monster) obj);
            }
        }
        return result;
    }

    public void start() {
        if (!Config.AUTOFARM_WITHOUT_RESTRICTION) {
            if (!this.player.isInsideZone(ZoneId.AUTOFARMZONE) || !this.player.isInsideZone(ZoneId.PARTYFARMZONE) || !this.player.isInsideZone(ZoneId.PEACE)) {
                this.player.sendPacket(new ExShowScreenMessage("You can't use AutoFarm in this zone.", 5000));
                this.player.sendPacket(new CreatureSay(0, 2, this.player.getName(), ": You can't use AutoFarm in this zone."));
                return;
            }
            if (this.player.isInsideZone(ZoneId.AUTOFARMZONE) && !Config.AUTOFARM_L2AutoFarmZone) {
                this.player.sendPacket(new ExShowScreenMessage("You can't use AutoFarm in this zone.", 5000));
                this.player.sendPacket(new CreatureSay(0, 2, this.player.getName(), ": You can't use AutoFarm in this zone."));
                return;
            }
            if (this.player.isInsideZone(ZoneId.PARTYFARMZONE) && !Config.AUTOFARM_L2PartyFarmZone) {
                this.player.sendPacket(new ExShowScreenMessage("You can't use AutoFarm in this zone.", 5000));
                this.player.sendPacket(new CreatureSay(0, 2, this.player.getName(), ": You can't use AutoFarm in this zone."));
                return;
            }
            if (this.player.isInsideZone(ZoneId.PEACE) && !Config.AUTOFARM_PeaceZone) {
                this.player.sendPacket(new ExShowScreenMessage("You can't use AutoFarm in this zone.", 5000));
                this.player.sendPacket(new CreatureSay(0, 2, this.player.getName(), ": You can't use AutoFarm in this zone."));
                return;
            }
        }
        if (this._task == null) {
            this._task = ThreadPool.scheduleAtFixedRate(() -> executeRoutine(), 450L, 450L);
            this.player.sendPacket(new ExShowScreenMessage("Auto Farming Actived...", 5000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            this.player.sendPacket(new SystemMessage(SystemMessageId.AUTO_FARM_ACTIVATED));
        }
    }

    public void stop() {
        if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
            this.player.sendPacket(new ExShowScreenMessage("Auto Farming Deactivated...", 5000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            this.player.sendPacket(new SystemMessage(SystemMessageId.AUTO_FARM_DESACTIVATED));
        }
    }

    public void executeRoutine() {
        if (this.player.isNoBuffProtected() && (this.player.getAllEffects()).length <= 8) {
            this.player.sendMessage("You don't have buffs to use autofarm.");
            this.player.broadcastUserInfo();
            stop();
            this.player.setAutoFarm(false);
            VoicedAutoFarm.showAutoFarm(this.player);
            return;
        }
        calculatePotions();
        checkSpoil();
        targetEligibleCreature();
        if (this.player.isMageClass()) {
            useAppropriateSpell();
        } else if (shotcutsContainAttack()) {
            attack();
        } else {
            useAppropriateSpell();
        }
        checkSpoil();
        useAppropriateSpell();
    }

    private void attack() {
        Boolean shortcutsContainAttack = Boolean.valueOf(shotcutsContainAttack());
        if (shortcutsContainAttack)
            physicalAttack();
    }

    private void useAppropriateSpell() {
        L2Skill chanceSkill = nextAvailableSkill(getChanceSpells(), AutofarmSpellType.Chance);
        if (chanceSkill != null) {
            useMagicSkill(chanceSkill, Boolean.valueOf(false));
            return;
        }
        L2Skill lowLifeSkill = nextAvailableSkill(getLowLifeSpells(), AutofarmSpellType.LowLife);
        if (lowLifeSkill != null) {
            useMagicSkill(lowLifeSkill, Boolean.valueOf(true));
            return;
        }
        L2Skill attackSkill = nextAvailableSkill(getAttackSpells(), AutofarmSpellType.Attack);
        if (attackSkill != null) {
            useMagicSkill(attackSkill, Boolean.valueOf(false));
        }
    }

    public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType) {
        for (Integer skillId : skillIds) {
            L2Skill skill = this.player.getSkill(skillId);
            if (skill == null)
                continue;
            if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
                continue;
            if (!this.player.checkDoCastConditions(skill))
                continue;
            if (isSpoil(skillId)) {
                if (monsterIsAlreadySpoiled())
                    continue;
                return skill;
            }
            if (spellType == AutofarmSpellType.Chance && getMonsterTarget() != null) {
                if (getMonsterTarget().getFirstEffect(skillId) == null)
                    return skill;
                continue;
            }
            if (spellType == AutofarmSpellType.LowLife && getHpPercentage() > this.player.getHealPercent())
                break;
            return skill;
        }
        return null;
    }

    private void checkSpoil() {
        if (canBeSweepedByMe() && getMonsterTarget().isDead()) {
            L2Skill sweeper = this.player.getSkill(42);
            if (sweeper == null)
                return;
            useMagicSkill(sweeper, Boolean.valueOf(false));
        }
    }

    private Double getHpPercentage() {
        return Double.valueOf(this.player.getCurrentHp() * 100.0D / this.player.getMaxHp());
    }

    private Double percentageMpIsLessThan() {
        return Double.valueOf(this.player.getCurrentMp() * 100.0D / this.player.getMaxMp());
    }

    private Double percentageHpIsLessThan() {
        return Double.valueOf(this.player.getCurrentHp() * 100.0D / this.player.getMaxHp());
    }

    private List<Integer> getAttackSpells() {
        return getSpellsInSlots(AutofarmConstants.attackSlots);
    }

    private List<Integer> getSpellsInSlots(List<Integer> attackSlots) {
        return Arrays.stream(this.player.getShortcutList().getShortcuts()).filter(shortcut -> (shortcut.getPage() == this.player.getPage() && shortcut.getType() == ShortcutType.SKILL && attackSlots.contains(Integer.valueOf(shortcut.getSlot())))).map(Shortcut::getId).collect(Collectors.toList());
    }

    private List<Integer> getChanceSpells() {
        return getSpellsInSlots(AutofarmConstants.chanceSlots);
    }

    private List<Integer> getLowLifeSpells() {
        return getSpellsInSlots(AutofarmConstants.lowLifeSlots);
    }

    private boolean shotcutsContainAttack() {
        return Arrays.stream(this.player.getShortcutList().getShortcuts()).anyMatch(shortcut -> (shortcut.getPage() == this.player.getPage() && shortcut.getType() == ShortcutType.ACTION && (shortcut.getId() == 2 || (this.player.isSummonAttack() && shortcut.getId() == 22))));
    }

    private boolean monsterIsAlreadySpoiled() {
        return (getMonsterTarget() != null && getMonsterTarget().getSpoilerId() != 0);
    }

    private boolean canBeSweepedByMe() {
        return (getMonsterTarget() != null && getMonsterTarget().isDead() && getMonsterTarget().getSpoilerId() == this.player.getObjectId());
    }

    private void castSpellWithAppropriateTarget(L2Skill skill, Boolean forceOnSelf) {
        if (forceOnSelf) {
            WorldObject oldTarget = this.player.getTarget();
            this.player.setTarget(this.player);
            this.player.useMagic(skill, false, false);
            this.player.setTarget(oldTarget);
            return;
        }
        this.player.useMagic(skill, false, false);
    }

    private void physicalAttack() {
        if (!(this.player.getTarget() instanceof Monster target))
            return;
        if (!this.player.isMageClass()) {
            if (target.isAutoAttackable(this.player) && GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                if (GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                    this.player.getAI().setIntention(IntentionType.ATTACK, target);
                    this.player.onActionRequest();
                    if (this.player.isSummonAttack() && this.player.getSummon() != null) {
                        if ((this.player.getSummon().getNpcId() >= 14702 && this.player.getSummon().getNpcId() <= 14798) || (this.player.getSummon().getNpcId() >= 14839 && this.player.getSummon().getNpcId() <= 14869))
                            return;
                        Summon activeSummon = this.player.getSummon();
                        activeSummon.setTarget(target);
                        activeSummon.getAI().setIntention(IntentionType.ATTACK, target);
                        int[] summonAttackSkills = {
                                4261, 4068, 4137, 4260, 4708, 4709, 4710, 4712, 5135, 5138,
                                5141, 5442, 5444, 6095, 6096, 6041, 6044};
                        if (Rnd.get(100) < this.player.getSummonSkillPercent())
                            for (int skillId : summonAttackSkills)
                                useMagicSkillBySummon(skillId, target);
                    }
                }
            } else if (target.isAutoAttackable(this.player) && GeoEngine.getInstance().canSeeTarget(this.player, target) &&
                    GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                this.player.getAI().setIntention(IntentionType.FOLLOW, target);
            }
        } else if (this.player.isSummonAttack() && this.player.getSummon() != null) {
            if ((this.player.getSummon().getNpcId() >= 14702 && this.player.getSummon().getNpcId() <= 14798) || (this.player.getSummon().getNpcId() >= 14839 && this.player.getSummon().getNpcId() <= 14869))
                return;
            Summon activeSummon = this.player.getSummon();
            activeSummon.setTarget(target);
            activeSummon.getAI().setIntention(IntentionType.ATTACK, target);
            int[] summonAttackSkills = {
                    4261, 4068, 4137, 4260, 4708, 4709, 4710, 4712, 5135, 5138,
                    5141, 5442, 5444, 6095, 6096, 6041, 6044};
            if (Rnd.get(100) < this.player.getSummonSkillPercent())
                for (int skillId : summonAttackSkills)
                    useMagicSkillBySummon(skillId, target);
        }
    }

    public void targetEligibleCreature() {
        if (this.player.getTarget() == null) {
            selectNewTarget();
            return;
        }
        if (this.committedTarget != null) {
            if (!this.committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(this.player, this.committedTarget)) {
                attack();
                return;
            }
            if (!GeoEngine.getInstance().canSeeTarget(this.player, this.committedTarget)) {
                this.committedTarget = null;
                selectNewTarget();
                return;
            }
            this.player.getAI().setIntention(IntentionType.FOLLOW, this.committedTarget);
            this.committedTarget = null;
            this.player.setTarget(null);
        }
        if (this.committedTarget instanceof Summon)
            return;
        List<Monster> targets = getKnownMonstersInRadius(this.player, this.player.getRadius(), creature -> Boolean.valueOf((GeoEngine.getInstance().canMoveToTarget(this.player.getX(), this.player.getY(), this.player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !this.player.ignoredMonsterContain(creature.getNpcId()) && !creature.isMinion() && !creature.isRaidBoss() && !creature.isDead() && !(creature instanceof net.sf.l2j.gameserver.model.actor.instance.Chest) && (!this.player.isAntiKsProtected() || creature.getTarget() == null || creature.getTarget() == this.player || creature.getTarget() == this.player.getSummon()))));
        if (targets.isEmpty())
            return;
        Monster closestTarget = targets.stream().min((o1, o2) -> Integer.compare((int) Math.sqrt(this.player.getDistanceSq(o1)), (int) Math.sqrt(this.player.getDistanceSq(o2)))).get();
        this.committedTarget = closestTarget;
        this.player.setTarget(closestTarget);
    }

    private void selectNewTarget() {
        List<Monster> targets = getKnownMonstersInRadius(this.player, this.player.getRadius(), creature -> Boolean.valueOf((GeoEngine.getInstance().canMoveToTarget(this.player.getX(), this.player.getY(), this.player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !this.player.ignoredMonsterContain(creature.getNpcId()) && !creature.isMinion() && !creature.isRaidBoss() && !creature.isDead() && !(creature instanceof net.sf.l2j.gameserver.model.actor.instance.Chest) && (!this.player.isAntiKsProtected() || creature.getTarget() == null || creature.getTarget() == this.player || creature.getTarget() == this.player.getSummon()))));
        if (targets.isEmpty())
            return;
        Monster closestTarget = targets.stream().min((o1, o2) -> Integer.compare((int) Math.sqrt(this.player.getDistanceSq(o1)), (int) Math.sqrt(this.player.getDistanceSq(o2)))).get();
        this.committedTarget = closestTarget;
        this.player.setTarget(closestTarget);
    }

    public Monster getMonsterTarget() {
        if (!(this.player.getTarget() instanceof Monster))
            return null;
        return (Monster) this.player.getTarget();
    }

    private void useMagicSkill(L2Skill skill, Boolean forceOnSelf) {
        if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && this.player.getKarma() > 0) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (skill.isToggle() && this.player.isMounted()) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (this.player.isOutOfControl()) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (this.player.isAttackingNow()) {
            this.player.getAI().setNextAction(new NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, () -> castSpellWithAppropriateTarget(skill, forceOnSelf)));
        } else {
            castSpellWithAppropriateTarget(skill, forceOnSelf);
        }
    }

    private boolean useMagicSkillBySummon(int skillId, WorldObject target) {
        if (this.player == null || this.player.isInStoreMode())
            return false;
        Summon activeSummon = this.player.getSummon();
        if (activeSummon == null)
            return false;
        if (activeSummon instanceof net.sf.l2j.gameserver.model.actor.instance.Pet && activeSummon.getLevel() - this.player.getLevel() > 20) {
            this.player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
            return false;
        }
        if (activeSummon.isOutOfControl()) {
            this.player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
            return false;
        }
        L2Skill skill = activeSummon.getSkill(skillId);
        if (skill == null)
            return false;
        if (skill.isOffensive() && this.player == target)
            return false;
        activeSummon.setTarget(target);
        return activeSummon.useMagic(skill, false, false);
    }

    private void calculatePotions() {
        if (percentageHpIsLessThan() < this.player.getHpPotionPercentage())
            forceUseItem(1539);
        if (percentageMpIsLessThan() < this.player.getMpPotionPercentage())
            forceUseItem(728);
    }

    private void forceUseItem(int itemId) {
        ItemInstance potion = this.player.getInventory().getItemByItemId(itemId);
        if (potion == null)
            return;
        IItemHandler handler = ItemHandler.getInstance().getHandler(potion.getEtcItem());
        if (handler != null)
            handler.useItem(this.player, potion, false);
    }
}
