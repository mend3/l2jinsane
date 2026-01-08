package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.location.Location;

import java.util.List;

public class ExCursedWeaponLocation extends L2GameServerPacket {
    private final List<CursedWeaponInfo> _cursedWeaponInfo;

    public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo) {
        this._cursedWeaponInfo = cursedWeaponInfo;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(70);
        if (!this._cursedWeaponInfo.isEmpty()) {
            writeD(this._cursedWeaponInfo.size());
            for (CursedWeaponInfo w : this._cursedWeaponInfo) {
                writeD(w.id);
                writeD(w.activated);
                writeD(w.pos.getX());
                writeD(w.pos.getY());
                writeD(w.pos.getZ());
            }
        } else {
            writeD(0);
            writeD(0);
        }
    }

    public record CursedWeaponInfo(Location pos, int id, int activated) {
    }
}
