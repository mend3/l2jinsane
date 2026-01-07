package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.CastleZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.Future;

public class DamageZone extends CastleZoneType {
    private Future<?> _task;

    private int _hpDamage = 200;

    private int _initialDelay = 1000;

    private int _reuseDelay = 5000;

    public DamageZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("hpDamage")) {
            this._hpDamage = Integer.parseInt(value);
        } else if (name.equalsIgnoreCase("initialDelay")) {
            this._initialDelay = Integer.parseInt(value);
        } else if (name.equalsIgnoreCase("reuseDelay")) {
            this._reuseDelay = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected boolean isAffected(Creature character) {
        return character instanceof net.sf.l2j.gameserver.model.actor.Playable;
    }

    protected void onEnter(Creature character) {
        if (this._task == null && this._hpDamage > 0) {
            if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress()))
                return;
            synchronized (this) {
                if (this._task == null) {
                    this._task = ThreadPool.scheduleAtFixedRate(() -> {
                        if (this._characters.isEmpty() || this._hpDamage <= 0 || (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress()))) {
                            stopTask();
                            return;
                        }
                        for (Creature temp : this._characters.values()) {
                            if (!temp.isDead())
                                temp.reduceCurrentHp(this._hpDamage * (1.0D + temp.calcStat(Stats.DAMAGE_ZONE_VULN, 0.0D, null, null) / 100.0D), null, null);
                        }
                    }, this._initialDelay, this._reuseDelay);
                    if (getCastle() != null)
                        getCastle().getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_TRIPPED), false);
                }
            }
        }
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, true);
            character.sendPacket(new EtcStatusUpdate((Player) character));
        }
    }

    protected void onExit(Creature character) {
        if (character instanceof Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, false);
            if (!character.isInsideZone(ZoneId.DANGER_AREA))
                character.sendPacket(new EtcStatusUpdate((Player) character));
        }
    }

    private void stopTask() {
        if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
