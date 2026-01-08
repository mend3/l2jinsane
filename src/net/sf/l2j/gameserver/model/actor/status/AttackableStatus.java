package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;

public class AttackableStatus extends NpcStatus {
    public AttackableStatus(Attackable activeChar) {
        super(activeChar);
    }

    public final void reduceHp(double value, Creature attacker) {
        this.reduceHp(value, attacker, true, false, false);
    }

    public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
        if (!this.getActiveChar().isDead()) {
            Monster monster = null;
            if (this.getActiveChar() instanceof Monster) {
                monster = (Monster) this.getActiveChar();
                if (value > (double) 0.0F) {
                    if (monster.isOverhit()) {
                        monster.setOverhitValues(attacker, value);
                    } else {
                        monster.overhitEnabled(false);
                    }
                } else {
                    monster.overhitEnabled(false);
                }
            }

            if (attacker != null) {
                this.getActiveChar().addAttacker(attacker);
            }

            super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
            if (monster != null && !monster.isDead()) {
                monster.overhitEnabled(false);
            }

        }
    }

    public Attackable getActiveChar() {
        return (Attackable) super.getActiveChar();
    }
}
