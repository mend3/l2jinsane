package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class RequestRecipeShopMessageSet extends L2GameClientPacket {
    private static final int MAX_MSG_LENGTH = 29;

    private String _name;

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._name != null && this._name.length() > 29)
            return;
        if (player.getCreateList() != null)
            player.getCreateList().setStoreName(this._name);
    }
}
