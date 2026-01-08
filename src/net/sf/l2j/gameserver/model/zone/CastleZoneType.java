package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.serverpackets.EventTrigger;

public abstract class CastleZoneType extends ZoneType {
    private int _castleId;
    private Castle _castle;
    private boolean _enabled;
    private int _eventId;

    protected CastleZoneType(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("castleId")) {
            this._castleId = Integer.parseInt(value);
        } else if (name.equals("eventId")) {
            this._eventId = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }

    }

    public void addKnownObject(WorldObject object) {
        if (this._eventId > 0 && this._enabled && object instanceof Player) {
            ((Player) object).sendPacket(new EventTrigger(this.getEventId(), true));
        }

    }

    public void removeKnownObject(WorldObject object) {
        if (this._eventId > 0 && object instanceof Player) {
            ((Player) object).sendPacket(new EventTrigger(this.getEventId(), false));
        }

    }

    public Castle getCastle() {
        if (this._castleId > 0 && this._castle == null) {
            this._castle = CastleManager.getInstance().getCastleById(this._castleId);
        }

        return this._castle;
    }

    public int getEventId() {
        return this._eventId;
    }

    public boolean isEnabled() {
        return this._enabled;
    }

    public void setEnabled(boolean val) {
        this._enabled = val;
        if (this._eventId > 0) {
            WorldRegion region = World.getInstance().getRegion(this);

            for (WorldRegion reg : region.getSurroundingRegions()) {
                for (WorldObject obj : reg.getObjects()) {
                    if (obj instanceof Player) {
                        ((Player) obj).sendPacket(new EventTrigger(this._eventId, val));
                    }
                }
            }
        }

    }
}
