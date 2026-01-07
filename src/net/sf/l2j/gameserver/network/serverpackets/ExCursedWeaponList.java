package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Set;

public class ExCursedWeaponList extends L2GameServerPacket {
    private final Set<Integer> _cursedWeaponIds;

    public ExCursedWeaponList(Set<Integer> cursedWeaponIds) {
        this._cursedWeaponIds = cursedWeaponIds;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(69);
        writeD(this._cursedWeaponIds.size());
        for (Iterator<Integer> iterator = this._cursedWeaponIds.iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            writeD(id);
        }
    }
}
