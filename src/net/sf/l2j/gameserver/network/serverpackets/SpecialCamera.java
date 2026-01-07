package net.sf.l2j.gameserver.network.serverpackets;

public class SpecialCamera extends L2GameServerPacket {
    private final int _id;

    private final int _dist;

    private final int _yaw;

    private final int _pitch;

    private final int _time;

    private final int _duration;

    private final int _turn;

    private final int _rise;

    private final int _widescreen;

    private final int _unknown;

    public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration) {
        this._id = id;
        this._dist = dist;
        this._yaw = yaw;
        this._pitch = pitch;
        this._time = time;
        this._duration = duration;
        this._turn = 0;
        this._rise = 0;
        this._widescreen = 0;
        this._unknown = 0;
    }

    public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk) {
        this._id = id;
        this._dist = dist;
        this._yaw = yaw;
        this._pitch = pitch;
        this._time = time;
        this._duration = duration;
        this._turn = turn;
        this._rise = rise;
        this._widescreen = widescreen;
        this._unknown = unk;
    }

    public void writeImpl() {
        writeC(199);
        writeD(this._id);
        writeD(this._dist);
        writeD(this._yaw);
        writeD(this._pitch);
        writeD(this._time);
        writeD(this._duration);
        writeD(this._turn);
        writeD(this._rise);
        writeD(this._widescreen);
        writeD(this._unknown);
    }
}
