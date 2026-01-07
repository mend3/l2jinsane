package net.sf.l2j.gameserver.communitybbs.Custom;

import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.xml.MultisellData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SellList;

import java.util.List;
import java.util.StringTokenizer;

public class ShopBBSManager extends BaseBBSManager {
    public static ShopBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (player.getPvpFlag() > 0) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are pvp flagged.</center></body></html>", player);
            player.sendMessage("You can't use Shop when you are pvp flagged.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInCombat()) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are in combat.</center></body></html>", player);
            player.sendMessage("You can't use Shop when you are in combat.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isDead()) {
            separateAndSend("<html><body><br><br><center>You're dead. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're dead. You can't use Shop.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!player.isInsideZone(ZoneId.PEACE)) {
            separateAndSend("<html><body><br><br><center>You're not in Peace Zone. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're not in Peace Zone. You can't use Shop.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (command.startsWith("_bbsshop;")) {
            player.setIsUsingCMultisell(true);
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            OpenPagBBSManager.getInstance().parseCmd("_bbspag;" + st.nextToken(), player);
            MultisellData.getInstance().separateAndSend(st.nextToken(), player, null, false);
        } else if (command.startsWith("_bbsshopsell;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            OpenPagBBSManager.getInstance().parseCmd("_bbspag;" + st.nextToken(), player);
            player.setIsUsingSellItemCommunity(true);
            List<ItemInstance> items = player.getInventory().getSellableItems();
            player.sendPacket(new SellList(player.getAdena(), items));
        } else {
            super.parseCmd(command, player);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final ShopBBSManager INSTANCE = new ShopBBSManager();
    }
}
