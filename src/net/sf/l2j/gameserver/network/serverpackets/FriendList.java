package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.ArrayList;
import java.util.List;

public class FriendList extends L2GameServerPacket {
    private final List<FriendInfo> _info;

    public FriendList(Player player) {
        this._info = new ArrayList<>(player.getFriendList().size());
        for (int objId : player.getFriendList()) {
            String name = PlayerInfoTable.getInstance().getPlayerName(objId);
            Player player1 = World.getInstance().getPlayer(objId);
            this._info.add(new FriendInfo(objId, name, (player1 != null && player1.isOnline())));
        }
    }

    protected final void writeImpl() {
        writeC(250);
        writeD(this._info.size());
        for (FriendInfo info : this._info) {
            writeD(info._objId);
            writeS(info._name);
            writeD(info._online ? 1 : 0);
            writeD(info._online ? info._objId : 0);
        }
    }

    private record FriendInfo(int _objId, String _name, boolean _online) {
    }
}
