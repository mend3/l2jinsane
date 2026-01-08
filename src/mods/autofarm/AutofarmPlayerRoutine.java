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
import net.sf.l2j.gameserver.model.actor.instance.Chest;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.*;
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
        return skillId == 254 || skillId == 302;
    }

    public static List<Monster> getKnownMonstersInRadius(Player player, int radius, Function<Monster, Boolean> condition) {
        WorldRegion region = player.getRegion();
        if (region == null) {
            return Collections.emptyList();
        } else {
            List<Monster> result = new ArrayList<>();

            for (WorldRegion reg : region.getSurroundingRegions()) {
                for (WorldObject obj : reg.getObjects()) {
                    if (obj instanceof Monster && MathUtil.checkIfInRange(radius, player, obj, true) && condition.apply((Monster) obj)) {
                        result.add((Monster) obj);
                    }
                }
            }

            return result;
        }
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
            this._task = ThreadPool.scheduleAtFixedRate(this::executeRoutine, 450L, 450L);
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
        if (this.player.isNoBuffProtected() && this.player.getAllEffects().length <= 8) {
            this.player.sendMessage("You don't have buffs to use autofarm.");
            this.player.broadcastUserInfo();
            this.stop();
            this.player.setAutoFarm(false);
            VoicedAutoFarm.showAutoFarm(this.player);
        } else {
            this.calculatePotions();
            this.checkSpoil();
            this.targetEligibleCreature();
            if (this.player.isMageClass()) {
                this.useAppropriateSpell();
            } else if (this.shotcutsContainAttack()) {
                this.attack();
            } else {
                this.useAppropriateSpell();
            }

            this.checkSpoil();
            this.useAppropriateSpell();
        }
    }

    private void attack() {
        boolean shortcutsContainAttack = this.shotcutsContainAttack();
        if (shortcutsContainAttack) {
            this.physicalAttack();
        }

    }

    private void useAppropriateSpell() {
        L2Skill chanceSkill = this.nextAvailableSkill(this.getChanceSpells(), AutofarmSpellType.Chance);
        if (chanceSkill != null) {
            this.useMagicSkill(chanceSkill, false);
        } else {
            L2Skill lowLifeSkill = this.nextAvailableSkill(this.getLowLifeSpells(), AutofarmSpellType.LowLife);
            if (lowLifeSkill != null) {
                this.useMagicSkill(lowLifeSkill, true);
            } else {
                L2Skill attackSkill = this.nextAvailableSkill(this.getAttackSpells(), AutofarmSpellType.Attack);
                if (attackSkill != null) {
                    this.useMagicSkill(attackSkill, false);
                }
            }
        }
    }

    public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType) {
        Iterator<Integer> var3 = skillIds.iterator();

        while (true) {
            if (var3.hasNext()) {
                Integer skillId = var3.next();
                L2Skill skill = this.player.getSkill(skillId);
                if (skill == null || skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME || !this.player.checkDoCastConditions(skill)) {
                    continue;
                }

                if (isSpoil(skillId)) {
                    if (!this.monsterIsAlreadySpoiled()) {
                        return skill;
                    }
                    continue;
                }

                if (spellType == AutofarmSpellType.Chance && this.getMonsterTarget() != null) {
                    if (this.getMonsterTarget().getFirstEffect(skillId) == null) {
                        return skill;
                    }
                    continue;
                }

                if (spellType != AutofarmSpellType.LowLife || !(this.getHpPercentage() > (double) this.player.getHealPercent())) {
                    return skill;
                }
            }

            return null;
        }
    }

    private void checkSpoil() {
        if (this.canBeSweepedByMe() && this.getMonsterTarget().isDead()) {
            L2Skill sweeper = this.player.getSkill(42);
            if (sweeper == null) {
                return;
            }

            this.useMagicSkill(sweeper, false);
        }

    }

    private Double getHpPercentage() {
        return this.player.getCurrentHp() * (double) 100.0F / (double) this.player.getMaxHp();
    }

    private Double percentageMpIsLessThan() {
        return this.player.getCurrentMp() * (double) 100.0F / (double) this.player.getMaxMp();
    }

    private Double percentageHpIsLessThan() {
        return this.player.getCurrentHp() * (double) 100.0F / (double) this.player.getMaxHp();
    }

    private List<Integer> getAttackSpells() {
        return this.getSpellsInSlots(AutofarmConstants.attackSlots);
    }

    private List<Integer> getSpellsInSlots(List<Integer> attackSlots) {
        return Arrays.stream(this.player.getShortcutList().getShortcuts()).filter((shortcut) -> shortcut.getPage() == this.player.getPage() && shortcut.getType() == ShortcutType.SKILL && attackSlots.contains(shortcut.getSlot())).map(Shortcut::getId).collect(Collectors.toList());
    }

    private List<Integer> getChanceSpells() {
        return this.getSpellsInSlots(AutofarmConstants.chanceSlots);
    }

    private List<Integer> getLowLifeSpells() {
        return this.getSpellsInSlots(AutofarmConstants.lowLifeSlots);
    }

    private boolean shotcutsContainAttack() {
        return Arrays.stream(this.player.getShortcutList().getShortcuts()).anyMatch((shortcut) -> shortcut.getPage() == this.player.getPage() && shortcut.getType() == ShortcutType.ACTION && (shortcut.getId() == 2 || this.player.isSummonAttack() && shortcut.getId() == 22));
    }

    private boolean monsterIsAlreadySpoiled() {
        return this.getMonsterTarget() != null && this.getMonsterTarget().getSpoilerId() != 0;
    }

    private boolean canBeSweepedByMe() {
        return this.getMonsterTarget() != null && this.getMonsterTarget().isDead() && this.getMonsterTarget().getSpoilerId() == this.player.getObjectId();
    }

    private void castSpellWithAppropriateTarget(L2Skill skill, Boolean forceOnSelf) {
        if (forceOnSelf) {
            WorldObject oldTarget = this.player.getTarget();
            this.player.setTarget(this.player);
            this.player.useMagic(skill, false, false);
            this.player.setTarget(oldTarget);
        } else {
            this.player.useMagic(skill, false, false);
        }
    }

    private void physicalAttack() {
        if (this.player.getTarget() instanceof Monster target) {
            if (!this.player.isMageClass()) {
                if (target.isAutoAttackable(this.player) && GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                    if (GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                        this.player.getAI().setIntention(IntentionType.ATTACK, target);
                        this.player.onActionRequest();
                        if (this.player.isSummonAttack() && this.player.getSummon() != null) {
                            if (this.player.getSummon().getNpcId() >= 14702 && this.player.getSummon().getNpcId() <= 14798 || this.player.getSummon().getNpcId() >= 14839 && this.player.getSummon().getNpcId() <= 14869) {
                                return;
                            }

                            Summon activeSummon = this.player.getSummon();
                            activeSummon.setTarget(target);
                            activeSummon.getAI().setIntention(IntentionType.ATTACK, target);
                            int[] summonAttackSkills = new int[]{4261, 4068, 4137, 4260, 4708, 4709, 4710, 4712, 5135, 5138, 5141, 5442, 5444, 6095, 6096, 6041, 6044};
                            if (Rnd.get(100) < this.player.getSummonSkillPercent()) {
                                for (int skillId : summonAttackSkills) {
                                    this.useMagicSkillBySummon(skillId, target);
                                }
                            }
                        }
                    }
                } else if (target.isAutoAttackable(this.player) && GeoEngine.getInstance().canSeeTarget(this.player, target) && GeoEngine.getInstance().canSeeTarget(this.player, target)) {
                    this.player.getAI().setIntention(IntentionType.FOLLOW, target);
                }
            } else if (this.player.isSummonAttack() && this.player.getSummon() != null) {
                if (this.player.getSummon().getNpcId() >= 14702 && this.player.getSummon().getNpcId() <= 14798 || this.player.getSummon().getNpcId() >= 14839 && this.player.getSummon().getNpcId() <= 14869) {
                    return;
                }

                Summon activeSummon = this.player.getSummon();
                activeSummon.setTarget(target);
                activeSummon.getAI().setIntention(IntentionType.ATTACK, target);
                int[] summonAttackSkills = new int[]{4261, 4068, 4137, 4260, 4708, 4709, 4710, 4712, 5135, 5138, 5141, 5442, 5444, 6095, 6096, 6041, 6044};
                if (Rnd.get(100) < this.player.getSummonSkillPercent()) {
                    for (int skillId : summonAttackSkills) {
                        this.useMagicSkillBySummon(skillId, target);
                    }
                }
            }

        }
    }

    public void targetEligibleCreature() {
        if (this.player.getTarget() == null) {
            this.selectNewTarget();
        } else {
            if (this.committedTarget != null) {
                if (!this.committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(this.player, this.committedTarget)) {
                    this.attack();
                    return;
                }

                if (!GeoEngine.getInstance().canSeeTarget(this.player, this.committedTarget)) {
                    this.committedTarget = null;
                    this.selectNewTarget();
                    return;
                }

                this.player.getAI().setIntention(IntentionType.FOLLOW, this.committedTarget);
                this.committedTarget = null;
                this.player.setTarget(null);
            }

            if (!(this.committedTarget instanceof Summon)) {
                List<Monster> targets = getKnownMonstersInRadius(this.player, this.player.getRadius(), (creature) -> GeoEngine.getInstance().canMoveToTarget(this.player.getX(), this.player.getY(), this.player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !this.player.ignoredMonsterContain(creature.getNpcId()) && !creature.isMinion() && !creature.isRaidBoss() && !creature.isDead() && !(creature instanceof Chest) && (!this.player.isAntiKsProtected() || creature.getTarget() == null || creature.getTarget() == this.player || creature.getTarget() == this.player.getSummon()));
                if (!targets.isEmpty()) {
                    Monster closestTarget = targets.stream().min((o1, o2) -> Integer.compare((int) Math.sqrt(this.player.getDistanceSq(o1)), (int) Math.sqrt(this.player.getDistanceSq(o2)))).get();
                    this.committedTarget = closestTarget;
                    this.player.setTarget(closestTarget);
                }
            }
        }
    }

    private void selectNewTarget() {
        List<Monster> targets = getKnownMonstersInRadius(this.player, this.player.getRadius(), (creature) -> GeoEngine.getInstance().canMoveToTarget(this.player.getX(), this.player.getY(), this.player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !this.player.ignoredMonsterContain(creature.getNpcId()) && !creature.isMinion() && !creature.isRaidBoss() && !creature.isDead() && !(creature instanceof Chest) && (!this.player.isAntiKsProtected() || creature.getTarget() == null || creature.getTarget() == this.player || creature.getTarget() == this.player.getSummon()));
        if (!targets.isEmpty()) {
            Monster closestTarget = targets.stream().min((o1, o2) -> Integer.compare((int) Math.sqrt(this.player.getDistanceSq(o1)), (int) Math.sqrt(this.player.getDistanceSq(o2)))).get();
            this.committedTarget = closestTarget;
            this.player.setTarget(closestTarget);
        }
    }

    public Monster getMonsterTarget() {
        return !(this.player.getTarget() instanceof Monster) ? null : (Monster) this.player.getTarget();
    }

    private void useMagicSkill(L2Skill skill, Boolean forceOnSelf) {
        if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && this.player.getKarma() > 0) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (skill.isToggle() && this.player.isMounted()) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (this.player.isOutOfControl()) {
            this.player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            if (this.player.isAttackingNow()) {
                this.player.getAI().setNextAction(new NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, () -> this.castSpellWithAppropriateTarget(skill, forceOnSelf)));
            } else {
                this.castSpellWithAppropriateTarget(skill, forceOnSelf);
            }

        }
    }

    private void useMagicSkillBySummon(int skillId, WorldObject target) {
        if (this.player != null && !this.player.isInStoreMode()) {
            Summon activeSummon = this.player.getSummon();
            if (activeSummon == null) {
            } else if (activeSummon instanceof Pet && activeSummon.getLevel() - this.player.getLevel() > 20) {
                this.player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
            } else if (activeSummon.isOutOfControl()) {
                this.player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
            } else {
                L2Skill skill = activeSummon.getSkill(skillId);
                if (skill == null) {
                } else if (skill.isOffensive() && this.player == target) {
                } else {
                    activeSummon.setTarget(target);
                    activeSummon.useMagic(skill, false, false);
                }
            }
        } else {
        }
    }

    private void calculatePotions() {
        if (this.percentageHpIsLessThan() < (double) this.player.getHpPotionPercentage()) {
            this.forceUseItem(1539);
        }

        if (this.percentageMpIsLessThan() < (double) this.player.getMpPotionPercentage()) {
            this.forceUseItem(728);
        }

    }

    private void forceUseItem(int itemId) {
        ItemInstance potion = this.player.getInventory().getItemByItemId(itemId);
        if (potion != null) {
            IItemHandler handler = ItemHandler.getInstance().getHandler(potion.getEtcItem());
            if (handler != null) {
                handler.useItem(this.player, potion, false);
            }

        }
    }
}
