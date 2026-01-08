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
        if (!this.getActiveChar().getAccessLevel().canGainExp()) {
            return false;
        } else if (!super.addExp(value)) {
            return false;
        } else {
            this.getActiveChar().sendPacket(new UserInfo(this.getActiveChar()));
            return true;
        }
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        if (!super.addExpAndSp(addToExp, addToSp)) {
            return false;
        } else {
            SystemMessage sm;
            if (addToExp == 0L && addToSp > 0) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp);
            } else if (addToExp > 0L && addToSp == 0) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp);
            } else {
                sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp);
            }

            this.getActiveChar().sendPacket(sm);
            return true;
        }
    }

    public void addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
        if (!this.getActiveChar().getAccessLevel().canGainExp()) {
        } else {
            if (this.getActiveChar().hasPet()) {
                Pet pet = (Pet) this.getActiveChar().getSummon();
                if (pet.getStat().getExp() <= pet.getTemplate().getPetDataEntry(81).getMaxExp() + 10000L && !pet.isDead() && MathUtil.checkIfInShortRadius(Config.PARTY_RANGE, pet, this.getActiveChar(), true)) {
                    int ratio = pet.getPetData().getExpType();
                    long petExp = 0L;
                    int petSp = 0;
                    if (ratio == -1) {
                        RewardInfo r = rewards.get(pet);
                        RewardInfo reward = rewards.get(this.getActiveChar());
                        if (r != null && reward != null) {
                            double damageDoneByPet = (double) r.getDamage() / (double) reward.getDamage();
                            petExp = (long) ((double) addToExp * damageDoneByPet);
                            petSp = (int) ((double) addToSp * damageDoneByPet);
                        }
                    } else {
                        if (ratio > 100) {
                            ratio = 100;
                        }

                        petExp = Math.round((double) addToExp * ((double) 1.0F - (double) ratio / (double) 100.0F));
                        petSp = (int) Math.round((double) addToSp * ((double) 1.0F - (double) ratio / (double) 100.0F));
                    }

                    addToExp -= petExp;
                    addToSp -= petSp;
                    pet.addExpAndSp(petExp, petSp);
                }
            }

            this.addExpAndSp(addToExp, addToSp);
        }
    }

    public boolean removeExpAndSp(long removeExp, int removeSp) {
        return this.removeExpAndSp(removeExp, removeSp, true);
    }

    public boolean removeExpAndSp(long removeExp, int removeSp, boolean sendMessage) {
        int oldLevel = this.getLevel();
        if (!super.removeExpAndSp(removeExp, removeSp)) {
            return false;
        } else {
            if (sendMessage) {
                if (removeExp > 0L) {
                    this.getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) removeExp));
                }

                if (removeSp > 0) {
                    this.getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(removeSp));
                }

                if (this.getLevel() < oldLevel) {
                    this.getActiveChar().broadcastStatusUpdate();
                }
            }

            return true;
        }
    }

    public final boolean addLevel(byte value) {
        if (this.getLevel() + value > 80) {
            return false;
        } else {
            boolean levelIncreased = super.addLevel(value);
            if (levelIncreased) {
                if (!Config.DISABLE_TUTORIAL) {
                    QuestState qs = this.getActiveChar().getQuestState("Tutorial");
                    if (qs != null) {
                        qs.getQuest().notifyEvent("CE40", null, this.getActiveChar());
                    }
                }

                this.getActiveChar().setCurrentCp(this.getMaxCp());
                this.getActiveChar().broadcastPacket(new SocialAction(this.getActiveChar(), 15));
                this.getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
                ClassMaster.showQuestionMark(this.getActiveChar());
            }

            this.getActiveChar().giveSkills();
            Clan clan = this.getActiveChar().getClan();
            if (clan != null) {
                ClanMember member = clan.getClanMember(this.getActiveChar().getObjectId());
                if (member != null) {
                    member.refreshLevel();
                }

                clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this.getActiveChar()));
            }

            Party party = this.getActiveChar().getParty();
            if (party != null) {
                party.recalculateLevel();
            }

            this.getActiveChar().refreshOverloaded();
            this.getActiveChar().refreshExpertisePenalty();
            this.getActiveChar().sendPacket(new UserInfo(this.getActiveChar()));
            return levelIncreased;
        }
    }

    public final long getExpForLevel(int level) {
        return Experience.LEVEL[level];
    }

    public final Player getActiveChar() {
        return (Player) super.getActiveChar();
    }

    public final long getExp() {
        return this.getActiveChar().isSubClassActive() ? (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).getExp() : super.getExp();
    }

    public final void setExp(long value) {
        if (this.getActiveChar().isSubClassActive()) {
            (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).setExp(value);
        } else {
            super.setExp(value);
        }

    }

    public final byte getLevel() {
        return this.getActiveChar().isSubClassActive() ? (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).getLevel() : super.getLevel();
    }

    public final void setLevel(byte value) {
        if (value > 80) {
            value = 80;
        }

        if (this.getActiveChar().isSubClassActive()) {
            (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).setLevel(value);
        } else {
            super.setLevel(value);
        }

    }

    public final int getMaxCp() {
        int val = (int) this.calcStat(Stats.MAX_CP, this.getActiveChar().getTemplate().getBaseCpMax(this.getActiveChar().getLevel()), null, null);
        if (val != this._oldMaxCp) {
            this._oldMaxCp = val;
            if (this.getActiveChar().getStatus().getCurrentCp() != (double) val) {
                this.getActiveChar().getStatus().setCurrentCp(this.getActiveChar().getStatus().getCurrentCp());
            }
        }

        return val;
    }

    public final int getMaxHp() {
        int val = super.getMaxHp();
        if (val != this._oldMaxHp) {
            this._oldMaxHp = val;
            if (this.getActiveChar().getStatus().getCurrentHp() != (double) val) {
                this.getActiveChar().getStatus().setCurrentHp(this.getActiveChar().getStatus().getCurrentHp());
            }
        }

        return val;
    }

    public final int getMaxMp() {
        int val = super.getMaxMp();
        if (val != this._oldMaxMp) {
            this._oldMaxMp = val;
            if (this.getActiveChar().getStatus().getCurrentMp() != (double) val) {
                this.getActiveChar().getStatus().setCurrentMp(this.getActiveChar().getStatus().getCurrentMp());
            }
        }

        return val;
    }

    public final int getSp() {
        return this.getActiveChar().isSubClassActive() ? (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).getSp() : super.getSp();
    }

    public final void setSp(int value) {
        if (this.getActiveChar().isSubClassActive()) {
            (this.getActiveChar().getSubClasses().get(this.getActiveChar().getClassIndex())).setSp(value);
        } else {
            super.setSp(value);
        }

        StatusUpdate su = new StatusUpdate(this.getActiveChar());
        su.addAttribute(13, this.getSp());
        this.getActiveChar().sendPacket(su);
    }

    public int getBaseRunSpeed() {
        if (this.getActiveChar().isMounted()) {
            int base = this.getActiveChar().isFlying() ? this.getActiveChar().getPetDataEntry().getMountFlySpeed() : this.getActiveChar().getPetDataEntry().getMountBaseSpeed();
            if (this.getActiveChar().getLevel() < this.getActiveChar().getMountLevel()) {
                base /= 2;
            }

            if (this.getActiveChar().checkFoodState(this.getActiveChar().getPetTemplate().getHungryLimit())) {
                base /= 2;
            }

            return base;
        } else {
            return super.getBaseRunSpeed();
        }
    }

    public int getBaseSwimSpeed() {
        if (this.getActiveChar().isMounted()) {
            int base = this.getActiveChar().getPetDataEntry().getMountSwimSpeed();
            if (this.getActiveChar().getLevel() < this.getActiveChar().getMountLevel()) {
                base /= 2;
            }

            if (this.getActiveChar().checkFoodState(this.getActiveChar().getPetTemplate().getHungryLimit())) {
                base /= 2;
            }

            return base;
        } else {
            return this.getActiveChar().getTemplate().getBaseSwimSpeed();
        }
    }

    public float getMoveSpeed() {
        float baseValue = this.getActiveChar().isInsideZone(ZoneId.WATER) ? (float) this.getBaseSwimSpeed() : (float) this.getBaseMoveSpeed();
        if (this.getActiveChar().isInsideZone(ZoneId.SWAMP)) {
            SwampZone zone = ZoneManager.getInstance().getZone(this.getActiveChar(), SwampZone.class);
            if (zone != null) {
                baseValue = (float) ((double) baseValue * ((double) (100 + zone.getMoveBonus()) / (double) 100.0F));
            }
        }

        int penalty = this.getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0) {
            baseValue = (float) ((double) baseValue * Math.pow(0.84, penalty));
        }

        return (float) this.calcStat(Stats.RUN_SPEED, baseValue, null, null);
    }

    public int getMAtk(Creature target, L2Skill skill) {
        if (this.getActiveChar().isMounted()) {
            double base = this.getActiveChar().getPetDataEntry().getMountMAtk();
            if (this.getActiveChar().getLevel() < this.getActiveChar().getMountLevel()) {
                base /= 2.0F;
            }

            return (int) this.calcStat(Stats.MAGIC_ATTACK, base, null, null);
        } else {
            return super.getMAtk(target, skill);
        }
    }

    public int getMAtkSpd() {
        double base = 333.0F;
        if (this.getActiveChar().isMounted() && this.getActiveChar().checkFoodState(this.getActiveChar().getPetTemplate().getHungryLimit())) {
            base /= 2.0F;
        }

        int penalty = this.getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0) {
            base *= Math.pow(0.84, penalty);
        }

        return (int) this.calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
    }

    public int getPAtk(Creature target) {
        if (this.getActiveChar().isMounted()) {
            double base = this.getActiveChar().getPetDataEntry().getMountPAtk();
            if (this.getActiveChar().getLevel() < this.getActiveChar().getMountLevel()) {
                base /= 2.0F;
            }

            return (int) this.calcStat(Stats.POWER_ATTACK, base, null, null);
        } else {
            return super.getPAtk(target);
        }
    }

    public int getPAtkSpd() {
        if (this.getActiveChar().isFlying()) {
            return this.getActiveChar().checkFoodState(this.getActiveChar().getPetTemplate().getHungryLimit()) ? 150 : 300;
        } else if (this.getActiveChar().isRiding()) {
            int base = this.getActiveChar().getPetDataEntry().getMountAtkSpd();
            if (this.getActiveChar().checkFoodState(this.getActiveChar().getPetTemplate().getHungryLimit())) {
                base /= 2;
            }

            return (int) this.calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
        } else {
            return super.getPAtkSpd();
        }
    }

    public int getEvasionRate(Creature target) {
        int val = super.getEvasionRate(target);
        int penalty = this.getActiveChar().getExpertiseArmorPenalty();
        if (penalty > 0) {
            val -= 2 * penalty;
        }

        return val;
    }

    public int getAccuracy() {
        int val = super.getAccuracy();
        if (this.getActiveChar().getExpertiseWeaponPenalty()) {
            val -= 20;
        }

        return val;
    }

    public int getPhysicalAttackRange() {
        return (int) this.calcStat(Stats.POWER_ATTACK_RANGE, this.getActiveChar().getAttackType().getRange(), null, null);
    }

    public final int getSTR() {
        return (int) this.calcStat(Stats.STAT_STR, this.getActiveChar().getTemplate().getBaseSTR() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_STR), null, null);
    }

    public final int getDEX() {
        return (int) this.calcStat(Stats.STAT_DEX, this.getActiveChar().getTemplate().getBaseDEX() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_DEX), null, null);
    }

    public final int getCON() {
        return (int) this.calcStat(Stats.STAT_CON, this.getActiveChar().getTemplate().getBaseCON() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_CON), null, null);
    }

    public int getINT() {
        return (int) this.calcStat(Stats.STAT_INT, this.getActiveChar().getTemplate().getBaseINT() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_INT), null, null);
    }

    public final int getMEN() {
        return (int) this.calcStat(Stats.STAT_MEN, this.getActiveChar().getTemplate().getBaseMEN() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_MEN), null, null);
    }

    public final int getWIT() {
        return (int) this.calcStat(Stats.STAT_WIT, this.getActiveChar().getTemplate().getBaseWIT() + PlayerData.get(this.getActiveChar().getActingPlayer()).getCustomStat(Stats.STAT_WIT), null, null);
    }
}
