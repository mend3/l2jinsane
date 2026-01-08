package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.zone.type.SwampZone;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class PlayableStat extends CreatureStat {
    public PlayableStat(Playable activeChar) {
        super(activeChar);
    }

    public boolean addExp(long value) {
        if (this.getExp() + value < 0L) {
            return true;
        } else {
            if (this.getExp() + value >= this.getExpForLevel(81)) {
                value = this.getExpForLevel(81) - 1L - this.getExp();
            }

            this.setExp(this.getExp() + value);
            byte level = 0;

            for (level = 1; level <= 81; ++level) {
                if (this.getExp() < this.getExpForLevel(level)) {
                    --level;
                    break;
                }
            }

            if (level != this.getLevel()) {
                this.addLevel((byte) (level - this.getLevel()));
            }

            return true;
        }
    }

    public boolean removeExp(long value) {
        if (this.getExp() - value < 0L) {
            value = this.getExp() - 1L;
        }

        this.setExp(this.getExp() - value);
        byte level = 0;

        for (level = 1; level <= 81; ++level) {
            if (this.getExp() < this.getExpForLevel(level)) {
                --level;
                break;
            }
        }

        if (level != this.getLevel()) {
            this.addLevel((byte) (level - this.getLevel()));
        }

        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        boolean expAdded = false;
        boolean spAdded = false;
        if (addToExp >= 0L) {
            expAdded = this.addExp(addToExp);
        }

        if (addToSp >= 0) {
            spAdded = this.addSp(addToSp);
        }

        return expAdded || spAdded;
    }

    public boolean removeExpAndSp(long removeExp, int removeSp) {
        boolean expRemoved = false;
        boolean spRemoved = false;
        if (removeExp > 0L) {
            expRemoved = this.removeExp(removeExp);
        }

        if (removeSp > 0) {
            spRemoved = this.removeSp(removeSp);
        }

        return expRemoved || spRemoved;
    }

    public boolean addLevel(byte value) {
        if (this.getLevel() + value > 80) {
            if (this.getLevel() >= 80) {
                return false;
            }

            value = (byte) (80 - this.getLevel());
        }

        boolean levelIncreased = this.getLevel() + value > this.getLevel();
        value = (byte) (value + this.getLevel());
        this.setLevel(value);
        if (this.getExp() >= this.getExpForLevel(this.getLevel() + 1) || this.getExpForLevel(this.getLevel()) > this.getExp()) {
            this.setExp(this.getExpForLevel(this.getLevel()));
        }

        if (!levelIncreased) {
            return false;
        } else {
            this.getActiveChar().getStatus().setCurrentHpMp(this.getMaxHp(), this.getMaxMp());
            if (Config.LEVEL_REWARDS_ENABLE && Config.LEVEL_REWARDS.containsKey((int) value)) {
                this.getActiveChar().sendMessage("You win a level reward. Good work !!.");
                this.getActiveChar().sendPacket(new CreatureSay(0, 2, "", "You win a level reward. Good work !!."));
                IntIntHolder rewardID = Config.LEVEL_REWARDS.get((int) value);
                ((Player) this.getActiveChar()).addItem("Level Reward", rewardID.getId(), rewardID.getValue(), this.getActiveChar(), true);
            }

            return true;
        }
    }

    public boolean addSp(int value) {
        if (value < 0) {
            return false;
        } else {
            int currentSp = this.getSp();
            if (currentSp == Integer.MAX_VALUE) {
                return false;
            } else {
                if (currentSp > Integer.MAX_VALUE - value) {
                    value = Integer.MAX_VALUE - currentSp;
                }

                this.setSp(currentSp + value);
                return true;
            }
        }
    }

    public boolean removeSp(int value) {
        int currentSp = this.getSp();
        if (currentSp < value) {
            value = currentSp;
        }

        this.setSp(this.getSp() - value);
        return true;
    }

    public long getExpForLevel(int level) {
        return level;
    }

    public float getMoveSpeed() {
        float baseValue = (float) this.getBaseMoveSpeed();
        if (this.getActiveChar().isInsideZone(ZoneId.SWAMP)) {
            SwampZone zone = ZoneManager.getInstance().getZone(this.getActiveChar(), SwampZone.class);
            if (zone != null) {
                baseValue = (float) ((double) baseValue * ((double) (100 + zone.getMoveBonus()) / (double) 100.0F));
            }
        }

        return (float) this.calcStat(Stats.RUN_SPEED, baseValue, null, null);
    }

    public Playable getActiveChar() {
        return (Playable) super.getActiveChar();
    }

    public int getMaxLevel() {
        return 81;
    }
}
