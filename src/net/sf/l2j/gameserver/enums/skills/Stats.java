/**/
package net.sf.l2j.gameserver.enums.skills;

import java.util.NoSuchElementException;

public enum Stats {
    MAX_HP("maxHp"),
    MAX_MP("maxMp"),
    MAX_CP("maxCp"),
    REGENERATE_HP_RATE("regHp"),
    REGENERATE_CP_RATE("regCp"),
    REGENERATE_MP_RATE("regMp"),
    RECHARGE_MP_RATE("gainMp"),
    HEAL_EFFECTIVNESS("gainHp"),
    HEAL_PROFICIENCY("giveHp"),
    POWER_DEFENCE("pDef"),
    MAGIC_DEFENCE("mDef"),
    POWER_ATTACK("pAtk"),
    MAGIC_ATTACK("mAtk"),
    POWER_ATTACK_SPEED("pAtkSpd"),
    MAGIC_ATTACK_SPEED("mAtkSpd"),
    MAGIC_REUSE_RATE("mReuse"),
    P_REUSE("pReuse"),
    SHIELD_DEFENCE("sDef"),
    SHIELD_DEFENCE_ANGLE("shieldDefAngle"),
    SHIELD_RATE("rShld"),
    CRITICAL_DAMAGE("cAtk"),
    CRITICAL_DAMAGE_POS("cAtkPos"),
    CRITICAL_DAMAGE_ADD("cAtkAdd"),
    PVP_PHYSICAL_DMG("pvpPhysDmg"),
    PVP_MAGICAL_DMG("pvpMagicalDmg"),
    PVP_PHYS_SKILL_DMG("pvpPhysSkillsDmg"),
    PVP_PHYS_SKILL_DEF("pvpPhysSkillsDef"),
    EVASION_RATE("rEvas"),
    P_SKILL_EVASION("pSkillEvas"),
    CRITICAL_RATE("rCrit"),
    BLOW_RATE("blowRate"),
    LETHAL_RATE("lethalRate"),
    MCRITICAL_RATE("mCritRate"),
    ATTACK_CANCEL("cancel"),
    ACCURACY_COMBAT("accCombat"),
    POWER_ATTACK_RANGE("pAtkRange"),
    POWER_ATTACK_ANGLE("pAtkAngle"),
    ATTACK_COUNT_MAX("atkCountMax"),
    RUN_SPEED("runSpd"),
    STAT_STR("STR"),
    STAT_CON("CON"),
    STAT_DEX("DEX"),
    STAT_INT("INT"),
    STAT_WIT("WIT"),
    STAT_MEN("MEN"),
    BREATH("breath"),
    FALL("fall"),
    AGGRESSION("aggression"),
    BLEED("bleed"),
    POISON("poison"),
    STUN("stun"),
    ROOT("root"),
    MOVEMENT("movement"),
    CONFUSION("confusion"),
    SLEEP("sleep"),
    VALAKAS("valakas"),
    VALAKAS_RES("valakasRes"),
    FIRE_RES("fireRes"),
    WATER_RES("waterRes"),
    WIND_RES("windRes"),
    EARTH_RES("earthRes"),
    HOLY_RES("holyRes"),
    DARK_RES("darkRes"),
    FIRE_POWER("firePower"),
    WATER_POWER("waterPower"),
    WIND_POWER("windPower"),
    EARTH_POWER("earthPower"),
    HOLY_POWER("holyPower"),
    DARK_POWER("darkPower"),
    BLEED_VULN("bleedVuln"),
    POISON_VULN("poisonVuln"),
    STUN_VULN("stunVuln"),
    PARALYZE_VULN("paralyzeVuln"),
    ROOT_VULN("rootVuln"),
    SLEEP_VULN("sleepVuln"),
    DAMAGE_ZONE_VULN("damageZoneVuln"),
    CRIT_VULN("critVuln"),
    CANCEL_VULN("cancelVuln"),
    DERANGEMENT_VULN("derangementVuln"),
    DEBUFF_VULN("debuffVuln"),
    SWORD_WPN_VULN("swordWpnVuln"),
    BLUNT_WPN_VULN("bluntWpnVuln"),
    DAGGER_WPN_VULN("daggerWpnVuln"),
    BOW_WPN_VULN("bowWpnVuln"),
    POLE_WPN_VULN("poleWpnVuln"),
    DUAL_WPN_VULN("dualWpnVuln"),
    DUALFIST_WPN_VULN("dualFistWpnVuln"),
    BIGSWORD_WPN_VULN("bigSwordWpnVuln"),
    BIGBLUNT_WPN_VULN("bigBluntWpnVuln"),
    REFLECT_DAMAGE_PERCENT("reflectDam"),
    REFLECT_SKILL_MAGIC("reflectSkillMagic"),
    REFLECT_SKILL_PHYSIC("reflectSkillPhysic"),
    VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),
    VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),
    ABSORB_DAMAGE_PERCENT("absorbDam"),
    TRANSFER_DAMAGE_PERCENT("transDam"),
    PATK_PLANTS("pAtk-plants"),
    PATK_INSECTS("pAtk-insects"),
    PATK_ANIMALS("pAtk-animals"),
    PATK_MONSTERS("pAtk-monsters"),
    PATK_DRAGONS("pAtk-dragons"),
    PATK_GIANTS("pAtk-giants"),
    PATK_MCREATURES("pAtk-magicCreature"),
    PDEF_PLANTS("pDef-plants"),
    PDEF_INSECTS("pDef-insects"),
    PDEF_ANIMALS("pDef-animals"),
    PDEF_MONSTERS("pDef-monsters"),
    PDEF_DRAGONS("pDef-dragons"),
    PDEF_GIANTS("pDef-giants"),
    PDEF_MCREATURES("pDef-magicCreature"),
    MAX_LOAD("maxLoad"),
    INV_LIM("inventoryLimit"),
    WH_LIM("whLimit"),
    FREIGHT_LIM("FreightLimit"),
    P_SELL_LIM("PrivateSellLimit"),
    P_BUY_LIM("PrivateBuyLimit"),
    REC_D_LIM("DwarfRecipeLimit"),
    REC_C_LIM("CommonRecipeLimit"),
    PHYSICAL_MP_CONSUME_RATE("PhysicalMpConsumeRate"),
    MAGICAL_MP_CONSUME_RATE("MagicalMpConsumeRate"),
    DANCE_MP_CONSUME_RATE("DanceMpConsumeRate"),
    SKILL_MASTERY("skillMastery");

    public static final int NUM_STATS = values().length;
    private final String _value;

    Stats(String s) {
        this._value = s;
    }

    public static Stats valueOfXml(String name) {
        name = name.intern();
        Stats[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            Stats s = var1[var3];
            if (s.getValue().equals(name)) {
                return s;
            }
        }

        throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
    }

    public String getValue() {
        return this._value;
    }
}
