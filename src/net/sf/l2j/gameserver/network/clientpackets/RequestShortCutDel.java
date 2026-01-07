package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestShortCutDel extends L2GameClientPacket {
    private int _slot;

    private int _page;

    protected void readImpl() {
        int id = readD();
        this._slot = id % 12;
        this._page = id / 12;
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._page < 0 || this._page > 9)
            return;
        player.getShortcutList().deleteShortcut(this._slot, this._page);
    }
}
