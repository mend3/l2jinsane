package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestSetAllyCrest extends L2GameClientPacket {
    private int _length;

    private byte[] _data;

    protected void readImpl() {
        this._length = readD();
        if (this._length > 192)
            return;
        this._data = new byte[this._length];
        readB(this._data);
    }

    protected void runImpl() {
        if (this._length < 0 || this._length > 192)
            return;
        Player player = getClient().getPlayer();
        if (player == null || player.getAllyId() == 0)
            return;
        Clan clan = ClanTable.getInstance().getClan(player.getAllyId());
        if (player.getClanId() != clan.getClanId() || !player.isClanLeader())
            return;
        if (this._length == 0 || this._data.length == 0) {
            if (clan.getAllyCrestId() != 0) {
                clan.changeAllyCrest(0, false);
                player.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
            }
        } else {
            int crestId = IdFactory.getInstance().getNextId();
            if (CrestCache.getInstance().saveCrest(CrestCache.CrestType.ALLY, crestId, this._data)) {
                clan.changeAllyCrest(crestId, false);
                player.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
            }
        }
    }
}
