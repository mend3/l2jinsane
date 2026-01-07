package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.PunishmentType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Punishment {
    private final Player _owner;

    private PunishmentType _type = PunishmentType.NONE;

    private long _timer;

    private ScheduledFuture<?> _task;

    public Punishment(Player owner) {
        this._owner = owner;
    }

    public Player getOwner() {
        return this._owner;
    }

    public PunishmentType getType() {
        return this._type;
    }

    public void setType(int type) {
        if (type < 0 || type > PunishmentType.VALUES.length)
            return;
        this._type = PunishmentType.VALUES[type];
    }

    public void setType(PunishmentType state, int delayInMinutes) {
        NpcHtmlMessage html;
        switch (state) {
            case NONE:
                switch (this._type) {
                    case CHAT:
                        this._type = state;
                        stopTask(true);
                        this._owner.sendPacket(new EtcStatusUpdate(this._owner));
                        this._owner.sendMessage("Chatting is now available.");
                        this._owner.sendPacket(new PlaySound("systemmsg_e.345"));
                        break;
                    case JAIL:
                        this._type = state;
                        html = new NpcHtmlMessage(0);
                        html.setFile("data/html/jail_out.htm");
                        this._owner.sendPacket(html);
                        stopTask(true);
                        this._owner.teleportTo(17836, 170178, -3507, 20);
                        break;
                }
                break;
            case CHAT:
                if (this._type == PunishmentType.JAIL)
                    break;
                this._type = state;
                this._timer = 0L;
                this._owner.sendPacket(new EtcStatusUpdate(this._owner));
                stopTask(false);
                if (delayInMinutes > 0) {
                    this._timer = delayInMinutes * 60000L;
                    this._task = ThreadPool.schedule(() -> setType(PunishmentType.NONE, 0), this._timer);
                    this._owner.sendMessage("Chatting has been suspended for " + delayInMinutes + " minute(s).");
                } else {
                    this._owner.sendMessage("Chatting has been suspended.");
                }
                this._owner.sendPacket(new PlaySound("systemmsg_e.346"));
                break;
            case JAIL:
                this._type = state;
                this._timer = 0L;
                stopTask(false);
                if (delayInMinutes > 0) {
                    this._timer = delayInMinutes * 60000L;
                    this._task = ThreadPool.schedule(() -> setType(PunishmentType.NONE, 0), this._timer);
                    this._owner.sendMessage("You are jailed for " + delayInMinutes + " minutes.");
                }
                if (OlympiadManager.getInstance().isRegisteredInComp(this._owner))
                    OlympiadManager.getInstance().removeDisconnectedCompetitor(this._owner);
                html = new NpcHtmlMessage(0);
                html.setFile("data/html/jail_in.htm");
                this._owner.sendPacket(html);
                this._owner.setIsIn7sDungeon(false);
                this._owner.teleportTo(-114356, -249645, -2984, 0);
                break;
            case CHAR:
                this._owner.setAccessLevel(-1);
                this._owner.logout(false);
                break;
            case ACC:
                this._owner.setAccountAccesslevel(-100);
                this._owner.logout(false);
                break;
            default:
                this._type = state;
                break;
        }
        this._owner.storeCharBase();
    }

    public long getTimer() {
        return this._timer;
    }

    public void load(int type, long timer) {
        setType(type);
        this._timer = (this._type == PunishmentType.NONE) ? 0L : timer;
    }

    public void handle() {
        if (this._type != PunishmentType.NONE) {
            if (this._timer > 0L) {
                this._task = ThreadPool.schedule(() -> setType(PunishmentType.NONE, 0), this._timer);
                this._owner.sendMessage("You are still " + this._type.getName() + " for " + Math.round((float) this._timer / 60000.0F) + " minutes.");
            }
            if (this._type == PunishmentType.JAIL && !this._owner.isInsideZone(ZoneId.JAIL))
                this._owner.teleportTo(-114356, -249645, -2984, 20);
        }
    }

    public void stopTask(boolean save) {
        if (this._task != null) {
            if (save) {
                long delay = this._task.getDelay(TimeUnit.MILLISECONDS);
                if (delay < 0L)
                    delay = 0L;
                this._timer = delay;
            }
            this._task.cancel(false);
            this._task = null;
        }
    }
}
