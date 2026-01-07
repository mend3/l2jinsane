package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestLinkHtml extends L2GameClientPacket {
    private String _link;

    protected void readImpl() {
        this._link = readS();
    }

    public void runImpl() {
        Player actor = getClient().getPlayer();
        if (actor == null)
            return;
        if (this._link.contains("..") || !this._link.contains(".htm"))
            return;
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile(this._link);
        sendPacket(html);
    }
}
