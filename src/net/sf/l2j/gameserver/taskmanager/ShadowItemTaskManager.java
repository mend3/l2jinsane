package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.OnEquipListener;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowItemTaskManager implements Runnable, OnEquipListener {
    private static final int DELAY = 1;

    private final Map<ItemInstance, Player> _shadowItems = new ConcurrentHashMap<>();

    protected ShadowItemTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static ShadowItemTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public final void run() {
        if (this._shadowItems.isEmpty())
            return;
        for (Map.Entry<ItemInstance, Player> entry : this._shadowItems.entrySet()) {
            ItemInstance item = entry.getKey();
            Player player = entry.getValue();
            int mana = item.decreaseMana(1);
            if (mana == -1) {
                player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(item);
                player.sendPacket(iu);
                player.destroyItem("ShadowItem", item, player, false);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addItemName(item.getItemId()));
                this._shadowItems.remove(item);
                continue;
            }
            if (mana == 59) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addItemName(item.getItemId()));
            } else if (mana == 299) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addItemName(item.getItemId()));
            } else if (mana == 599) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addItemName(item.getItemId()));
            }
            if (mana % 60 == 59) {
                InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(item);
                player.sendPacket(iu);
            }
        }
    }

    public final void onEquip(int slot, ItemInstance item, Playable playable) {
        if (!item.isShadowItem())
            return;
        if (!(playable instanceof Player))
            return;
        this._shadowItems.put(item, (Player) playable);
    }

    public final void onUnequip(int slot, ItemInstance item, Playable actor) {
        if (!item.isShadowItem())
            return;
        this._shadowItems.remove(item);
    }

    public final void remove(Player player) {
        if (this._shadowItems.isEmpty())
            return;
        this._shadowItems.values().removeAll(Collections.singleton(player));
    }

    private static class SingletonHolder {
        protected static final ShadowItemTaskManager INSTANCE = new ShadowItemTaskManager();
    }
}
