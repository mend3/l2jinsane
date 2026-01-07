package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.RecipeItemMaker;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket {
    private int _recipeId;

    protected void readImpl() {
        this._recipeId = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.MANUFACTURE))
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.getStoreType() == StoreType.MANUFACTURE || player.isCrafting())
            return;
        if (player.isInDuel() || player.isInCombat()) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            return;
        }
        Recipe recipe = RecipeData.getInstance().getRecipeList(this._recipeId);
        if (recipe == null)
            return;
        if (recipe.isDwarven()) {
            if (!player.getDwarvenRecipeBook().contains(recipe))
                return;
        } else if (!player.getCommonRecipeBook().contains(recipe)) {
            return;
        }
        RecipeItemMaker maker = new RecipeItemMaker(player, recipe, player);
        if (maker._isValid)
            maker.run();
    }
}
