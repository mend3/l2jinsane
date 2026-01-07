package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.GaugeColor;

public class SetupGauge extends L2GameServerPacket {
    private final GaugeColor _color;

    private final int _time;

    private final int _maxTime;

    public SetupGauge(GaugeColor color, int time) {
        this._color = color;
        this._time = time;
        this._maxTime = time;
    }

    public SetupGauge(GaugeColor color, int currentTime, int maxTime) {
        this._color = color;
        this._time = currentTime;
        this._maxTime = maxTime;
    }

    protected final void writeImpl() {
        writeC(109);
        writeD(this._color.ordinal());
        writeD(this._time);
        writeD(this._maxTime);
    }
}
