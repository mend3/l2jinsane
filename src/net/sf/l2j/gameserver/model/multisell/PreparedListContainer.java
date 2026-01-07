package net.sf.l2j.gameserver.model.multisell;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.ArrayList;
import java.util.LinkedList;

public class PreparedListContainer extends ListContainer {
    private int _npcObjectId = 0;

    public PreparedListContainer(ListContainer template, boolean inventoryOnly, Player player, Npc npc) {
        super(template.getId());
        setMaintainEnchantment(template.getMaintainEnchantment());
        setApplyTaxes(false);
        this._npcsAllowed = template._npcsAllowed;
        double taxRate = 0.0D;
        if (npc != null) {
            this._npcObjectId = npc.getObjectId();
            if (template.getApplyTaxes() && npc.getCastle() != null && npc.getCastle().getOwnerId() > 0) {
                setApplyTaxes(true);
                taxRate = npc.getCastle().getTaxRate();
            }
        }
        if (inventoryOnly) {
            ItemInstance[] items;
            if (player == null)
                return;
            if (getMaintainEnchantment()) {
                items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
            } else {
                items = player.getInventory().getUniqueItems(false, false, false);
            }
            this._entries = new LinkedList<>();
            for (ItemInstance item : items) {
                if (!item.isEquipped() && (item.getItem() instanceof net.sf.l2j.gameserver.model.item.kind.Armor || item.getItem() instanceof net.sf.l2j.gameserver.model.item.kind.Weapon))
                    for (Entry ent : template.getEntries()) {
                        for (Ingredient ing : ent.getIngredients()) {
                            if (item.getItemId() == ing.getItemId())
                                this._entries.add(new PreparedEntry(ent, item, getApplyTaxes(), getMaintainEnchantment(), taxRate));
                        }
                    }
            }
        } else {
            this._entries = new ArrayList<>(template.getEntries().size());
            for (Entry ent : template.getEntries())
                this._entries.add(new PreparedEntry(ent, null, getApplyTaxes(), false, taxRate));
        }
    }

    public final boolean checkNpcObjectId(int npcObjectId) {
        return this._npcObjectId == 0 || ((this._npcObjectId == npcObjectId));
    }
}
