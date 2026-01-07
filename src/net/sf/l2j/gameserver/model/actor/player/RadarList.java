package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.gameserver.model.RadarMarker;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;

import java.util.ArrayList;
import java.util.List;

public final class RadarList {
    private final Player _player;

    private final List<RadarMarker> _markers = new ArrayList<>();

    public RadarList(Player player) {
        this._player = player;
    }

    public void addMarker(int x, int y, int z) {
        this._markers.add(new RadarMarker(x, y, z));
        this._player.sendPacket(new RadarControl(2, 2, x, y, z));
        this._player.sendPacket(new RadarControl(0, 1, x, y, z));
    }

    public void removeMarker(int x, int y, int z) {
        this._markers.remove(new RadarMarker(x, y, z));
        this._player.sendPacket(new RadarControl(1, 1, x, y, z));
    }

    public void loadMarkers() {
        this._player.sendPacket(new RadarControl(2, 2, this._player.getX(), this._player.getY(), this._player.getZ()));
        for (RadarMarker marker : this._markers)
            this._player.sendPacket(new RadarControl(0, 1, marker._x, marker._y, marker._z));
    }

    public void removeAllMarkers() {
        for (RadarMarker marker : this._markers)
            this._player.sendPacket(new RadarControl(2, 2, marker._x, marker._y, marker._z));
        this._markers.clear();
    }
}
