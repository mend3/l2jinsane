package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class L2SkillElemental extends L2Skill {
    private final int[] _seeds;

    private final boolean _seedAny;

    public L2SkillElemental(StatSet set) {
        super(set);
        this._seeds = new int[3];
        this._seeds[0] = set.getInteger("seed1", 0);
        this._seeds[1] = set.getInteger("seed2", 0);
        this._seeds[2] = set.getInteger("seed3", 0);
        this._seedAny = set.getInteger("seed_any", 0) == 1;
    }

    public void useSkill(Creature activeChar, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isAlikeDead()) {
                    boolean charged = true;
                    if (!this._seedAny) {
                        for (int _seed : this._seeds) {
                            if (_seed != 0) {
                                L2Effect e = target.getFirstEffect(_seed);
                                if (e == null || !e.getInUse()) {
                                    charged = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        charged = false;
                        for (int _seed : this._seeds) {
                            if (_seed != 0) {
                                L2Effect e = target.getFirstEffect(_seed);
                                if (e != null && e.getInUse()) {
                                    charged = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!charged) {
                        activeChar.sendMessage("Target is not charged by elements.");
                    } else {
                        boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
                        byte shld = Formulas.calcShldUse(activeChar, target, this);
                        int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, sps, bsps, mcrit);
                        if (damage > 0) {
                            target.reduceCurrentHp(damage, activeChar, this);
                            Formulas.calcCastBreak(target, damage);
                            activeChar.sendDamageMessage(target, damage, false, false, false);
                        }
                        target.stopSkillEffects(getId());
                        getEffects(activeChar, target, new Env(shld, sps, false, bsps));
                    }
                }
            }
        }
        activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
    }
}
