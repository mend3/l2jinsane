package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;

public class ShowMiniMap extends L2GameServerPacket {
    public static final ShowMiniMap REGULAR_MAP = new ShowMiniMap(1665);

    private final int _mapId;

    public ShowMiniMap(int mapId) {
        this._mapId = mapId;
    }

    protected final void writeImpl() {
        writeC(157);
        writeD(this._mapId);
        writeD(SevenSignsManager.getInstance().getCurrentPeriod().ordinal());
    }
}
