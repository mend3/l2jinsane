package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class AdminCreateItem implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_itemcreate", "admin_create_item", "admin_create_set", "admin_create_coin", "admin_reward_all"};

    private static void createItem(Player activeChar, Player target, int id, int num, int radius, boolean sendGmMessage) {
        Item template = ItemTable.getInstance().getTemplate(id);
        if (template == null) {
            activeChar.sendMessage("This item doesn't exist.");
            return;
        }
        if (num > 1 && !template.isStackable()) {
            activeChar.sendMessage("This item doesn't stack - Creation aborted.");
            return;
        }
        if (radius > 0) {
            List<Player> players = activeChar.getKnownTypeInRadius(Player.class, radius);
            for (Player obj : players) {
                obj.addItem("Admin", id, num, activeChar, false);
                obj.sendMessage("A GM spawned " + num + " " + template.getName() + " in your inventory.");
            }
            if (sendGmMessage)
                activeChar.sendMessage(players.size() + " players rewarded with " + players.size() + " " + num + " in a " + template.getName() + " radius.");
        } else {
            target.getInventory().addItem("Admin", id, num, target, activeChar);
            if (activeChar != target)
                target.sendMessage("A GM spawned " + num + " " + template.getName() + " in your inventory.");
            if (sendGmMessage)
                activeChar.sendMessage("You have spawned " + num + " " + template.getName() + " (" + id + ") in " + target.getName() + "'s inventory.");
            target.sendPacket(new ItemList(target, true));
        }
    }

    private static int getCoinId(String name) {
        if (name.equalsIgnoreCase("adena"))
            return 57;
        if (name.equalsIgnoreCase("ancientadena"))
            return 5575;
        if (name.equalsIgnoreCase("festivaladena"))
            return 6673;
        return 0;
    }

    public void useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        command = st.nextToken();
        if (command.equals("admin_itemcreate")) {
            AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
        } else if (command.equals("admin_reward_all")) {
            try {
                int id = Integer.parseInt(st.nextToken());
                int count = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                Collection<Player> players = World.getInstance().getPlayers();
                for (Player player : players)
                    createItem(activeChar, player, id, count, 0, false);
                activeChar.sendMessage(players.size() + " players rewarded with " + players.size());
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //reward_all <itemId> [amount]");
            }
            AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
        } else {
            Player target = activeChar;
            if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player)
                target = (Player) activeChar.getTarget();
            switch (command) {
                case "admin_create_item" -> {
                    try {
                        int id = Integer.parseInt(st.nextToken());
                        int count = 1;
                        int radius = 0;
                        if (st.hasMoreTokens()) {
                            count = Integer.parseInt(st.nextToken());
                            if (st.hasMoreTokens())
                                radius = Integer.parseInt(st.nextToken());
                        }
                        createItem(activeChar, target, id, count, radius, true);
                    } catch (Exception e) {
                        activeChar.sendMessage("Usage: //create_item <itemId> [amount] [radius]");
                    }
                    AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
                }
                case "admin_create_coin" -> {
                    try {
                        int id = getCoinId(st.nextToken());
                        if (id <= 0) {
                            activeChar.sendMessage("Usage: //create_coin <name> [amount]");
                            return;
                        }
                        createItem(activeChar, target, id, st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1, 0, true);
                    } catch (Exception e) {
                        activeChar.sendMessage("Usage: //create_coin <name> [amount]");
                    }
                    AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
                }
                case "admin_create_set" -> {
                    if (st.hasMoreTokens())
                        try {
                            ArmorSet set = ArmorSetData.getInstance().getSet(Integer.parseInt(st.nextToken()));
                            if (set == null) {
                                activeChar.sendMessage("This chest has no set.");
                                return;
                            }
                            for (int itemId : set.getSetItemsId()) {
                                if (itemId > 0)
                                    target.getInventory().addItem("Admin", itemId, 1, target, activeChar);
                            }
                            if (set.getShield() > 0)
                                target.getInventory().addItem("Admin", set.getShield(), 1, target, activeChar);
                            activeChar.sendMessage("You have spawned " + set + " in " + target.getName() + "'s inventory.");
                            target.sendPacket(new ItemList(target, true));
                        } catch (Exception e) {
                            activeChar.sendMessage("Usage: //create_set <chestId>");
                        }
                    int i = 0;
                    StringBuilder sb = new StringBuilder();
                    for (ArmorSet set : ArmorSetData.getInstance().getSets()) {
                        boolean isNextLine = (i % 2 == 0);
                        if (isNextLine)
                            sb.append("<tr>");
                        sb.append("<td><a action=\"bypass -h admin_create_set ").append(set.getSetItemsId()[0]).append("\">").append(set).append("</a></td>");
                        if (isNextLine)
                            sb.append("</tr>");
                        i++;
                    }
                    NpcHtmlMessage html = new NpcHtmlMessage(0);
                    html.setFile("data/html/admin/itemsets.htm");
                    html.replace("%sets%", sb.toString());
                    activeChar.sendPacket(html);
                }
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
