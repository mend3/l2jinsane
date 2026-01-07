package net.sf.l2j.gameserver.model.actor.stat;

import enginemods.main.data.PlayerData;
import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ClassMaster;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.player.Experience;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.zone.type.SwampZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.Map;

public class PlayerStat extends PlayableStat {
    private int _oldMaxHp;

    private int _oldMaxMp;

    private int _oldMaxCp;

    public PlayerStat(Player activeChar) {
        super(activeChar);
    }

    public boolean addExp(long value) {
        if (!getActiveChar().getAccessLevel().canGainExp())
            return false;
        if (!super.addExp(value))
            return false;
        getActiveChar().sendPacket(new UserInfo(getActiveChar()));
        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        SystemMessage sm;
        if (!super.addExpAndSp(addToExp, addToSp))
            return false;
        if (addToExp == 0L && addToSp > 0) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp);
        } else if (addToExp > 0L && addToSp == 0) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp);
        } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp);
        }
        getActiveChar().sendPacket(sm);
        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
        if (!getActiveChar().getAccessLevel().canGainExp())
            return false;
        if (getActiveChar().hasPet()) {
            Pet pet = (Pet) getActiveChar().getSummon();
            if (pet.getStat().getExp() <= pet.getTemplate().getPetDataEntry(81).getMaxExp() + 10000L && !pet.isDead())
                if (MathUtil.checkIfInShortRadius(Config.PARTY_RANGE, pet, getActiveChar(), true)) {
                    int ratio = pet.getPetData().getExpType();
                    long petExp = 0L;
                    int petSp = 0;
                    if (ratio == -1) {
                        RewardInfo r = rewards.get(pet);
                        RewardInfo reward = rewards.get(getActiveChar());
                        if (r != null && reward != null) {
                            double damageDoneByPet = r.getDamage() / reward.getDamage();
                            petExp = (long) (addToExp * damageDoneByPet);
                            petSp = (int) (addToSp * damageDoneByPet);
                        }
                    } else {
                        if (ratio > 100)
                            ratio = 100;
                        petExp = Math.round(addToExp * (1.0D - ratio / 100.0D));
                        petSp = (int) Math.round(addToSp * (1.0D - ratio / 100.0D));
                    }
                    addToExp -= petExp;
                    addToSp -= petSp;
                    pet.addExpAndSp(petExp, petSp);
                }
        }
        return addExpAndSp(addToExp, addToSp);
    }

    public boolean removeExpAndSp(long removeExp, int removeSp) {
        return removeExpAndSp(removeExp, removeSp, true);
    }

    public boolean removeExpAndSp(long removeExp, int removeSp, boolean sendMessage) {
        int oldLevel = getLevel();
        if (!super.removeExpAndSp(removeExp, removeSp))
            return false;
        if (sendMessage) {
            if (removeExp > 0L)
                getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) removeExp));
            if (removeSp > 0)
                getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(removeSp));
            if (getLevel() < oldLevel)
                getActiveChar().broadcastStatusUpdate();
        }
        return true;
    }

    public final boolean addLevel(byte value) {
        if (getLevel() + value > 80)
            return false;
        boolean levelIncreased = super.addLevel(value);
        if (levelIncreased) {
            if (!Config.DISABLE_TUTORIAL) {
                QuestState qs = getActiveChar().getQuestState("Tutorial");
                if (qs != null)
                    qs.getQuest().notifyEvent("CE40", null, getActiveChar());
            }
            getActiveChar().setCurrentCp(getMaxCp());
            getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
            getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
            ClassMaster.showQuestionMark(getActiveChar());
        }
        getActiveChar().giveSkills();
        Clan clan = getActiveChar().getClan();
        if (clan != null) {
            ClanMember member = clan.getClanMember(getActiveChar().getObjectId());
            if (member != null)
                member.refreshLevel();
            clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
        }
        Party party = getActiveChar().getParty();
        if (party != null)
            party.recalculateLevel();
        getActiveChar().refreshOverloaded();
        getActiveChar().refreshExpertisePenalty();
        getActiveChar().sendPacket(new UserInfo(getActiveChar()));
        return levelIncreased;
    }

    public final long getExpForLevel(int level) {
        return Experience.LEVEL[level];
    }

    public final Player getActiveChar() {
        return (Player) super.getActiveChar();
    }

    public final long getExp() {
        if (getActiveChar().isSubClassActive())
            return getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).getExp();
        return super.getExp();
    }

    public final void setExp(long value) {
        if (getActiveChar().isSubClassActive()) {
            getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).setExp(value);
        } else {
            super.setExp(value);
        }
    }

    public final byte getLevel() {
        if (getActiveChar().isSubClassActive())
            return getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).getLevel();
        return super.getLevel();
    }

    public final void setLevel(byte value) {
        if (value > 80)
            value = 80;
        if (getActiveChar().isSubClassActive()) {
            getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).setLevel(value);
        } else {
            super.setLevel(value);
        }
    }

    public final int getMaxCp() {
        int val = (int) calcStat(Stats.MAX_CP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()), null, null);
        if (val != this._oldMaxCp) {
            this._oldMaxCp = val;
            if (getActiveChar().getStatus().getCurrentCp() != val)
                getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
        }
        return val;
    }

    public final int getMaxHp() {
        int val = super.getMaxHp();
        if (val != this._oldMaxHp) {
            this._oldMaxHp = val;
            if (getActiveChar().getStatus().getCurrentHp() != val)
                getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp());
        }
        return val;
    }

    public final int getMaxMp() {
        int val = super.getMaxMp();
        if (val != this._oldMaxMp) {
            this._oldMaxMp = val;
            if (getActiveChar().getStatus().getCurrentMp() != val)
                getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp());
        }
        return val;
    }

    public final int getSp() {
        if (getActiveChar().isSubClassActive())
            return getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).getSp();
        return super.getSp();
    }

    public final void setSp(int value) {
        if (getActiveChar().isSubClassActive()) {
            getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex())).setSp(value);
        } else {
            super.setSp(value);
        }
        StatusUpdate su = new StatusUpdate(getActiveChar());
        su.addAttribute(13, getSp());
        getActiveChar().sendPacket(su);
    }

    public int getBaseRunSpeed() {
        if (getActiveChar().isMounted()) {
            int base = getActiveChar().isFlying() ? getActiveChar().getPetDataEntry().getMountFlySpeed() : getActiveChar().getPetDataEntry().getMountBaseSpeed();
            if (getActiveChar().getLevel() < getActiveChar().getMountLevel())
                base /= 2;
            if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit()))
                base /= 2;
            return base;
        }
        return super.getBaseRunSpeed();
    }

    public int getBaseSwimSpeed() {
        if (getActiveChar().isMounted()) {
            int base = getActiveChar().getPetDataEntry().getMountSwimSpeed();
            if (getActiveChar().getLevel() < getActiveChar().getMountLevel())
                base /= 2;
            if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit()))
                base /= 2;
            return base;
        }
        return getActiveChar().getTemplate().getBaseSwimSpeed();
    }

    public float getMoveSpeed() {
        float baseValue = getActiveChar().isInsideZone(ZoneId.WATER) ? getBaseSwimSpeed() : getBaseMoveSpeed();
        if (getActiveChar().isInsideZone(ZoneId.SWAMP)) {
            SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), SwampZone.class);
            if (zone != null)
                baseValue = (float) (baseValue * (100 + zone.getMoveBonus()) / 100.0D);
        }
        int penalty = getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0)
            baseValue = (float) (baseValue * Math.pow(0.84D, penalty));
        return (float) calcStat(Stats.RUN_SPEED, baseValue, null, null);
    }

    public int getMAtk(Creature target, L2Skill skill) {
        if (getActiveChar().isMounted()) {
            double base = getActiveChar().getPetDataEntry().getMountMAtk();
            if (getActiveChar().getLevel() < getActiveChar().getMountLevel())
                base /= 2.0D;
            return (int) calcStat(Stats.MAGIC_ATTACK, base, null, null);
        }
        return super.getMAtk(target, skill);
    }

    public int getMAtkSpd() {
        double base = 333.0D;
        if (getActiveChar().isMounted())
            if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit()))
                base /= 2.0D;
        int penalty = getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0)
            base *= Math.pow(0.84D, penalty);
        return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
    }

    public int getPAtk(Creature target) {
        if (getActiveChar().isMounted()) {
            double base = getActiveChar().getPetDataEntry().getMountPAtk();
            if (getActiveChar().getLevel() < getActiveChar().getMountLevel())
                base /= 2.0D;
            return (int) calcStat(Stats.POWER_ATTACK, base, null, null);
        }
        return super.getPAtk(target);
    }

    public int getPAtkSpd() {
        if (getActiveChar().isFlying())
            return getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit()) ? 150 : 300;
        if (getActiveChar().isRiding()) {
            int base = getActiveChar().getPetDataEntry().getMountAtkSpd();
            if (getActiveChar().checkFoodState(getActiveChar().getPetTemplate().getHungryLimit()))
                base /= 2;
            return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
        }
        return super.getPAtkSpd();
    }

    public int getEvasionRate(Creature target) {
        int val = super.getEvasionRate(target);
        int penalty = getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0)
            val -= 2 * penalty;
        return val;
    }

    public int getAccuracy() {
        int val = super.getAccuracy();
        if (getActiveChar().getExpertiseWeaponPenalty())
            val -= 20;
        return val;
    }

    public int getPhysicalAttackRange() {
        return (int) calcStat(Stats.POWER_ATTACK_RANGE, getActiveChar().getAttackType().getRange(), null, null);
    }

    public final int getSTR() {
        return (int) calcStat(Stats.STAT_STR, (getActiveChar().getTemplate().getBaseSTR() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_STR)), null, null);
    }

    public final int getDEX() {
        return (int) calcStat(Stats.STAT_DEX, (getActiveChar().getTemplate().getBaseDEX() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_DEX)), null, null);
    }

    public final int getCON() {
        return (int) calcStat(Stats.STAT_CON, (getActiveChar().getTemplate().getBaseCON() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_CON)), null, null);
    }

    public int getINT() {
        return (int) calcStat(Stats.STAT_INT, (getActiveChar().getTemplate().getBaseINT() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_INT)), null, null);
    }

    public final int getMEN() {
        return (int) calcStat(Stats.STAT_MEN, (getActiveChar().getTemplate().getBaseMEN() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_MEN)), null, null);
    }

    public final int getWIT() {
        return (int) calcStat(Stats.STAT_WIT, (getActiveChar().getTemplate().getBaseWIT() + PlayerData.get(getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_WIT)), null, null);
    }
}
