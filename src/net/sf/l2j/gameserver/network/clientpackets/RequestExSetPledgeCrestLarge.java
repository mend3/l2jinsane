package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket {
    private int _length;

    private byte[] _data;

    protected void readImpl() {
        this._length = readD();
        if (this._length > 2176)
            return;
        this._data = new byte[this._length];
        readB(this._data);
    }

    protected void runImpl() {
        if (this._length < 0 || this._length > 2176)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Clan clan = player.getClan();
        if (clan == null)
            return;
        if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
            return;
        }
        if ((player.getClanPrivileges() & 0x80) != 128) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (this._length == 0 || this._data.length == 0) {
            if (clan.getCrestLargeId() != 0) {
                clan.changeLargeCrest(0);
                player.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
            }
        } else {
            if (clan.getLevel() < 3) {
                player.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST);
                return;
            }
            int crestId = IdFactory.getInstance().getNextId();
            if (CrestCache.getInstance().saveCrest(CrestCache.CrestType.PLEDGE_LARGE, crestId, this._data)) {
                clan.changeLargeCrest(crestId);
                player.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
            }
        }
    }
}
