package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.skills.effects.EffectChanceSkillTrigger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition> {
    protected static final Logger _log = Logger.getLogger(ChanceSkillList.class.getName());

    private static final long serialVersionUID = 1L;

    private final Creature _owner;

    public ChanceSkillList(Creature owner) {
        this._owner = owner;
    }

    public Creature getOwner() {
        return this._owner;
    }

    public void onHit(Creature target, boolean ownerWasHit, boolean wasCrit) {
        int event;
        if (ownerWasHit) {
            event = 384;
            if (wasCrit)
                event |= 0x200;
        } else {
            event = 1;
            if (wasCrit)
                event |= 0x2;
        }
        onChanceSkillEvent(event, target);
    }

    public void onEvadedHit(Creature attacker) {
        onChanceSkillEvent(8192, attacker);
    }

    public void onSkillHit(Creature target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive) {
        int event;
        if (ownerWasHit) {
            event = 1024;
            if (wasOffensive) {
                event |= 0x800;
                event |= 0x80;
            } else {
                event |= 0x1000;
            }
        } else {
            event = 4;
            event |= wasMagic ? 16 : 8;
            event |= wasOffensive ? 64 : 32;
        }
        onChanceSkillEvent(event, target);
    }

    public void onStart() {
        onChanceSkillEvent(16384, this._owner);
    }

    public void onActionTime() {
        onChanceSkillEvent(32768, this._owner);
    }

    public void onExit() {
        onChanceSkillEvent(65536, this._owner);
    }

    public void onChanceSkillEvent(int event, Creature target) {
        if (this._owner.isDead())
            return;
        for (Map.Entry<IChanceSkillTrigger, ChanceCondition> entry : entrySet()) {
            IChanceSkillTrigger trigger = entry.getKey();
            ChanceCondition cond = entry.getValue();
            if (cond != null && cond.trigger(event)) {
                if (trigger instanceof L2Skill) {
                    makeCast((L2Skill) trigger, target);
                    continue;
                }
                if (trigger instanceof EffectChanceSkillTrigger)
                    makeCast((EffectChanceSkillTrigger) trigger, target);
            }
        }
    }

    private void makeCast(L2Skill skill, Creature target) {
        try {
            if (skill.getWeaponDependancy(this._owner) && skill.checkCondition(this._owner, target, false)) {
                if (skill.triggersChanceSkill()) {
                    skill = SkillTable.getInstance().getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());
                    if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
                        return;
                }
                if (this._owner.isSkillDisabled(skill))
                    return;
                if (skill.getReuseDelay() > 0)
                    this._owner.disableSkill(skill, skill.getReuseDelay());
                WorldObject[] targets = skill.getTargetList(this._owner, false, target);
                if (targets.length == 0)
                    return;
                Creature firstTarget = (Creature) targets[0];
                this._owner.broadcastPacket(new MagicSkillLaunched(this._owner, skill.getId(), skill.getLevel(), targets));
                this._owner.broadcastPacket(new MagicSkillUse(this._owner, firstTarget, skill.getId(), skill.getLevel(), 0, 0));
                ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
                if (handler != null) {
                    handler.useSkill(this._owner, skill, targets);
                } else {
                    skill.useSkill(this._owner, targets);
                }
            }
        } catch (Exception e) {
            _log.log(Level.WARNING, "", e);
        }
    }

    private void makeCast(EffectChanceSkillTrigger effect, Creature target) {
        try {
            if (effect == null || !effect.triggersChanceSkill())
                return;
            L2Skill triggered = SkillTable.getInstance().getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
            if (triggered == null)
                return;
            Creature caster = (triggered.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) ? this._owner : effect.getEffector();
            if (caster == null || triggered.getSkillType() == L2SkillType.NOTDONE || caster.isSkillDisabled(triggered))
                return;
            if (triggered.getReuseDelay() > 0)
                caster.disableSkill(triggered, triggered.getReuseDelay());
            WorldObject[] targets = triggered.getTargetList(caster, false, target);
            if (targets.length == 0)
                return;
            Creature firstTarget = (Creature) targets[0];
            ISkillHandler handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());
            this._owner.broadcastPacket(new MagicSkillLaunched(this._owner, triggered.getId(), triggered.getLevel(), targets));
            this._owner.broadcastPacket(new MagicSkillUse(this._owner, firstTarget, triggered.getId(), triggered.getLevel(), 0, 0));
            if (handler != null) {
                handler.useSkill(caster, triggered, targets);
            } else {
                triggered.useSkill(caster, targets);
            }
        } catch (Exception e) {
            _log.log(Level.WARNING, "", e);
        }
    }
}
