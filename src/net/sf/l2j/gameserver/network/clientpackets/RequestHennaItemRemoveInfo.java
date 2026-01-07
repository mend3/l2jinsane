package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.network.serverpackets.HennaItemRemoveInfo;

public final class RequestHennaItemRemoveInfo extends L2GameClientPacket {
    private int _symbolId;

    protected void readImpl() {
        this._symbolId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Henna template = HennaData.getInstance().getHenna(this._symbolId);
        if (template == null)
            return;
        activeChar.sendPacket(new HennaItemRemoveInfo(template, activeChar));
    }
}
