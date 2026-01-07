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
        if (getExp() + value < 0L)
            return true;
        if (getExp() + value >= getExpForLevel(81))
            value = getExpForLevel(81) - 1L - getExp();
        setExp(getExp() + value);
        byte level = 0;
        for (level = 1; level <= 81; ) {
            if (getExp() >= getExpForLevel(level)) {
                level = (byte) (level + 1);
                continue;
            }
            level = (byte) (level - 1);
        }
        if (level != getLevel())
            addLevel((byte) (level - getLevel()));
        return true;
    }

    public boolean removeExp(long value) {
        if (getExp() - value < 0L)
            value = getExp() - 1L;
        setExp(getExp() - value);
        byte level = 0;
        for (level = 1; level <= 81; ) {
            if (getExp() >= getExpForLevel(level)) {
                level = (byte) (level + 1);
                continue;
            }
            level = (byte) (level - 1);
        }
        if (level != getLevel())
            addLevel((byte) (level - getLevel()));
        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        boolean expAdded = false;
        boolean spAdded = false;
        if (addToExp >= 0L)
            expAdded = addExp(addToExp);
        if (addToSp >= 0)
            spAdded = addSp(addToSp);
        return (expAdded || spAdded);
    }

    public boolean removeExpAndSp(long removeExp, int removeSp) {
        boolean expRemoved = false;
        boolean spRemoved = false;
        if (removeExp > 0L)
            expRemoved = removeExp(removeExp);
        if (removeSp > 0)
            spRemoved = removeSp(removeSp);
        return (expRemoved || spRemoved);
    }

    public boolean addLevel(byte value) {
        if (getLevel() + value > 80)
            if (getLevel() < 80) {
                value = (byte) (80 - getLevel());
            } else {
                return false;
            }
        boolean levelIncreased = (getLevel() + value > getLevel());
        value = (byte) (value + getLevel());
        setLevel(value);
        if (getExp() >= getExpForLevel(getLevel() + 1) || getExpForLevel(getLevel()) > getExp())
            setExp(getExpForLevel(getLevel()));
        if (!levelIncreased)
            return false;
        getActiveChar().getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
        if (Config.LEVEL_REWARDS_ENABLE)
            if (Config.LEVEL_REWARDS.containsKey((int) value)) {
                getActiveChar().sendMessage("You win a level reward. Good work !!.");
                getActiveChar().sendPacket(new CreatureSay(0, 2, "", "You win a level reward. Good work !!."));
                IntIntHolder rewardID = Config.LEVEL_REWARDS.get((int) value);
                ((Player) getActiveChar()).addItem("Level Reward", rewardID.getId(), rewardID.getValue(), getActiveChar(), true);
            }
        return true;
    }

    public boolean addSp(int value) {
        if (value < 0)
            return false;
        int currentSp = getSp();
        if (currentSp == Integer.MAX_VALUE)
            return false;
        if (currentSp > Integer.MAX_VALUE - value)
            value = Integer.MAX_VALUE - currentSp;
        setSp(currentSp + value);
        return true;
    }

    public boolean removeSp(int value) {
        int currentSp = getSp();
        if (currentSp < value)
            value = currentSp;
        setSp(getSp() - value);
        return true;
    }

    public long getExpForLevel(int level) {
        return level;
    }

    public float getMoveSpeed() {
        float baseValue = getBaseMoveSpeed();
        if (getActiveChar().isInsideZone(ZoneId.SWAMP)) {
            SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), SwampZone.class);
            if (zone != null)
                baseValue = (float) (baseValue * (100 + zone.getMoveBonus()) / 100.0D);
        }
        return (float) calcStat(Stats.RUN_SPEED, baseValue, null, null);
    }

    public Playable getActiveChar() {
        return (Playable) super.getActiveChar();
    }

    public int getMaxLevel() {
        return 81;
    }
}
