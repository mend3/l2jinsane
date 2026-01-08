package net.sf.l2j.gameserver.handler.bypasshandlers;

import mods.instance.InstanceManager;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.handler.IBypassHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.memo.DungeonMemo;

public class DungeonBypasses implements IBypassHandler {
    public void handleBypass(String bypass, Player activeChar) {
        if (bypass.startsWith("bp_reward")) {
            int type = Integer.parseInt(bypass.substring(10));
            int itemId = 0;
            int count = 1;
            itemId = switch (type) {
                case 0 -> 3470;
                case 1 -> 3470;
                case 2 -> 3470;
                case 3 -> 3470;
                case 4 -> 3470;
                case 5 -> 3470;
                default -> itemId;
            };
            if (itemId == 0) {
                System.out.println(activeChar.getName() + " tried to send custom id on dungeon solo rewards.");
                return;
            }
            ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
            activeChar.addItem("dungeon reward", itemId, count, null, true);
            DungeonMemo.setVar(activeChar, "delete_temp_item_" + item.getObjectId(), item.getObjectId(), System.currentTimeMillis() + 21600000L);
            activeChar.setInstance(InstanceManager.getInstance().getInstance(0), true);
            activeChar.setDungeon(null);
            activeChar.teleportTo(82635, 148798, -3464, 25);
        } else if (bypass.startsWith("bp_party_reward")) {
            int type = Integer.parseInt(bypass.substring(16));
            int itemId = 0;
            int count = switch (type) {
                case 0 -> {
                    itemId = 3470;
                    yield 10;
                }
                default -> 1;
            };
            if (itemId == 0) {
                System.out.println(activeChar.getName() + " tried to send custom id on dungeon party rewards.");
                return;
            }
            ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
            activeChar.addItem("dungeon reward", itemId, count, null, true);
            if (!item.isStackable())
                DungeonMemo.setVar(activeChar, "delete_temp_item_" + item.getObjectId(), item.getObjectId(), System.currentTimeMillis() + 21600000L);
            activeChar.setInstance(InstanceManager.getInstance().getInstance(0), true);
            activeChar.setDungeon(null);
            activeChar.teleportTo(82635, 148798, -3464, 25);
        }
    }

    public String[] getBypassHandlersList() {
        return new String[]{"bp_reward", "bp_party_reward"};
    }
}
