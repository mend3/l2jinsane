package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopItemInfo;

public final class RequestRecipeShopMakeInfo extends L2GameClientPacket {
    private int _objectId;

    private int _recipeId;

    protected void readImpl() {
        this._objectId = readD();
        this._recipeId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Player manufacturer = World.getInstance().getPlayer(this._objectId);
        if (manufacturer == null || manufacturer.getStoreType() != StoreType.MANUFACTURE)
            return;
        player.sendPacket(new RecipeShopItemInfo(manufacturer, this._recipeId));
    }
}
