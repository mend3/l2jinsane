package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;

public class DoorStat extends CreatureStat {
    private int _upgradeHpRatio = 1;

    public DoorStat(Door activeChar) {
        super(activeChar);
    }

    public Door getActiveChar() {
        return (Door) super.getActiveChar();
    }

    public int getMDef(Creature target, L2Skill skill) {
        double defense = this.getActiveChar().getTemplate().getBaseMDef();
        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DAWN -> defense *= 1.2;
            case DUSK -> defense *= 0.3;
        }

        return (int) defense;
    }

    public int getPDef(Creature target) {
        double defense = this.getActiveChar().getTemplate().getBasePDef();
        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DAWN -> defense *= 1.2;
            case DUSK -> defense *= 0.3;
        }

        return (int) defense;
    }

    public int getMaxHp() {
        return super.getMaxHp() * this._upgradeHpRatio;
    }

    public final int getUpgradeHpRatio() {
        return this._upgradeHpRatio;
    }

    public final void setUpgradeHpRatio(int hpRatio) {
        this._upgradeHpRatio = hpRatio;
    }
}
