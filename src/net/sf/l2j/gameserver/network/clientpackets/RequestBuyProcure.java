package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.ArrayList;
import java.util.List;

public class RequestBuyProcure extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 8;

    private int _manorId;

    private List<IntIntHolder> _items;

    protected void readImpl() {
        this._manorId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 8 != this._buf.remaining())
            return;
        this._items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            readD();
            int itemId = readD();
            int cnt = readD();
            if (itemId < 1 || cnt < 1) {
                this._items = null;
                return;
            }
            this._items.add(new IntIntHolder(itemId, cnt));
        }
    }

    protected void runImpl() {
        LOGGER.warn("RequestBuyProcure: normally unused, but infos found for manorId {}.", this._manorId);
    }
}
