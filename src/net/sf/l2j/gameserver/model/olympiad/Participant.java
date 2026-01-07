package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public final class Participant {
    private final int _objectId;

    private final String _name;

    private final int _side;

    private final int _baseClass;

    private final StatSet _stats;

    private boolean _isDisconnected = false;

    private boolean _isDefecting = false;

    private Player _player;

    public Participant(Player player, int side) {
        this._objectId = player.getObjectId();
        this._player = player;
        this._name = player.getName();
        this._side = side;
        this._baseClass = player.getBaseClass();
        this._stats = Olympiad.getInstance().getNobleStats(this._objectId);
    }

    public Participant(int objectId, int side) {
        this._objectId = objectId;
        this._player = null;
        this._name = "-";
        this._side = side;
        this._baseClass = 0;
        this._stats = null;
    }

    public int getObjectId() {
        return this._objectId;
    }

    public String getName() {
        return this._name;
    }

    public int getSide() {
        return this._side;
    }

    public int getBaseClass() {
        return this._baseClass;
    }

    public StatSet getStats() {
        return this._stats;
    }

    public boolean isDisconnected() {
        return this._isDisconnected;
    }

    public void setDisconnection(boolean isDisconnected) {
        this._isDisconnected = isDisconnected;
    }

    public boolean isDefecting() {
        return this._isDefecting;
    }

    public void setDefection(boolean isDefecting) {
        this._isDefecting = isDefecting;
    }

    public Player getPlayer() {
        return this._player;
    }

    public void setPlayer(Player player) {
        this._player = player;
    }

    public void updatePlayer() {
        if (this._player == null || !this._player.isOnline())
            this._player = World.getInstance().getPlayer(this._objectId);
    }

    public void updateStat(String statName, int increment) {
        this._stats.set(statName, Math.max(this._stats.getInteger(statName) + increment, 0));
    }
}
