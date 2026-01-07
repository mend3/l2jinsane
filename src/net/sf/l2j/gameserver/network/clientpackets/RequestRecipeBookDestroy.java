package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.RecipeBookItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestRecipeBookDestroy extends L2GameClientPacket {
    private int _recipeId;

    protected void readImpl() {
        this._recipeId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.getStoreType() == StoreType.MANUFACTURE) {
            player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
            return;
        }
        Recipe recipe = RecipeData.getInstance().getRecipeList(this._recipeId);
        if (recipe == null)
            return;
        player.unregisterRecipeList(this._recipeId);
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED).addItemName(recipe.getRecipeId()));
        player.sendPacket(new RecipeBookItemList(player, recipe.isDwarven()));
    }
}
