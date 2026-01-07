package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.location.Location;

public class PlaySound extends L2GameServerPacket {
    private final int _soundType;

    private final String _soundFile;

    private final boolean _bindToObject;

    private final int _objectId;

    private final Location _location;

    private final int _delay;

    public PlaySound(String soundFile) {
        this._soundType = 0;
        this._soundFile = soundFile;
        this._bindToObject = false;
        this._objectId = 0;
        this._location = Location.DUMMY_LOC;
        this._delay = 0;
    }

    public PlaySound(int soundType, String soundFile) {
        this._soundType = soundType;
        this._soundFile = soundFile;
        this._bindToObject = false;
        this._objectId = 0;
        this._location = Location.DUMMY_LOC;
        this._delay = 0;
    }

    public PlaySound(int soundType, String soundFile, WorldObject object) {
        this._soundType = soundType;
        this._soundFile = soundFile;
        this._bindToObject = true;
        this._objectId = object.getObjectId();
        this._location = object.getPosition();
        this._delay = 0;
    }

    public PlaySound(int soundType, String soundFile, boolean bindToObject, int objectId, Location location, int delay) {
        this._soundType = soundType;
        this._soundFile = soundFile;
        this._bindToObject = bindToObject;
        this._objectId = objectId;
        this._location = location;
        this._delay = delay;
    }

    protected final void writeImpl() {
        writeC(152);
        writeD(this._soundType);
        writeS(this._soundFile);
        writeD(this._bindToObject ? 1 : 0);
        writeD(this._objectId);
        writeLoc(this._location);
        writeD(this._delay);
    }
}
