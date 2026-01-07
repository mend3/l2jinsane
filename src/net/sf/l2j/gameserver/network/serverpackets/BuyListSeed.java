package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.SeedProduction;

import java.util.ArrayList;
import java.util.List;

public final class BuyListSeed extends L2GameServerPacket {
    private final int _manorId;

    private final List<SeedProduction> _list;

    private final int _money;

    public BuyListSeed(int currentMoney, int castleId) {
        this._money = currentMoney;
        this._manorId = castleId;
        this._list = new ArrayList<>();
        for (SeedProduction s : CastleManorManager.getInstance().getSeedProduction(castleId, false)) {
            if (s.getAmount() > 0 && s.getPrice() > 0)
                this._list.add(s);
        }
    }

    protected void writeImpl() {
        writeC(232);
        writeD(this._money);
        writeD(this._manorId);
        if (!this._list.isEmpty()) {
            writeH(this._list.size());
            for (SeedProduction s : this._list) {
                writeH(4);
                writeD(s.getId());
                writeD(s.getId());
                writeD(s.getAmount());
                writeH(4);
                writeH(0);
                writeD(s.getPrice());
            }
        }
    }
}
