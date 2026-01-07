/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDrain;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class Cubic {
    public static final int STORM_CUBIC = 1;
    public static final int VAMPIRIC_CUBIC = 2;
    public static final int LIFE_CUBIC = 3;
    public static final int VIPER_CUBIC = 4;
    public static final int POLTERGEIST_CUBIC = 5;
    public static final int BINDING_CUBIC = 6;
    public static final int AQUA_CUBIC = 7;
    public static final int SPARK_CUBIC = 8;
    public static final int ATTRACT_CUBIC = 9;
    public static final int MAX_MAGIC_RANGE = 900;
    public static final int SKILL_CUBIC_HEAL = 4051;
    public static final int SKILL_CUBIC_CURE = 5579;
    private final boolean _givenByOther;
    protected Player _owner;
    protected Creature _target;
    protected int _id;
    protected int _matk;
    protected int _activationtime;
    protected int _activationchance;
    protected boolean _active;
    protected List<L2Skill> _skills = new ArrayList();
    private Future<?> _disappearTask;
    private Future<?> _actionTask;

    public Cubic(Player owner, int id, int level, int mAtk, int activationtime, int activationchance, int totallifetime, boolean givenByOther) {
        this._owner = owner;
        this._id = id;
        this._matk = mAtk;
        this._activationtime = activationtime * 1000;
        this._activationchance = activationchance;
        this._active = false;
        this._givenByOther = givenByOther;
        switch (this._id) {
            case 1:
                this._skills.add(SkillTable.getInstance().getInfo(4049, level));
                break;
            case 2:
                this._skills.add(SkillTable.getInstance().getInfo(4050, level));
                break;
            case 3:
                this._skills.add(SkillTable.getInstance().getInfo(4051, level));
                this.doAction();
                break;
            case 4:
                this._skills.add(SkillTable.getInstance().getInfo(4052, level));
                break;
            case 5:
                this._skills.add(SkillTable.getInstance().getInfo(4053, level));
                this._skills.add(SkillTable.getInstance().getInfo(4054, level));
                this._skills.add(SkillTable.getInstance().getInfo(4055, level));
                break;
            case 6:
                this._skills.add(SkillTable.getInstance().getInfo(4164, level));
                break;
            case 7:
                this._skills.add(SkillTable.getInstance().getInfo(4165, level));
                break;
            case 8:
                this._skills.add(SkillTable.getInstance().getInfo(4166, level));
                break;
            case 9:
                this._skills.add(SkillTable.getInstance().getInfo(5115, level));
                this._skills.add(SkillTable.getInstance().getInfo(5116, level));
        }

        this._disappearTask = ThreadPool.schedule(new Cubic.Disappear(), totallifetime);
    }

    public synchronized void doAction() {
        if (!this._active) {
            this._active = true;
            switch (this._id) {
                case 1:
                case 2:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    this._actionTask = ThreadPool.scheduleAtFixedRate(new Cubic.Action(this._activationchance), 0L, this._activationtime);
                    break;
                case 3:
                    this._actionTask = ThreadPool.scheduleAtFixedRate(new Cubic.Heal(), 0L, this._activationtime);
            }

        }
    }

    public int getId() {
        return this._id;
    }

    public Player getOwner() {
        return this._owner;
    }

    public final int getMCriticalHit(Creature target, L2Skill skill) {
        return this._owner.getMCriticalHit(target, skill);
    }

    public int getMAtk() {
        return this._matk;
    }

    public void stopAction() {
        this._target = null;
        if (this._actionTask != null) {
            this._actionTask.cancel(true);
            this._actionTask = null;
        }

        this._active = false;
    }

    public void cancelDisappear() {
        if (this._disappearTask != null) {
            this._disappearTask.cancel(true);
            this._disappearTask = null;
        }

    }

    public void getCubicTarget() {
        this._target = null;
        WorldObject ownerTarget = this._owner.getTarget();
        if (ownerTarget != null) {
            Player enemy;
            Party ownerParty;
            if (this._owner.isInDuel()) {
                enemy = DuelManager.getInstance().getDuel(this._owner.getDuelId()).getPlayerA();
                Player PlayerB = DuelManager.getInstance().getDuel(this._owner.getDuelId()).getPlayerB();
                if (DuelManager.getInstance().getDuel(this._owner.getDuelId()).isPartyDuel()) {
                    ownerParty = enemy.getParty();
                    Party partyB = PlayerB.getParty();
                    Party partyEnemy = null;
                    if (ownerParty != null) {
                        if (ownerParty.containsPlayer(this._owner)) {
                            if (partyB != null) {
                                partyEnemy = partyB;
                            } else {
                                this._target = PlayerB;
                            }
                        } else {
                            partyEnemy = ownerParty;
                        }
                    } else if (enemy == this._owner) {
                        if (partyB != null) {
                            partyEnemy = partyB;
                        } else {
                            this._target = PlayerB;
                        }
                    } else {
                        this._target = enemy;
                    }

                    if ((this._target == enemy || this._target == PlayerB) && this._target == ownerTarget) {
                        return;
                    }

                    if (partyEnemy != null) {
                        if (partyEnemy.containsPlayer(ownerTarget)) {
                            this._target = (Creature) ownerTarget;
                        }

                        return;
                    }
                }

                if (enemy != this._owner && ownerTarget == enemy) {
                    this._target = enemy;
                } else if (PlayerB != this._owner && ownerTarget == PlayerB) {
                    this._target = PlayerB;
                } else {
                    this._target = null;
                }
            } else if (this._owner.isInOlympiadMode()) {
                if (this._owner.isOlympiadStart() && ownerTarget instanceof Playable) {
                    enemy = ownerTarget.getActingPlayer();
                    if (enemy != null && enemy.getOlympiadGameId() == this._owner.getOlympiadGameId() && enemy.getOlympiadSide() != this._owner.getOlympiadSide()) {
                        this._target = (Creature) ownerTarget;
                    }
                }

            } else {
                if (ownerTarget instanceof Creature && ownerTarget != this._owner.getSummon() && ownerTarget != this._owner) {
                    if (ownerTarget instanceof Attackable && !((Attackable) ownerTarget).isDead()) {
                        if (((Attackable) ownerTarget).getAggroList().get(this._owner) != null) {
                            this._target = (Creature) ownerTarget;
                            return;
                        }

                        if (this._owner.getSummon() != null && ((Attackable) ownerTarget).getAggroList().get(this._owner.getSummon()) != null) {
                            this._target = (Creature) ownerTarget;
                            return;
                        }
                    }

                    enemy = null;
                    if (this._owner.getPvpFlag() > 0 && !this._owner.isInsideZone(ZoneId.PEACE) || this._owner.isInsideZone(ZoneId.PVP)) {
                        if (!((Creature) ownerTarget).isDead()) {
                            enemy = ownerTarget.getActingPlayer();
                        }

                        if (enemy != null) {
                            boolean targetIt = true;
                            ownerParty = this._owner.getParty();
                            if (ownerParty != null) {
                                if (ownerParty.containsPlayer(enemy)) {
                                    targetIt = false;
                                } else if (ownerParty.getCommandChannel() != null && ownerParty.getCommandChannel().containsPlayer(enemy)) {
                                    targetIt = false;
                                }
                            }

                            if (this._owner.getClan() != null && !this._owner.isInsideZone(ZoneId.PVP)) {
                                if (this._owner.getClan().isMember(enemy.getObjectId())) {
                                    targetIt = false;
                                }

                                if (this._owner.getAllyId() > 0 && enemy.getAllyId() > 0 && this._owner.getAllyId() == enemy.getAllyId()) {
                                    targetIt = false;
                                }
                            }

                            if (enemy.getPvpFlag() == 0 && !enemy.isInsideZone(ZoneId.PVP)) {
                                targetIt = false;
                            }

                            if (enemy.isInsideZone(ZoneId.PEACE)) {
                                targetIt = false;
                            }

                            if (this._owner.getSiegeState() > 0 && this._owner.getSiegeState() == enemy.getSiegeState()) {
                                targetIt = false;
                            }

                            if (!enemy.isVisible()) {
                                targetIt = false;
                            }

                            if (targetIt) {
                                this._target = enemy;
                            }
                        }
                    }
                }

            }
        }
    }

    public void useCubicContinuous(Cubic activeCubic, L2Skill skill, WorldObject[] targets) {
        WorldObject[] var4 = targets;
        int var5 = targets.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            WorldObject obj = var4[var6];
            if (obj instanceof Creature target) {
                if (!target.isDead()) {
                    if (skill.isOffensive()) {
                        byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
                        boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
                        boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss);
                        if (!acted) {
                            activeCubic.getOwner().sendPacket(SystemMessageId.ATTACK_FAILED);
                            continue;
                        }
                    }

                    if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId()) {
                        Iterator var13 = skill.getEffects(activeCubic.getOwner(), target).iterator();

                        while (var13.hasNext()) {
                            L2Effect debuff = (L2Effect) var13.next();
                            if (debuff != null) {
                                DuelManager.getInstance().onBuff((Player) target, debuff);
                            }
                        }
                    } else {
                        skill.getEffects(activeCubic, target, null);
                    }
                }
            }
        }

    }

    public void useCubicMdam(Cubic activeCubic, L2Skill skill, WorldObject[] targets) {
        WorldObject[] var4 = targets;
        int var5 = targets.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            WorldObject obj = var4[var6];
            if (obj instanceof Creature target) {
                if (target.isAlikeDead()) {
                    if (!(target instanceof Player)) {
                        continue;
                    }

                    target.stopFakeDeath(true);
                }

                boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
                byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
                boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
                int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);
                if ((Formulas.calcSkillReflect(target, skill) & 1) > 0) {
                    damage = 0;
                }

                if (damage > 0) {
                    Formulas.calcCastBreak(target, damage);
                    activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
                    if (skill.hasEffects()) {
                        target.stopSkillEffects(skill.getId());
                        if (target.getFirstEffect(skill) != null) {
                            target.removeEffect(target.getFirstEffect(skill));
                        }

                        if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss)) {
                            skill.getEffects(activeCubic, target, null);
                        }
                    }

                    target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
                }
            }
        }

    }

    public void useCubicDisabler(L2SkillType type, Cubic activeCubic, L2Skill skill, WorldObject[] targets) {
        WorldObject[] var5 = targets;
        int var6 = targets.length;

        label89:
        for (int var7 = 0; var7 < var6; ++var7) {
            WorldObject obj = var5[var7];
            if (obj instanceof Creature target) {
                if (!target.isDead()) {
                    byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
                    boolean bss = activeCubic.getOwner().isChargedShot(ShotType.BLESSED_SPIRITSHOT);
                    switch (type) {
                        case STUN:
                        case PARALYZE:
                        case ROOT:
                            if (!Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss)) {
                                break;
                            }

                            if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == L2SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId()) {
                                Iterator var18 = skill.getEffects(activeCubic.getOwner(), target).iterator();

                                while (true) {
                                    if (!var18.hasNext()) {
                                        continue label89;
                                    }

                                    L2Effect debuff = (L2Effect) var18.next();
                                    if (debuff != null) {
                                        DuelManager.getInstance().onBuff((Player) target, debuff);
                                    }
                                }
                            }

                            skill.getEffects(activeCubic, target, null);
                            break;
                        case CANCEL_DEBUFF:
                            L2Effect[] effects = target.getAllEffects();
                            if (effects == null || effects.length == 0) {
                                break;
                            }

                            int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
                            L2Effect[] var14 = effects;
                            int var15 = effects.length;
                            int var16 = 0;

                            while (true) {
                                if (var16 >= var15) {
                                    continue label89;
                                }

                                L2Effect e = var14[var16];
                                if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects() && e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082) {
                                    e.exit();
                                    if (count > -1) {
                                        ++count;
                                    }
                                }

                                ++var16;
                            }
                        case AGGDAMAGE:
                            if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld, bss)) {
                                if (target instanceof Attackable) {
                                    target.getAI().notifyEvent(AiEventType.AGGRESSION, activeCubic.getOwner(), (int) (150.0D * skill.getPower() / (double) (target.getLevel() + 7)));
                                }

                                skill.getEffects(activeCubic, target, null);
                            }
                    }
                }
            }
        }

    }

    public boolean isInCubicRange(Creature owner, Creature target) {
        if (owner != null && target != null) {
            int range = 900;
            int x = owner.getX() - target.getX();
            int y = owner.getY() - target.getY();
            int z = owner.getZ() - target.getZ();
            return x * x + y * y + z * z <= range * range;
        } else {
            return false;
        }
    }

    public void cubicTargetForHeal() {
        Creature target = null;
        double percentleft = 100.0D;
        Party party = this._owner.getParty();
        if (this._owner.isInDuel() && !DuelManager.getInstance().getDuel(this._owner.getDuelId()).isPartyDuel()) {
            party = null;
        }

        if (party != null && !this._owner.isInOlympiadMode()) {
            Iterator var5 = party.getMembers().iterator();

            while (var5.hasNext()) {
                Creature partyMember = (Creature) var5.next();
                if (!partyMember.isDead() && this.isInCubicRange(this._owner, partyMember) && partyMember.getCurrentHp() < (double) partyMember.getMaxHp() && percentleft > partyMember.getCurrentHp() / (double) partyMember.getMaxHp()) {
                    percentleft = partyMember.getCurrentHp() / (double) partyMember.getMaxHp();
                    target = partyMember;
                }

                Summon summon = partyMember.getSummon();
                if (summon != null && !summon.isDead() && this.isInCubicRange(this._owner, summon) && summon.getCurrentHp() < (double) summon.getMaxHp() && percentleft > summon.getCurrentHp() / (double) summon.getMaxHp()) {
                    percentleft = summon.getCurrentHp() / (double) summon.getMaxHp();
                    target = summon;
                }
            }
        } else {
            if (this._owner.getCurrentHp() < (double) this._owner.getMaxHp()) {
                percentleft = this._owner.getCurrentHp() / (double) this._owner.getMaxHp();
                target = this._owner;
            }

            if (this._owner.getSummon() != null && !this._owner.getSummon().isDead() && this._owner.getSummon().getCurrentHp() < (double) this._owner.getSummon().getMaxHp() && percentleft > this._owner.getSummon().getCurrentHp() / (double) this._owner.getSummon().getMaxHp() && this.isInCubicRange(this._owner, this._owner.getSummon())) {
                target = this._owner.getSummon();
            }
        }

        this._target = target;
    }

    public boolean givenByOther() {
        return this._givenByOther;
    }

    private class Disappear implements Runnable {
        Disappear() {
        }

        public void run() {
            Cubic.this.stopAction();
            Cubic.this._owner.delCubic(Cubic.this._id);
            Cubic.this._owner.broadcastUserInfo();
        }
    }

    private class Action implements Runnable {
        private final int _chance;

        Action(int chance) {
            this._chance = chance;
        }

        public void run() {
            if (!Cubic.this._owner.isDead() && Cubic.this._owner.isOnline()) {
                if (!AttackStanceTaskManager.getInstance().isInAttackStance(Cubic.this._owner)) {
                    Cubic.this.stopAction();
                } else {
                    if (Rnd.get(1, 100) < this._chance) {
                        L2Skill skill = Rnd.get(Cubic.this._skills);
                        if (skill != null) {
                            if (skill.getId() == 4051) {
                                Cubic.this.cubicTargetForHeal();
                            } else {
                                Cubic.this.getCubicTarget();
                                if (!Cubic.this.isInCubicRange(Cubic.this._owner, Cubic.this._target)) {
                                    Cubic.this._target = null;
                                }
                            }

                            Creature target = Cubic.this._target;
                            if (target != null && !target.isDead()) {
                                Cubic.this._owner.broadcastPacket(new MagicSkillUse(Cubic.this._owner, target, skill.getId(), skill.getLevel(), 0, 0));
                                L2SkillType type = skill.getSkillType();
                                ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
                                Creature[] targets = new Creature[]{target};
                                if (type != L2SkillType.PARALYZE && type != L2SkillType.STUN && type != L2SkillType.ROOT && type != L2SkillType.AGGDAMAGE) {
                                    if (type == L2SkillType.MDAM) {
                                        Cubic.this.useCubicMdam(Cubic.this, skill, targets);
                                    } else if (type != L2SkillType.POISON && type != L2SkillType.DEBUFF && type != L2SkillType.DOT) {
                                        if (type == L2SkillType.DRAIN) {
                                            ((L2SkillDrain) skill).useCubicSkill(Cubic.this, targets);
                                        } else {
                                            handler.useSkill(Cubic.this._owner, skill, targets);
                                        }
                                    } else {
                                        Cubic.this.useCubicContinuous(Cubic.this, skill, targets);
                                    }
                                } else {
                                    Cubic.this.useCubicDisabler(type, Cubic.this, skill, targets);
                                }
                            }
                        }
                    }

                }
            } else {
                Cubic.this.stopAction();
                Cubic.this._owner.delCubic(Cubic.this._id);
                Cubic.this._owner.broadcastUserInfo();
                Cubic.this.cancelDisappear();
            }
        }
    }

    private class Heal implements Runnable {
        Heal() {
        }

        public void run() {
            if (!Cubic.this._owner.isDead() && Cubic.this._owner.isOnline()) {
                L2Skill skill = null;
                Iterator var2 = Cubic.this._skills.iterator();

                while (var2.hasNext()) {
                    L2Skill sk = (L2Skill) var2.next();
                    if (sk.getId() == 4051) {
                        skill = sk;
                        break;
                    }
                }

                if (skill != null) {
                    Cubic.this.cubicTargetForHeal();
                    Creature target = Cubic.this._target;
                    if (target != null && !target.isDead() && (double) target.getMaxHp() - target.getCurrentHp() > skill.getPower()) {
                        Creature[] targets = new Creature[]{target};
                        ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
                        if (handler != null) {
                            handler.useSkill(Cubic.this._owner, skill, targets);
                        } else {
                            skill.useSkill(Cubic.this._owner, targets);
                        }

                        MagicSkillUse msu = new MagicSkillUse(Cubic.this._owner, target, skill.getId(), skill.getLevel(), 0, 0);
                        Cubic.this._owner.broadcastPacket(msu);
                    }
                }

            } else {
                Cubic.this.stopAction();
                Cubic.this._owner.delCubic(Cubic.this._id);
                Cubic.this._owner.broadcastUserInfo();
                Cubic.this.cancelDisappear();
            }
        }
    }
}