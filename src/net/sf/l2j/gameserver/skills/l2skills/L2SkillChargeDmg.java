package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class L2SkillChargeDmg extends L2Skill {
    public L2SkillChargeDmg(StatSet set) {
        super(set);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        if (!caster.isAlikeDead()) {
            double modifier = 0.0F;
            if (caster instanceof Player) {
                modifier = 0.7 + 0.3 * (double) (((Player) caster).getCharges() + this.getNumCharges());
            }

            boolean ss = caster.isChargedShot(ShotType.SOULSHOT);

            for (WorldObject obj : targets) {
                if (obj instanceof Creature target) {
                    if (!target.isAlikeDead()) {
                        boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
                        if (skillIsEvaded) {
                            if (caster instanceof Player) {
                                caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
                            }

                            if (target instanceof Player) {
                                target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(caster));
                            }
                        } else {
                            byte shld = Formulas.calcShldUse(caster, target, this);
                            boolean crit = false;
                            if (this.getBaseCritRate() > 0) {
                                crit = Formulas.calcCrit((double) (this.getBaseCritRate() * 10) * Formulas.getSTRBonus(caster));
                            }

                            double damage = Formulas.calcPhysDam(caster, target, this, shld, false, ss);
                            if (crit) {
                                damage *= 2.0F;
                            }

                            if (damage > (double) 0.0F) {
                                byte reflect = Formulas.calcSkillReflect(target, this);
                                if (this.hasEffects()) {
                                    if ((reflect & 1) != 0) {
                                        caster.stopSkillEffects(this.getId());
                                        this.getEffects(target, caster);
                                        caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(this));
                                    } else {
                                        target.stopSkillEffects(this.getId());
                                        if (Formulas.calcSkillSuccess(caster, target, this, shld, true)) {
                                            this.getEffects(caster, target, new Env(shld, false, false, false));
                                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(this));
                                        } else {
                                            caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(this));
                                        }
                                    }
                                }

                                double finalDamage = damage * modifier;
                                target.reduceCurrentHp(finalDamage, caster, this);
                                if ((reflect & 2) != 0) {
                                    caster.reduceCurrentHp(damage, target, this);
                                }

                                caster.sendDamageMessage(target, (int) finalDamage, false, crit, false);
                            } else {
                                caster.sendDamageMessage(target, 0, false, false, true);
                            }
                        }
                    }
                }
            }

            if (this.hasSelfEffects()) {
                L2Effect effect = caster.getFirstEffect(this.getId());
                if (effect != null && effect.isSelfEffect()) {
                    effect.exit();
                }

                this.getEffectsSelf(caster);
            }

            caster.setChargedShot(ShotType.SOULSHOT, this.isStaticReuse());
        }
    }
}
