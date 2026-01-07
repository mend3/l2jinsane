package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;

public class FriendStatus extends L2GameServerPacket {
    private final boolean _online;

    private final int _objid;

    private final String _name;

    public FriendStatus(int objId) {
        this._objid = objId;
        this._name = PlayerInfoTable.getInstance().getPlayerName(objId);
        this._online = (World.getInstance().getPlayer(objId) != null);
    }

    protected final void writeImpl() {
        writeC(123);
        writeD(this._online ? 1 : 0);
        writeS(this._name);
        writeD(this._objid);
    }
}
