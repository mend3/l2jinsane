/**/
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.enums.AttackType;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.SkillChangeType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.MotherTreeZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public final class Formulas {
    public static final byte SHIELD_DEFENSE_FAILED = 0;
    public static final byte SHIELD_DEFENSE_SUCCEED = 1;
    public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2;
    public static final byte SKILL_REFLECT_FAILED = 0;
    public static final byte SKILL_REFLECT_SUCCEED = 1;
    public static final byte SKILL_REFLECT_VENGEANCE = 2;
    public static final int MAX_STAT_VALUE = 100;
    public static final double[] WIT_BONUS = new double[100];
    public static final double[] MEN_BONUS = new double[100];
    public static final double[] INT_BONUS = new double[100];
    public static final double[] STR_BONUS = new double[100];
    public static final double[] DEX_BONUS = new double[100];
    public static final double[] CON_BONUS = new double[100];
    public static final double[] BASE_EVASION_ACCURACY = new double[100];
    private static final CLogger LOGGER = new CLogger(Formulas.class.getName());
    private static final double[] SQRT_MEN_BONUS = new double[100];
    private static final double[] SQRT_CON_BONUS = new double[100];
    private static final int HP_REGENERATE_PERIOD = 3000;
    private static final byte MELEE_ATTACK_RANGE = 40;
    private static final double[] STR_COMPUTE = new double[]{1.036, 34.845};
    private static final double[] INT_COMPUTE = new double[]{1.02, (double) 31.375F};
    private static final double[] DEX_COMPUTE = new double[]{1.009, 19.36};
    private static final double[] WIT_COMPUTE = new double[]{1.05, (double) 20.0F};
    private static final double[] CON_COMPUTE = new double[]{1.03, 27.632};
    private static final double[] MEN_COMPUTE = new double[]{1.01, -0.06};
    private static final double[] karmaMods;

    static {
        for (int i = 0; i < STR_BONUS.length; ++i) {
            STR_BONUS[i] = Math.floor(Math.pow(STR_COMPUTE[0], (double) i - STR_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < INT_BONUS.length; ++i) {
            INT_BONUS[i] = Math.floor(Math.pow(INT_COMPUTE[0], (double) i - INT_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < DEX_BONUS.length; ++i) {
            DEX_BONUS[i] = Math.floor(Math.pow(DEX_COMPUTE[0], (double) i - DEX_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < WIT_BONUS.length; ++i) {
            WIT_BONUS[i] = Math.floor(Math.pow(WIT_COMPUTE[0], (double) i - WIT_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < CON_BONUS.length; ++i) {
            CON_BONUS[i] = Math.floor(Math.pow(CON_COMPUTE[0], (double) i - CON_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < MEN_BONUS.length; ++i) {
            MEN_BONUS[i] = Math.floor(Math.pow(MEN_COMPUTE[0], (double) i - MEN_COMPUTE[1]) * (double) 100.0F + (double) 0.5F) / (double) 100.0F;
        }

        for (int i = 0; i < BASE_EVASION_ACCURACY.length; ++i) {
            BASE_EVASION_ACCURACY[i] = Math.sqrt(i) * (double) 6.0F;
        }

        for (int i = 0; i < SQRT_CON_BONUS.length; ++i) {
            SQRT_CON_BONUS[i] = Math.sqrt(CON_BONUS[i]);
        }

        for (int i = 0; i < SQRT_MEN_BONUS.length; ++i) {
            SQRT_MEN_BONUS[i] = Math.sqrt(MEN_BONUS[i]);
        }

        karmaMods = new double[]{(double) 0.0F, 0.772184315, 2.069377971, 2.315085083, 2.467800843, 2.514219611, 2.510075822, 2.532083418, 2.473028945, 2.377178509, 2.285526643, 2.654635163, 2.963434737, 3.266100461, 3.561112664, 3.847320291, 4.123878064, 4.390194136, 4.645886341, 4.890745518, 5.124704707, 6.97914069, 7.270620642, 7.548951721, 7.81438302, 8.067235867, 8.307889422, 8.536768399, 8.754332624, 8.961068152, 9.157479758, 11.41901707, 11.64989746, 11.87007991, 12.08015809, 12.28072687, 12.47237891, 12.65570177, 12.83127553, 12.99967093, 13.16144786, 15.6563607, 15.84513182, 16.02782135, 16.20501182, 16.37727218, 16.54515749, 16.70920885, 16.86995336, 17.02790439, 17.18356182, 19.85792061, 20.04235517, 20.22556446, 20.40806338, 20.59035551, 20.77293378, 20.95628115, 21.1408714, 21.3271699, 21.51563446, 24.3895427, 24.61486587, 24.84389213, 25.07711247, 25.31501442, 25.55808296, 25.80680152, 26.06165297, 26.32312062, 26.59168923, 26.86784604, 27.15208178, 27.44489172, 27.74677676, 28.05824444, 28.37981005, 28.71199773, 29.05534154, 29.41038662, 29.77769028};
    }

    public static int getRegeneratePeriod(Creature cha) {
        return cha instanceof Door ? 300000 : 3000;
    }

    public static double calcHpRegen(Creature cha) {
        double init = cha.getTemplate().getBaseHpReg();
        double hpRegenMultiplier = cha.isRaidRelated() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
        double hpRegenBonus = 0.0F;
        if (cha.isChampion()) {
            hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;
        }

        if (cha instanceof Player player) {
            init += player.getLevel() > 10 ? (double) (player.getLevel() - 1) / (double) 10.0F : (double) 0.5F;
            if (FestivalOfDarknessManager.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) {
                hpRegenMultiplier *= calcFestivalRegenModifier(player);
            } else if (calcSiegeRegenModifer(player)) {
                hpRegenMultiplier *= 1.5F;
            }

            if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null) {
                int clanHallIndex = player.getClan().getClanHallId();
                if (clanHallIndex > 0) {
                    ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
                    if (clansHall != null && clansHall.getFunction(3) != null) {
                        hpRegenMultiplier *= 1 + clansHall.getFunction(3).getLvl() / 100;
                    }
                }
            }

            if (player.isInsideZone(ZoneId.MOTHER_TREE)) {
                MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
                int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
                hpRegenBonus += hpBonus;
            }

            if (player.isSitting()) {
                hpRegenMultiplier *= 1.5F;
            } else if (!player.isMoving()) {
                hpRegenMultiplier *= 1.1;
            } else if (player.isRunning()) {
                hpRegenMultiplier *= 0.7;
            }
        }

        init *= cha.getLevelMod() * CON_BONUS[cha.getCON()];
        if (init < (double) 1.0F) {
            init = 1.0F;
        }

        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
    }

    public static double calcMpRegen(Creature cha) {
        double init = cha.getTemplate().getBaseMpReg();
        double mpRegenMultiplier = cha.isRaidRelated() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
        double mpRegenBonus = 0.0F;
        if (cha instanceof Player player) {
            init += 0.3 * ((double) (player.getLevel() - 1) / (double) 10.0F);
            if (FestivalOfDarknessManager.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) {
                mpRegenMultiplier *= calcFestivalRegenModifier(player);
            }

            if (player.isInsideZone(ZoneId.MOTHER_TREE)) {
                MotherTreeZone zone = ZoneManager.getInstance().getZone(player, MotherTreeZone.class);
                int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
                mpRegenBonus += mpBonus;
            }

            if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null) {
                int clanHallIndex = player.getClan().getClanHallId();
                if (clanHallIndex > 0) {
                    ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
                    if (clansHall != null && clansHall.getFunction(4) != null) {
                        mpRegenMultiplier *= 1 + clansHall.getFunction(4).getLvl() / 100;
                    }
                }
            }

            if (player.isSitting()) {
                mpRegenMultiplier *= 1.5F;
            } else if (!player.isMoving()) {
                mpRegenMultiplier *= 1.1;
            } else if (player.isRunning()) {
                mpRegenMultiplier *= 0.7;
            }
        }

        init *= cha.getLevelMod() * MEN_BONUS[cha.getMEN()];
        if (init < (double) 1.0F) {
            init = 1.0F;
        }

        return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
    }

    public static double calcCpRegen(Player player) {
        double init = player.getTemplate().getBaseHpReg() + (player.getLevel() > 10 ? (double) (player.getLevel() - 1) / (double) 10.0F : (double) 0.5F);
        double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
        if (player.isSitting()) {
            cpRegenMultiplier *= 1.5F;
        } else if (!player.isMoving()) {
            cpRegenMultiplier *= 1.1;
        } else if (player.isRunning()) {
            cpRegenMultiplier *= 0.7;
        }

        init *= player.getLevelMod() * CON_BONUS[player.getCON()];
        if (init < (double) 1.0F) {
            init = 1.0F;
        }

        return player.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier;
    }

    public static double calcFestivalRegenModifier(Player player) {
        int[] festivalInfo = FestivalOfDarknessManager.getInstance().getFestivalForPlayer(player);
        int festivalId = festivalInfo[1];
        if (festivalId < 0) {
            return 1.0F;
        } else {
            CabalType oracle = CabalType.VALUES[festivalInfo[0]];
            int[] festivalCenter;
            if (oracle == CabalType.DAWN) {
                festivalCenter = FestivalOfDarknessManager.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
            } else {
                festivalCenter = FestivalOfDarknessManager.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
            }

            double distToCenter = player.getPlanDistanceSq(festivalCenter[0], festivalCenter[1]);
            if (Config.DEVELOPER) {
                LOGGER.info("calcFestivalRegenModifier() distance: {}, RegenMulti: {}.", distToCenter, String.format("%1.2f", (double) 1.0F - distToCenter * 5.0E-4));
            }

            return (double) 1.0F - distToCenter * 5.0E-4;
        }
    }

    public static boolean calcSiegeRegenModifer(Player player) {
        if (player == null) {
            return false;
        } else {
            Clan clan = player.getClan();
            if (clan == null) {
                return false;
            } else {
                Siege siege = CastleManager.getInstance().getActiveSiege(player);
                return siege != null && siege.checkSide(clan, SiegeSide.ATTACKER) ? MathUtil.checkIfInRange(200, player, clan.getFlag(), true) : false;
            }
        }
    }

    public static double calcBlowDamage(Creature attacker, Creature target, L2Skill skill, byte shld, boolean ss) {
        double defence = target.getPDef(attacker);
        switch (shld) {
            case 1 -> defence += target.getShldDef();
            case 2 -> {
                return 1.0F;
            }
        }

        boolean isPvP = attacker instanceof Playable && target instanceof Playable;
        double power = skill.getPower();
        double damage = 0.0F;
        damage += calcValakasAttribute(attacker, target, skill);
        if (ss) {
            damage *= 2.0F;
            if (skill.getSSBoost() > 0.0F) {
                power *= skill.getSSBoost();
            }
        }

        damage += power;
        damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, 1.0F, target, skill);
        damage *= (attacker.calcStat(Stats.CRITICAL_DAMAGE_POS, 1.0F, target, skill) - (double) 1.0F) / (double) 2.0F + (double) 1.0F;
        damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0F, target, skill) * (double) 6.5F;
        damage *= target.calcStat(Stats.CRIT_VULN, 1.0F, target, skill);
        damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
        damage *= (double) 70.0F / defence;
        damage *= attacker.getRandomDamageMultiplier();
        damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.Blow, attacker, target);
        int keyId = target instanceof Player ? target.getActingPlayer().getClassId().getId() : (target instanceof Monster ? -1 : -2);
        damage *= SkillBalanceManager.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.SkillBlow, target);
        if (isPvP) {
            damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0F, null, null);
        }

        return Math.max(damage, 1.0F);
    }

    public static double calcPhysDam(Creature attacker, Creature target, L2Skill skill, byte shld, boolean crit, boolean ss) {
        if (attacker instanceof Player pcInst) {
            if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage()) {
                return 0.0F;
            }
        }

        double defence = target.getPDef(attacker);
        switch (shld) {
            case 1 -> defence += target.getShldDef();
            case 2 -> {
                return 1.0F;
            }
        }

        boolean isPvP = attacker instanceof Playable && target instanceof Playable;
        double damage = attacker.getPAtk(target);
        damage += calcValakasAttribute(attacker, target, skill);
        if (ss) {
            damage *= 2.0F;
        }

        if (skill != null) {
            double skillpower = skill.getPower(attacker);
            float ssBoost = skill.getSSBoost();
            if (ssBoost > 0.0F && ss) {
                skillpower *= ssBoost;
            }

            damage += skillpower;
        }

        Weapon weapon = attacker.getActiveWeaponItem();
        Stats stat = null;
        if (weapon != null) {
            switch (weapon.getItemType()) {
                case BOW -> stat = Stats.BOW_WPN_VULN;
                case BLUNT -> stat = Stats.BLUNT_WPN_VULN;
                case BIGSWORD -> stat = Stats.BIGSWORD_WPN_VULN;
                case BIGBLUNT -> stat = Stats.BIGBLUNT_WPN_VULN;
                case DAGGER -> stat = Stats.DAGGER_WPN_VULN;
                case DUAL -> stat = Stats.DUAL_WPN_VULN;
                case DUALFIST -> stat = Stats.DUALFIST_WPN_VULN;
                case POLE -> stat = Stats.POLE_WPN_VULN;
                case SWORD -> stat = Stats.SWORD_WPN_VULN;
            }
        }

        if (crit) {
            damage = (double) 2.0F * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1.0F, target, skill) * attacker.calcStat(Stats.CRITICAL_DAMAGE_POS, 1.0F, target, skill) * target.calcStat(Stats.CRIT_VULN, 1.0F, target, null) * ((double) 70.0F * damage / defence);
            damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0F, target, skill) * (double) 70.0F / defence;
        } else {
            damage = (double) 70.0F * damage / defence;
        }

        if (stat != null) {
            damage = target.calcStat(stat, damage, target, null);
        }

        if (skill == null || skill.getEffectType() != L2SkillType.CHARGEDAM) {
            damage *= attacker.getRandomDamageMultiplier();
        }

        if (target instanceof Npc) {
            double multiplier;
            switch (((Npc) target).getTemplate().getRace()) {
                case BEAST:
                    multiplier = (double) 1.0F + (attacker.getPAtkMonsters(target) - target.getPDefMonsters(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case ANIMAL:
                    multiplier = (double) 1.0F + (attacker.getPAtkAnimals(target) - target.getPDefAnimals(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case PLANT:
                    multiplier = (double) 1.0F + (attacker.getPAtkPlants(target) - target.getPDefPlants(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case DRAGON:
                    multiplier = (double) 1.0F + (attacker.getPAtkDragons(target) - target.getPDefDragons(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case BUG:
                    multiplier = (double) 1.0F + (attacker.getPAtkInsects(target) - target.getPDefInsects(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case GIANT:
                    multiplier = (double) 1.0F + (attacker.getPAtkGiants(target) - target.getPDefGiants(target)) / (double) 100.0F;
                    damage *= multiplier;
                    break;
                case MAGICCREATURE:
                    multiplier = (double) 1.0F + (attacker.getPAtkMagicCreatures(target) - target.getPDefMagicCreatures(target)) / (double) 100.0F;
                    damage *= multiplier;
            }
        }

        if (damage > (double) 0.0F && damage < (double) 1.0F) {
            damage = 1.0F;
        } else if (damage < (double) 0.0F) {
            damage = 0.0F;
        }

        if (isPvP) {
            if (skill == null) {
                damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0F, null, null);
            } else {
                damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0F, null, null);
            }
        }

        damage += calcElemental(attacker, target, null);
        if (crit) {
            if (skill != null && skill.getSkillType() == L2SkillType.PDAM) {
                damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.PhysicalSkillCritical, attacker, target);
                int keyId = target instanceof Player ? target.getActingPlayer().getClassId().getId() : (target instanceof Monster ? -1 : -2);
                damage *= SkillBalanceManager.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.PCrit, target);
            } else {
                damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.Crit, attacker, target);
            }
        } else if (skill != null && skill.getSkillType() == L2SkillType.PDAM) {
            damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.PhysicalSkillDamage, attacker, target);
        } else {
            damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.Normal, attacker, target);
        }

        return damage;
    }

    public static double calcMagicDam(Creature attacker, Creature target, L2Skill skill, byte shld, boolean ss, boolean bss, boolean mcrit) {
        if (attacker instanceof Player pcInst) {
            if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage()) {
                return 0.0F;
            }
        }

        double mDef = target.getMDef(attacker, skill);
        switch (shld) {
            case 1:
                mDef += target.getShldDef();
            default:
                double mAtk = attacker.getMAtk(target, skill);
                if (bss) {
                    mAtk *= 4.0F;
                } else if (ss) {
                    mAtk *= 2.0F;
                }

                double damage = (double) 91.0F * Math.sqrt(mAtk) / mDef * skill.getPower(attacker);
                if (Config.MAGIC_FAILURES && !calcMagicSuccess(attacker, target, skill)) {
                    if (attacker instanceof Player) {
                        if (calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9) {
                            if (skill.getSkillType() == L2SkillType.DRAIN) {
                                attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
                            } else {
                                attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
                            }

                            damage /= 2.0F;
                        } else {
                            attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                            damage = 1.0F;
                        }
                    }

                    if (target instanceof Player) {
                        if (skill.getSkillType() == L2SkillType.DRAIN) {
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_DRAIN).addCharName(attacker));
                        } else {
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_MAGIC).addCharName(attacker));
                        }
                    }
                } else if (mcrit) {
                    damage *= 4.0F;
                }

                if (mcrit) {
                    damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.MCrit, attacker, target);
                    int keyId = target instanceof Player ? target.getActingPlayer().getClassId().getId() : (target instanceof Monster ? -1 : -2);
                    damage *= SkillBalanceManager.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.MCrit, target);
                } else {
                    damage *= ClassBalanceManager.getInstance().getBalancedClass(AttackType.Magic, attacker, target);
                }

                if (attacker instanceof Playable && target instanceof Playable) {
                    if (skill.isMagic()) {
                        damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1.0F, null, null);
                    } else {
                        damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0F, null, null);
                    }
                }

                damage *= calcElemental(attacker, target, skill);
                return damage;
            case 2:
                return 1.0F;
        }
    }

    public static double calcMagicDam(Cubic attacker, Creature target, L2Skill skill, boolean mcrit, byte shld) {
        double mDef = target.getMDef(attacker.getOwner(), skill);
        switch (shld) {
            case 1 -> mDef += target.getShldDef();
            case 2 -> {
                return 1.0F;
            }
        }

        double damage = (double) 91.0F / mDef * skill.getPower();
        Player owner = attacker.getOwner();
        if (Config.MAGIC_FAILURES && !calcMagicSuccess(owner, target, skill)) {
            if (calcMagicSuccess(owner, target, skill) && target.getLevel() - skill.getMagicLevel() <= 9) {
                if (skill.getSkillType() == L2SkillType.DRAIN) {
                    owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
                } else {
                    owner.sendPacket(SystemMessageId.ATTACK_FAILED);
                }

                damage /= 2.0F;
            } else {
                owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                damage = 1.0F;
            }

            if (target instanceof Player) {
                if (skill.getSkillType() == L2SkillType.DRAIN) {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_DRAIN).addCharName(owner));
                } else {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_MAGIC).addCharName(owner));
                }
            }
        } else if (mcrit) {
            damage *= 4.0F;
        }

        damage *= calcElemental(owner, target, skill);
        return damage;
    }

    public static boolean calcCrit(double rate) {
        return rate > (double) Rnd.get(1000);
    }

    public static boolean calcBlow(Creature attacker, Creature target, int chance) {
        return attacker.calcStat(Stats.BLOW_RATE, (double) chance * ((double) 1.0F + (double) ((attacker.getDEX() - 20) / 100)), target, null) > (double) Rnd.get(100);
    }

    public static double calcLethal(Creature attacker, Creature target, int baseLethal, int magiclvl) {
        double chance = 0.0F;
        if (magiclvl > 0) {
            int delta = (magiclvl + attacker.getLevel()) / 2 - 1 - target.getLevel();
            if (delta >= -3) {
                chance = (double) baseLethal * ((double) attacker.getLevel() / (double) target.getLevel());
            } else if (delta < -3 && delta >= -9) {
                chance = -3 * (baseLethal / delta);
            } else {
                chance = baseLethal / 15;
            }
        } else {
            chance = (double) baseLethal * ((double) attacker.getLevel() / (double) target.getLevel());
        }

        chance = (double) 10.0F * attacker.calcStat(Stats.LETHAL_RATE, chance, target, null);
        if (Config.DEVELOPER) {
            LOGGER.info("Current calcLethal: {} / 1000.", chance);
        }

        return chance;
    }

    public static void calcLethalHit(Creature attacker, Creature target, L2Skill skill) {
        if (!target.isRaidRelated() && !(target instanceof Door)) {
            if (target instanceof Npc) {
                switch (((Npc) target).getNpcId()) {
                    case 22215:
                    case 22216:
                    case 22217:
                    case 35062:
                        return;
                }
            }

            if (skill.getLethalChance2() > 0 && (double) Rnd.get(1000) < calcLethal(attacker, target, skill.getLethalChance2(), skill.getMagicLevel())) {
                if (target instanceof Npc) {
                    target.reduceCurrentHp(target.getCurrentHp() - (double) 1.0F, attacker, skill);
                } else if (target instanceof Player player) {
                    if (!player.isInvul() && (!(attacker instanceof Player) || !attacker.isGM() || ((Player) attacker).getAccessLevel().canGiveDamage())) {
                        player.setCurrentHp(1.0F);
                        player.setCurrentCp(1.0F);
                        player.sendPacket(SystemMessageId.LETHAL_STRIKE);
                    }
                }

                attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
            } else if (skill.getLethalChance1() > 0 && (double) Rnd.get(1000) < calcLethal(attacker, target, skill.getLethalChance1(), skill.getMagicLevel())) {
                if (target instanceof Npc) {
                    target.reduceCurrentHp(target.getCurrentHp() / (double) 2.0F, attacker, skill);
                } else if (target instanceof Player player) {
                    if (!player.isInvul() && (!(attacker instanceof Player) || !attacker.isGM() || ((Player) attacker).getAccessLevel().canGiveDamage())) {
                        player.setCurrentCp(1.0F);
                        player.sendPacket(SystemMessageId.LETHAL_STRIKE);
                    }
                }

                attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
            }

        }
    }

    public static boolean calcMCrit(int mRate) {
        if (Config.DEVELOPER) {
            LOGGER.info("Current mCritRate: {} / 1000.", mRate);
        }

        return mRate > Rnd.get(1000);
    }

    public static void calcCastBreak(Creature target, double dmg) {
        if (!target.isRaidRelated() && !target.isInvul()) {
            if (target instanceof Player && target.getFusionSkill() != null) {
                target.breakCast();
            } else if (target.isCastingNow() || target.getLastSkillCast() == null || target.getLastSkillCast().isMagic()) {
                double rate = target.calcStat(Stats.ATTACK_CANCEL, (double) 15.0F + Math.sqrt((double) 13.0F * dmg) - (MEN_BONUS[target.getMEN()] * (double) 100.0F - (double) 100.0F), null, null);
                if (rate > (double) 99.0F) {
                    rate = 99.0F;
                } else if (rate < (double) 1.0F) {
                    rate = 1.0F;
                }

                if (Config.DEVELOPER) {
                    LOGGER.info("calcCastBreak rate: {}%.", (int) rate);
                }

                if ((double) Rnd.get(100) < rate) {
                    target.breakCast();
                }

            }
        }
    }

    public static int calcPAtkSpd(Creature attacker, Creature target, double rate) {
        return rate < (double) 2.0F ? 2700 : (int) ((double) 470000.0F / rate);
    }

    public static int calcAtkSpd(Creature attacker, L2Skill skill, double skillTime) {
        return skill.isMagic() ? (int) (skillTime * (double) 333.0F / (double) attacker.getMAtkSpd()) : (int) (skillTime * (double) 333.0F / (double) attacker.getPAtkSpd());
    }

    public static boolean calcHitMiss(Creature attacker, Creature target) {
        int chance = (80 + 2 * (attacker.getAccuracy() - target.getEvasionRate(attacker))) * 10;
        double modifier = 100.0F;
        if (attacker.getZ() - target.getZ() > 50) {
            modifier += 3.0F;
        } else if (attacker.getZ() - target.getZ() < -50) {
            modifier -= 3.0F;
        }

        if (GameTimeTaskManager.getInstance().isNight()) {
            modifier -= 10.0F;
        }

        if (attacker.isBehindTarget()) {
            modifier += 10.0F;
        } else if (!attacker.isInFrontOfTarget()) {
            modifier += 5.0F;
        }

        chance = (int) ((double) chance * (modifier / (double) 100.0F));
        if (Config.DEVELOPER) {
            LOGGER.info("calcHitMiss rate: {}%, modifier : x{}.", chance / 10, modifier / (double) 100.0F);
        }

        return Math.max(Math.min(chance, 980), 200) < Rnd.get(1000);
    }

    public static byte calcShldUse(Creature attacker, Creature target, L2Skill skill) {
        if (skill != null && skill.ignoreShield()) {
            return 0;
        } else {
            Item item = target.getSecondaryWeaponItem();
            if (item instanceof Armor) {
                double shldRate = target.calcStat(Stats.SHIELD_RATE, 0.0F, attacker, null) * DEX_BONUS[target.getDEX()];
                if (shldRate == (double) 0.0F) {
                    return 0;
                } else {
                    int degreeside = (int) target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 120.0F, null, null);
                    if (degreeside < 360 && !target.isFacing(attacker, degreeside)) {
                        return 0;
                    } else {
                        byte shldSuccess = 0;
                        if (attacker.getAttackType() == WeaponType.BOW) {
                            shldRate *= 1.3;
                        }

                        if (shldRate > (double) 0.0F && 100 - Config.PERFECT_SHIELD_BLOCK_RATE < Rnd.get(100)) {
                            shldSuccess = 2;
                        } else if (shldRate > (double) Rnd.get(100)) {
                            shldSuccess = 1;
                        }

                        if (target instanceof Player) {
                            switch (shldSuccess) {
                                case 1 -> ((Player) target).sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
                                case 2 ->
                                        ((Player) target).sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
                            }
                        }

                        return shldSuccess;
                    }
                }
            } else {
                return 0;
            }
        }
    }

    public static boolean calcMagicAffected(Creature actor, Creature target, L2Skill skill) {
        L2SkillType type = skill.getSkillType();
        if (target.isRaidRelated() && !calcRaidAffected(type)) {
            return false;
        } else {
            double defence = 0.0F;
            if (skill.isActive() && skill.isOffensive()) {
                defence = target.getMDef(actor, skill);
            }

            double attack = (double) (2 * actor.getMAtk(target, skill)) * calcSkillVulnerability(actor, target, skill, type);
            double d = (attack - defence) / (attack + defence);
            d += (double) 0.5F * Rnd.nextGaussian();
            return d > (double) 0.0F;
        }
    }

    public static double calcSkillVulnerability(Creature attacker, Creature target, L2Skill skill, L2SkillType type) {
        double multiplier = 1.0F;
        if (skill.getElement() > 0) {
            multiplier *= Math.sqrt(calcElemental(attacker, target, skill));
        }

        multiplier = switch (type) {
            case BLEED -> target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
            case POISON -> target.calcStat(Stats.POISON_VULN, multiplier, target, null);
            case STUN -> target.calcStat(Stats.STUN_VULN, multiplier, target, null);
            case PARALYZE -> target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
            case ROOT -> target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
            case SLEEP -> target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
            case MUTE, FEAR, BETRAY, AGGDEBUFF, AGGREDUCE_CHAR, ERASE, CONFUSION ->
                    target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
            case DEBUFF, WEAKNESS -> target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
            case CANCEL -> target.calcStat(Stats.CANCEL_VULN, multiplier, target, null);
            default -> multiplier;
        };

        return multiplier;
    }

    private static double calcSkillStatModifier(L2SkillType type, Creature target) {
        double multiplier = 1.0F;
        switch (type) {
            case BLEED:
            case POISON:
            case STUN:
                multiplier = (double) 2.0F - SQRT_CON_BONUS[target.getStat().getCON()];
                break;
            case PARALYZE:
            case ROOT:
            case SLEEP:
            case MUTE:
            case FEAR:
            case BETRAY:
            case AGGREDUCE_CHAR:
            case ERASE:
            case CONFUSION:
            case DEBUFF:
            case WEAKNESS:
                multiplier = (double) 2.0F - SQRT_MEN_BONUS[target.getStat().getMEN()];
            case AGGDEBUFF:
        }

        return Math.max(0.0F, multiplier);
    }

    public static double getSTRBonus(Creature activeChar) {
        return STR_BONUS[activeChar.getSTR()];
    }

    private static double getLevelModifier(Creature attacker, Creature target, L2Skill skill) {
        if (skill.getLevelDepend() == 0) {
            return 1.0F;
        } else {
            int delta = (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) + skill.getLevelDepend() - target.getLevel();
            return (double) 1.0F + (delta < 0 ? 0.01 : 0.005) * (double) delta;
        }
    }

    private static double getMatkModifier(Creature attacker, Creature target, L2Skill skill, boolean bss) {
        double mAtkModifier = 1.0F;
        if (skill.isMagic()) {
            double mAtk = attacker.getMAtk(target, skill);
            double val = mAtk;
            if (bss) {
                val = mAtk * (double) 4.0F;
            }

            mAtkModifier = Math.sqrt(val) / (double) target.getMDef(attacker, skill) * (double) 11.0F;
        }

        return mAtkModifier;
    }

    public static boolean calcEffectSuccess(Creature attacker, Creature target, EffectTemplate effect, L2Skill skill, byte shld, boolean bss) {
        if (shld == 2) {
            return false;
        } else {
            L2SkillType type = effect.effectType;
            double baseChance = effect.effectPower;
            if (type == null) {
                return (double) Rnd.get(100) < baseChance;
            } else if (type.equals(L2SkillType.CANCEL)) {
                return true;
            } else {
                double statModifier = calcSkillStatModifier(type, target);
                double skillModifier = calcSkillVulnerability(attacker, target, skill, type);
                double mAtkModifier = getMatkModifier(attacker, target, skill, bss);
                double lvlModifier = getLevelModifier(attacker, target, skill);
                double rate = Math.max(1.0F, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0F));
                int keyId = target instanceof Player ? target.getActingPlayer().getClassId().getId() : (target instanceof Monster ? -1 : -2);
                double multiplier = SkillBalanceManager.getInstance().getSkillValue(skill.getId() + ";" + keyId, SkillChangeType.Chance, target);
                rate *= multiplier;
                if (Config.DEVELOPER) {
                    LOGGER.info("calcEffectSuccess(): name:{} eff.type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.", skill.getName(), type.toString(), baseChance, String.format("%1.2f", statModifier), String.format("%1.2f", skillModifier), String.format("%1.2f", mAtkModifier), String.format("%1.2f", lvlModifier), String.format("%1.2f", rate));
                }

                return (double) Rnd.get(100) < rate;
            }
        }
    }

    public static boolean calcSkillSuccess(Creature attacker, Creature target, L2Skill skill, byte shld, boolean bss) {
        if (shld == 2) {
            return false;
        } else {
            L2SkillType type = skill.getEffectType();
            if (target.isRaidRelated() && !calcRaidAffected(type)) {
                return false;
            } else {
                double baseChance = skill.getEffectPower();
                if (skill.ignoreResists()) {
                    return (double) Rnd.get(100) < baseChance;
                } else {
                    double statModifier = calcSkillStatModifier(type, target);
                    double skillModifier = calcSkillVulnerability(attacker, target, skill, type);
                    double mAtkModifier = getMatkModifier(attacker, target, skill, bss);
                    double lvlModifier = getLevelModifier(attacker, target, skill);
                    double rate = Math.max(1.0F, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0F));
                    if (Config.DEVELOPER) {
                        LOGGER.info("calcSkillSuccess(): name:{} type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.", skill.getName(), skill.getSkillType().toString(), baseChance, String.format("%1.2f", statModifier), String.format("%1.2f", skillModifier), String.format("%1.2f", mAtkModifier), String.format("%1.2f", lvlModifier), String.format("%1.2f", rate));
                    }

                    return (double) Rnd.get(100) < rate;
                }
            }
        }
    }

    public static boolean calcCubicSkillSuccess(Cubic attacker, Creature target, L2Skill skill, byte shld, boolean bss) {
        if (calcSkillReflect(target, skill) != 0) {
            return false;
        } else if (shld == 2) {
            return false;
        } else {
            L2SkillType type = skill.getEffectType();
            if (target.isRaidRelated() && !calcRaidAffected(type)) {
                return false;
            } else {
                double baseChance = skill.getEffectPower();
                if (skill.ignoreResists()) {
                    return (double) Rnd.get(100) < baseChance;
                } else {
                    double mAtkModifier = 1.0F;
                    if (skill.isMagic()) {
                        double mAtk = attacker.getMAtk();
                        double val = mAtk;
                        if (bss) {
                            val = mAtk * (double) 4.0F;
                        }

                        mAtkModifier = Math.sqrt(val) / (double) target.getMDef(null, null) * (double) 11.0F;
                    }

                    double statModifier = calcSkillStatModifier(type, target);
                    double skillModifier = calcSkillVulnerability(attacker.getOwner(), target, skill, type);
                    double lvlModifier = getLevelModifier(attacker.getOwner(), target, skill);
                    double rate = Math.max(1.0F, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0F));
                    if (Config.DEVELOPER) {
                        LOGGER.info("calcCubicSkillSuccess(): name:{} type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.", skill.getName(), skill.getSkillType().toString(), baseChance, String.format("%1.2f", statModifier), String.format("%1.2f", skillModifier), String.format("%1.2f", mAtkModifier), String.format("%1.2f", lvlModifier), String.format("%1.2f", rate));
                    }

                    return (double) Rnd.get(100) < rate;
                }
            }
        }
    }

    public static boolean calcMagicSuccess(Creature attacker, Creature target, L2Skill skill) {
        int lvlDifference = target.getLevel() - ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) + skill.getLevelDepend());
        double rate = 100.0F;
        if (lvlDifference > 0) {
            rate = Math.pow(1.166, lvlDifference) * (double) 100.0F;
        }

        if (attacker instanceof Player && ((Player) attacker).getExpertiseWeaponPenalty()) {
            rate += 6000.0F;
        }

        if (Config.DEVELOPER) {
            LOGGER.info("calcMagicSuccess(): name:{} lvlDiff:{} fail:{}%.", skill.getName(), lvlDifference, String.format("%1.2f", rate / (double) 100.0F));
        }

        rate = Math.min(rate, 9900.0F);
        return (double) Rnd.get(10000) > rate;
    }

    public static double calcManaDam(Creature attacker, Creature target, L2Skill skill, boolean ss, boolean bss) {
        double mAtk = attacker.getMAtk(target, skill);
        double mDef = target.getMDef(attacker, skill);
        double mp = target.getMaxMp();
        if (bss) {
            mAtk *= 4.0F;
        } else if (ss) {
            mAtk *= 2.0F;
        }

        double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * (mp / (double) 97.0F) / mDef;
        damage *= calcSkillVulnerability(attacker, target, skill, skill.getSkillType());
        return damage;
    }

    public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, Creature caster) {
        if (baseRestorePercent != (double) 0.0F && baseRestorePercent != (double) 100.0F) {
            double restorePercent = baseRestorePercent * WIT_BONUS[caster.getWIT()];
            if (restorePercent - baseRestorePercent > (double) 20.0F) {
                restorePercent += 20.0F;
            }

            restorePercent = Math.max(restorePercent, baseRestorePercent);
            restorePercent = Math.min(restorePercent, 90.0F);
            return restorePercent;
        } else {
            return baseRestorePercent;
        }
    }

    public static boolean calcPhysicalSkillEvasion(Creature target, L2Skill skill) {
        if (skill.isMagic()) {
            return false;
        } else {
            return (double) Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0.0F, null, skill);
        }
    }

    public static boolean calcSkillMastery(Creature actor, L2Skill sk) {
        if (!(actor instanceof Player)) {
            return false;
        } else if (sk.getSkillType() == L2SkillType.FISHING) {
            return false;
        } else {
            double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0.0F, null, null);
            if (((Player) actor).isMageClass()) {
                val *= INT_BONUS[actor.getINT()];
            } else {
                val *= STR_BONUS[actor.getSTR()];
            }

            return (double) Rnd.get(100) < val;
        }
    }

    public static double calcValakasAttribute(Creature attacker, Creature target, L2Skill skill) {
        double calcPower = 0.0F;
        double calcDefen = 0.0F;
        if (skill != null && skill.getAttributeName().contains("valakas")) {
            calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
            calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
        } else {
            calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
            if (calcPower > (double) 0.0F) {
                calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
                calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
            }
        }

        return calcPower - calcDefen;
    }

    public static double calcElemental(Creature attacker, Creature target, L2Skill skill) {
        if (skill != null) {
            byte element = skill.getElement();
            return element > 0 ? (double) 1.0F + ((double) attacker.getAttackElementValue(element) / (double) 10.0F / (double) 100.0F - ((double) 1.0F - target.getDefenseElementValue(element))) : (double) 1.0F;
        } else {
            double elemDamage = 0.0F;

            for (byte i = 1; i < 7; ++i) {
                int attackerBonus = attacker.getAttackElementValue(i);
                elemDamage += Math.max(0.0F, (double) attackerBonus - (double) attackerBonus * (target.getDefenseElementValue(i) / (double) 100.0F));
            }

            return elemDamage;
        }
    }

    public static byte calcSkillReflect(Creature target, L2Skill skill) {
        if (!skill.ignoreResists() && skill.canBeReflected()) {
            if (skill.isMagic() || skill.getCastRange() != -1 && skill.getCastRange() <= 40) {
                byte reflect = 0;
                switch (skill.getSkillType()) {
                    case AGGDEBUFF:
                    case BUFF:
                    case REFLECT:
                    case HEAL_PERCENT:
                    case MANAHEAL_PERCENT:
                    case HOT:
                    case CPHOT:
                    case MPHOT:
                    case UNDEAD_DEFENSE:
                    case CONT:
                        return 0;
                    case PDAM:
                    case BLOW:
                    case MDAM:
                    case DEATHLINK:
                    case CHARGEDAM:
                        double venganceChance = target.getStat().calcStat(skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0.0F, target, skill);
                        if (venganceChance > (double) Rnd.get(100)) {
                            reflect = (byte) (reflect | 2);
                        }
                    case AGGREDUCE_CHAR:
                    case ERASE:
                    case CONFUSION:
                    case DEBUFF:
                    case WEAKNESS:
                    case CANCEL:
                    default:
                        double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0.0F, null, skill);
                        if ((double) Rnd.get(100) < reflectChance) {
                            reflect = (byte) (reflect | 1);
                        }

                        return reflect;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static double calcFallDam(Creature actor, int fallHeight) {
        return Config.ENABLE_FALLING_DAMAGE && fallHeight >= 0 ? actor.calcStat(Stats.FALL, fallHeight * actor.getMaxHp() / 1000, null, null) : (double) 0.0F;
    }

    public static boolean calcRaidAffected(L2SkillType type) {
        switch (type) {
            case STUN:
            case PARALYZE:
            case ROOT:
            case SLEEP:
            case MUTE:
            case FEAR:
            case AGGDEBUFF:
            case AGGREDUCE_CHAR:
            case CONFUSION:
            case DEBUFF:
                if (Rnd.get(1000) == 1) {
                    return true;
                }
            case BETRAY:
            case ERASE:
            case WEAKNESS:
            case CANCEL:
            case BUFF:
            case REFLECT:
            case HEAL_PERCENT:
            case MANAHEAL_PERCENT:
            case HOT:
            case CPHOT:
            case MPHOT:
            case UNDEAD_DEFENSE:
            case CONT:
            case PDAM:
            case BLOW:
            case MDAM:
            case DEATHLINK:
            case CHARGEDAM:
            default:
                return false;
            case MANADAM:
            case MDOT:
                return true;
        }
    }

    public static int calculateKarmaLost(int level, long exp) {
        return (int) ((double) exp / karmaMods[level] / (double) 15.0F);
    }

    public static int calculateKarmaGain(int pkCount, boolean isSummon) {
        int result = 14400;
        if (pkCount < 100) {
            result = (int) (((double) (pkCount - 1) * (double) 0.5F + (double) 1.0F) * (double) 60.0F * (double) 4.0F);
        } else if (pkCount < 180) {
            result = (int) (((double) (pkCount + 1) * (double) 0.125F + (double) 37.5F) * (double) 60.0F * (double) 4.0F);
        }

        if (isSummon) {
            result = (pkCount & 3) + result >> 2;
        }

        return result;
    }
}
