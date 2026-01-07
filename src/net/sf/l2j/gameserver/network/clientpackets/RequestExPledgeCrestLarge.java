package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket {
    private int _crestId;

    protected void readImpl() {
        this._crestId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        byte[] data = CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE_LARGE, this._crestId);
        if (data == null)
            return;
        player.sendPacket(new ExPledgeCrestLarge(this._crestId, data));
    }
}
