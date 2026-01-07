package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

import java.util.ArrayList;
import java.util.List;

public class RequestSetCrop extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 13;

    private int _manorId;

    private List<CropProcure> _items;

    protected void readImpl() {
        this._manorId = readD();
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 13 != this._buf.remaining())
            return;
        this._items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int itemId = readD();
            int sales = readD();
            int price = readD();
            int type = readC();
            if (itemId < 1 || sales < 0 || price < 0) {
                this._items.clear();
                return;
            }
            if (sales > 0)
                this._items.add(new CropProcure(itemId, sales, type, sales, price));
        }
    }

    protected void runImpl() {
        if (this._items.isEmpty())
            return;
        CastleManorManager manor = CastleManorManager.getInstance();
        if (!manor.isModifiablePeriod()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        Player player = getClient().getPlayer();
        if (player == null || player.getClan() == null || player.getClan().getCastleId() != this._manorId || (player.getClanPrivileges() & 0x10000) != 65536 || !player.getCurrentFolk().canInteract(player)) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        List<CropProcure> list = new ArrayList<>(this._items.size());
        for (CropProcure cp : this._items) {
            Seed s = manor.getSeedByCrop(cp.getId(), this._manorId);
            if (s != null && cp.getStartAmount() <= s.getCropLimit() && cp.getPrice() >= s.getCropMinPrice() && cp.getPrice() <= s.getCropMaxPrice())
                list.add(cp);
        }
        manor.setNextCropProcure(list, this._manorId);
    }
}
