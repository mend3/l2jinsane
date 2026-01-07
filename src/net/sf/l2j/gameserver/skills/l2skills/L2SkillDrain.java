package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class L2SkillDrain extends L2Skill {
    private final float _absorbPart;

    private final int _absorbAbs;

    public L2SkillDrain(StatSet set) {
        super(set);
        this._absorbPart = set.getFloat("absorbPart", 0.0F);
        this._absorbAbs = set.getInteger("absorbAbs", 0);
    }

    public void useSkill(Creature activeChar, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        boolean isPlayable = activeChar instanceof net.sf.l2j.gameserver.model.actor.Playable;
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isAlikeDead() || getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
                    if (activeChar == target || !target.isInvul()) {
                        boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
                        byte shld = Formulas.calcShldUse(activeChar, target, this);
                        int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, sps, bsps, mcrit);
                        if (damage > 0) {
                            int _drain = 0;
                            int _cp = (int) target.getCurrentCp();
                            int _hp = (int) target.getCurrentHp();
                            if (isPlayable && _cp > 0) {
                                if (damage < _cp) {
                                    _drain = 0;
                                } else {
                                    _drain = damage - _cp;
                                }
                            } else if (damage > _hp) {
                                _drain = _hp;
                            } else {
                                _drain = damage;
                            }
                            double hpAdd = (this._absorbAbs + this._absorbPart * _drain);
                            if (hpAdd > 0.0D) {
                                double hp = (activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp()) ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd);
                                activeChar.setCurrentHp(hp);
                                StatusUpdate suhp = new StatusUpdate(activeChar);
                                suhp.addAttribute(9, (int) hp);
                                activeChar.sendPacket(suhp);
                            }
                            if (!target.isDead() || getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                                Formulas.calcCastBreak(target, damage);
                                activeChar.sendDamageMessage(target, damage, mcrit, false, false);
                                if (hasEffects() && getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
                                    if ((Formulas.calcSkillReflect(target, this) & 0x1) > 0) {
                                        activeChar.stopSkillEffects(getId());
                                        getEffects(target, activeChar);
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(getId()));
                                    } else {
                                        target.stopSkillEffects(getId());
                                        if (Formulas.calcSkillSuccess(activeChar, target, this, shld, bsps)) {
                                            getEffects(activeChar, target);
                                        } else {
                                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(getId()));
                                        }
                                    }
                                target.reduceCurrentHp(damage, activeChar, this);
                            }
                        }
                    }
            }
        }
        if (hasSelfEffects()) {
            L2Effect effect = activeChar.getFirstEffect(getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();
            getEffectsSelf(activeChar);
        }
        activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
    }

    public void useCubicSkill(Cubic activeCubic, WorldObject[] targets) {
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isAlikeDead() || getTargetType() == L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                    boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
                    byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);
                    int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
                    if (damage > 0) {
                        Player owner = activeCubic.getOwner();
                        double hpAdd = (this._absorbAbs + this._absorbPart * damage);
                        if (hpAdd > 0.0D) {
                            double hp = (owner.getCurrentHp() + hpAdd > owner.getMaxHp()) ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd);
                            owner.setCurrentHp(hp);
                            StatusUpdate suhp = new StatusUpdate(owner);
                            suhp.addAttribute(9, (int) hp);
                            owner.sendPacket(suhp);
                        }
                        if (!target.isDead() || getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB) {
                            target.reduceCurrentHp(damage, activeCubic.getOwner(), this);
                            Formulas.calcCastBreak(target, damage);
                            owner.sendDamageMessage(target, damage, mcrit, false, false);
                        }
                    }
                }
            }
        }
    }
}
