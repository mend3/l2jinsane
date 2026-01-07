/**/
package net.sf.l2j.gameserver.enums.skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.l2skills.*;

import java.lang.reflect.InvocationTargetException;

public enum L2SkillType {
    PDAM,
    FATAL,
    MDAM,
    CPDAMPERCENT,
    MANADAM,
    DOT,
    MDOT,
    DRAIN_SOUL,
    DRAIN(L2SkillDrain.class),
    DEATHLINK,
    BLOW,
    SIGNET(L2SkillSignet.class),
    SIGNET_CASTTIME(L2SkillSignetCasttime.class),
    SEED(L2SkillSeed.class),
    BLEED,
    POISON,
    STUN,
    ROOT,
    CONFUSION,
    FEAR,
    SLEEP,
    MUTE,
    PARALYZE,
    WEAKNESS,
    HEAL,
    MANAHEAL,
    COMBATPOINTHEAL,
    HOT,
    MPHOT,
    CPHOT,
    BALANCE_LIFE,
    HEAL_STATIC,
    MANARECHARGE,
    HEAL_PERCENT,
    MANAHEAL_PERCENT,
    GIVE_SP,
    AGGDAMAGE,
    AGGREDUCE,
    AGGREMOVE,
    AGGREDUCE_CHAR,
    AGGDEBUFF,
    FISHING,
    PUMPING,
    REELING,
    UNLOCK,
    UNLOCK_SPECIAL,
    DELUXE_KEY_UNLOCK,
    ENCHANT_ARMOR,
    ENCHANT_WEAPON,
    SOULSHOT,
    SPIRITSHOT,
    SIEGEFLAG(L2SkillSiegeFlag.class),
    TAKECASTLE,
    WEAPON_SA,
    SOW,
    HARVEST,
    GET_PLAYER,
    DUMMY,
    INSTANT_JUMP,
    COMMON_CRAFT,
    DWARVEN_CRAFT,
    CREATE_ITEM(L2SkillCreateItem.class),
    EXTRACTABLE,
    EXTRACTABLE_FISH,
    SUMMON(L2SkillSummon.class),
    FEED_PET,
    DEATHLINK_PET,
    STRSIEGEASSAULT,
    ERASE,
    BETRAY,
    SPAWN(L2SkillSpawn.class),
    CANCEL,
    MAGE_BANE,
    WARRIOR_BANE,
    NEGATE,
    CANCEL_DEBUFF,
    BUFF,
    DEBUFF,
    PASSIVE,
    CONT,
    RESURRECT,
    CHARGEDAM(L2SkillChargeDmg.class),
    MHOT,
    DETECT_WEAKNESS,
    LUCK,
    RECALL(L2SkillTeleport.class),
    TELEPORT(L2SkillTeleport.class),
    SUMMON_FRIEND,
    REFLECT,
    SPOIL,
    SWEEP,
    FAKE_DEATH,
    UNBLEED,
    UNPOISON,
    UNDEAD_DEFENSE,
    BEAST_FEED,
    FUSION,
    CHANGE_APPEARANCE(L2SkillAppearance.class),
    COREDONE,
    NOTDONE;

    private Class<? extends L2Skill> _class = L2SkillDefault.class;

    L2SkillType() {
    }

    L2SkillType(Class<? extends L2Skill> classType) {
        this._class = classType;
    }

    public L2Skill makeSkill(StatSet set) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return this._class.getConstructor(StatSet.class).newInstance(set);
    }

}