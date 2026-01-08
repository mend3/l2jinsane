package net.sf.l2j.gameserver.model.item.kind;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameChance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Weapon extends Item {
    private final WeaponType _type;
    private final int _rndDam;
    private final int _soulShotCount;
    private final int _spiritShotCount;
    private final int _mpConsume;
    private final int _mpConsumeReduceRate;
    private final int _mpConsumeReduceValue;
    private final boolean _isMagical;
    private final int _reuseDelay;
    private final int _reducedSoulshot;
    private final int _reducedSoulshotChance;
    private IntIntHolder _enchant4Skill = null;
    private IntIntHolder _skillsOnCast;
    private Condition _skillsOnCastCondition = null;
    private IntIntHolder _skillsOnCrit;
    private Condition _skillsOnCritCondition = null;

    public Weapon(StatSet set) {
        super(set);
        this._type = WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
        this._type1 = 0;
        this._type2 = 0;
        this._soulShotCount = set.getInteger("soulshots", 0);
        this._spiritShotCount = set.getInteger("spiritshots", 0);
        this._rndDam = set.getInteger("random_damage", 0);
        this._mpConsume = set.getInteger("mp_consume", 0);
        String[] reduce = set.getString("mp_consume_reduce", "0,0").split(",");
        this._mpConsumeReduceRate = Integer.parseInt(reduce[0]);
        this._mpConsumeReduceValue = Integer.parseInt(reduce[1]);
        this._reuseDelay = set.getInteger("reuse_delay", 0);
        this._isMagical = set.getBool("is_magical", false);
        String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
        this._reducedSoulshotChance = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[0]) : 0;
        this._reducedSoulshot = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[1]) : 0;
        String skill = set.getString("enchant4_skill", null);
        if (skill != null) {
            String[] info = skill.split("-");
            if (info != null && info.length == 2) {
                int id = 0;
                int level = 0;

                try {
                    id = Integer.parseInt(info[0]);
                    level = Integer.parseInt(info[1]);
                } catch (Exception var13) {
                    _log.info("> Couldnt parse " + skill + " in weapon enchant skills! item " + this.toString());
                }

                if (id > 0 && level > 0) {
                    this._enchant4Skill = new IntIntHolder(id, level);
                }
            }
        }

        skill = set.getString("oncast_skill", null);
        if (skill != null) {
            String[] info = skill.split("-");
            String infochance = set.getString("oncast_chance", null);
            if (info != null && info.length == 2) {
                int id = 0;
                int level = 0;
                int chance = 0;

                try {
                    id = Integer.parseInt(info[0]);
                    level = Integer.parseInt(info[1]);
                    if (infochance != null) {
                        chance = Integer.parseInt(infochance);
                    }
                } catch (Exception var12) {
                    _log.info("> Couldnt parse " + skill + " in weapon oncast skills! item " + this.toString());
                }

                if (id > 0 && level > 0 && chance > 0) {
                    this._skillsOnCast = new IntIntHolder(id, level);
                    if (infochance != null) {
                        this._skillsOnCastCondition = new ConditionGameChance(chance);
                    }
                }
            }
        }

        skill = set.getString("oncrit_skill", null);
        if (skill != null) {
            String[] info = skill.split("-");
            String infochance = set.getString("oncrit_chance", null);
            if (info != null && info.length == 2) {
                int id = 0;
                int level = 0;
                int chance = 0;

                try {
                    id = Integer.parseInt(info[0]);
                    level = Integer.parseInt(info[1]);
                    if (infochance != null) {
                        chance = Integer.parseInt(infochance);
                    }
                } catch (Exception var11) {
                    _log.info("> Couldnt parse " + skill + " in weapon oncrit skills! item " + this.toString());
                }

                if (id > 0 && level > 0 && chance > 0) {
                    this._skillsOnCrit = new IntIntHolder(id, level);
                    if (infochance != null) {
                        this._skillsOnCritCondition = new ConditionGameChance(chance);
                    }
                }
            }
        }

    }

    public WeaponType getItemType() {
        return this._type;
    }

    public int getItemMask() {
        return this.getItemType().mask();
    }

    public int getSoulShotCount() {
        return this._soulShotCount;
    }

    public int getSpiritShotCount() {
        return this._spiritShotCount;
    }

    public int getReducedSoulShot() {
        return this._reducedSoulshot;
    }

    public int getReducedSoulShotChance() {
        return this._reducedSoulshotChance;
    }

    public int getRandomDamage() {
        return this._rndDam;
    }

    public int getReuseDelay() {
        return this._reuseDelay;
    }

    public final boolean isMagical() {
        return this._isMagical;
    }

    public int getMpConsume() {
        return this._mpConsumeReduceRate > 0 && Rnd.get(100) < this._mpConsumeReduceRate ? this._mpConsumeReduceValue : this._mpConsume;
    }

    public L2Skill getEnchant4Skill() {
        return this._enchant4Skill == null ? null : this._enchant4Skill.getSkill();
    }

    public List<L2Effect> getSkillEffects(Creature caster, Creature target, boolean crit) {
        if (this._skillsOnCrit != null && crit) {
            List<L2Effect> effects = new ArrayList<>();
            if (this._skillsOnCritCondition != null) {
                Env env = new Env();
                env.setCharacter(caster);
                env.setTarget(target);
                env.setSkill(this._skillsOnCrit.getSkill());
                if (!this._skillsOnCritCondition.test(env)) {
                    return Collections.emptyList();
                }
            }

            byte shld = Formulas.calcShldUse(caster, target, this._skillsOnCrit.getSkill());
            if (!Formulas.calcSkillSuccess(caster, target, this._skillsOnCrit.getSkill(), shld, false)) {
                return Collections.emptyList();
            } else {
                if (target.getFirstEffect(this._skillsOnCrit.getSkill().getId()) != null) {
                    target.getFirstEffect(this._skillsOnCrit.getSkill().getId()).exit();
                }

                for (L2Effect e : this._skillsOnCrit.getSkill().getEffects(caster, target, new Env(shld, false, false, false))) {
                    effects.add(e);
                }

                return effects;
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<L2Effect> getSkillEffects(Creature caster, Creature target, L2Skill trigger) {
        if (this._skillsOnCast == null) {
            return Collections.emptyList();
        } else if (trigger.isOffensive() != this._skillsOnCast.getSkill().isOffensive()) {
            return Collections.emptyList();
        } else if ((trigger.isToggle() || !trigger.isMagic()) && this._skillsOnCast.getSkill().getSkillType() == L2SkillType.BUFF) {
            return Collections.emptyList();
        } else {
            if (this._skillsOnCastCondition != null) {
                Env env = new Env();
                env.setCharacter(caster);
                env.setTarget(target);
                env.setSkill(this._skillsOnCast.getSkill());
                if (!this._skillsOnCastCondition.test(env)) {
                    return Collections.emptyList();
                }
            }

            byte shld = Formulas.calcShldUse(caster, target, this._skillsOnCast.getSkill());
            if (this._skillsOnCast.getSkill().isOffensive() && !Formulas.calcSkillSuccess(caster, target, this._skillsOnCast.getSkill(), shld, false)) {
                return Collections.emptyList();
            } else {
                ISkillHandler handler = SkillHandler.getInstance().getHandler(this._skillsOnCast.getSkill().getSkillType());
                Creature[] targets = new Creature[]{target};
                if (handler != null) {
                    handler.useSkill(caster, this._skillsOnCast.getSkill(), targets);
                } else {
                    this._skillsOnCast.getSkill().useSkill(caster, targets);
                }

                if (caster instanceof Player) {
                    for (Npc npcMob : caster.getKnownTypeInRadius(Npc.class, 1000)) {
                        List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
                        if (scripts != null) {
                            for (Quest quest : scripts) {
                                quest.notifySkillSee(npcMob, (Player) caster, this._skillsOnCast.getSkill(), targets, false);
                            }
                        }
                    }
                }

                return Collections.emptyList();
            }
        }
    }
}
