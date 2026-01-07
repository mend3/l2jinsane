package net.sf.l2j.gameserver.handler.bypasshandlers;

import mods.instance.InstanceManager;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.handler.IBypassHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.memo.DungeonMemo;

public class DungeonBypasses implements IBypassHandler {
    public boolean handleBypass(String bypass, Player activeChar) {
        if (bypass.startsWith("bp_reward")) {
            int type = Integer.parseInt(bypass.substring(10));
            int itemId = 0;
            int count = 1;
            switch (type) {
                case 0:
                    itemId = 3470;
                    break;
                case 1:
                    itemId = 3470;
                    break;
                case 2:
                    itemId = 3470;
                    break;
                case 3:
                    itemId = 3470;
                    break;
                case 4:
                    itemId = 3470;
                    break;
                case 5:
                    itemId = 3470;
                    break;
            }
            if (itemId == 0) {
                System.out.println(activeChar.getName() + " tried to send custom id on dungeon solo rewards.");
                return false;
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
            int count = 1;
            switch (type) {
                case 0:
                    itemId = 3470;
                    count = 10;
                    break;
            }
            if (itemId == 0) {
                System.out.println(activeChar.getName() + " tried to send custom id on dungeon party rewards.");
                return false;
            }
            ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
            activeChar.addItem("dungeon reward", itemId, count, null, true);
            if (!item.isStackable())
                DungeonMemo.setVar(activeChar, "delete_temp_item_" + item.getObjectId(), item.getObjectId(), System.currentTimeMillis() + 21600000L);
            activeChar.setInstance(InstanceManager.getInstance().getInstance(0), true);
            activeChar.setDungeon(null);
            activeChar.teleportTo(82635, 148798, -3464, 25);
        }
        return true;
    }

    public String[] getBypassHandlersList() {
        return new String[]{"bp_reward", "bp_party_reward"};
    }
}
