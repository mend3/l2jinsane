package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.xml.RecipeData;
import net.sf.l2j.gameserver.enums.actors.StoreType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.RecipeItemMaker;
import net.sf.l2j.gameserver.model.item.Recipe;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestRecipeShopMakeItem extends L2GameClientPacket {
    private int _objectId;

    private int _recipeId;

    private int _unknow;

    protected void readImpl() {
        this._objectId = readD();
        this._recipeId = readD();
        this._unknow = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.MANUFACTURE))
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Player manufacturer = World.getInstance().getPlayer(this._objectId);
        if (manufacturer == null)
            return;
        if (player.isInStoreMode())
            return;
        if (manufacturer.getStoreType() != StoreType.MANUFACTURE)
            return;
        if (player.isCrafting() || manufacturer.isCrafting())
            return;
        if (manufacturer.isInDuel() || player.isInDuel() || manufacturer.isInCombat() || player.isInCombat()) {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            return;
        }
        if (!MathUtil.checkIfInRange(150, player, manufacturer, true))
            return;
        Recipe recipe = RecipeData.getInstance().getRecipeList(this._recipeId);
        if (recipe == null)
            return;
        if (recipe.isDwarven()) {
            if (!manufacturer.getDwarvenRecipeBook().contains(recipe))
                return;
        } else if (!manufacturer.getCommonRecipeBook().contains(recipe)) {
            return;
        }
        RecipeItemMaker maker = new RecipeItemMaker(manufacturer, recipe, player);
        if (maker._isValid)
            maker.run();
    }
}
