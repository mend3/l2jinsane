package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.DerbyTrackManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.HistoryInfo;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.Locale;

public class DerbyTrackManagerNpc extends Folk {
    protected static final int[] TICKET_PRICES = new int[]{100, 500, 1000, 5000, 10000, 20000, 50000, 100000};

    public DerbyTrackManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("BuyTicket")) {
            if (DerbyTrackManager.getInstance().getCurrentRaceState() != DerbyTrackManager.RaceState.ACCEPTING_BETS) {
                player.sendPacket(SystemMessageId.MONSRACE_TICKETS_NOT_AVAILABLE);
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            int val = Integer.parseInt(command.substring(10));
            if (val == 0) {
                player.setRace(0, 0);
                player.setRace(1, 0);
            }
            if ((val == 10 && player.getRace(0) == 0) || (val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0))
                val = 0;
            int npcId = getTemplate().getNpcId();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (val < 10) {
                html.setFile(getHtmlPath(npcId, 2));
                for (int i = 0; i < 8; i++) {
                    int n = i + 1;
                    String str = "Mob" + n;
                    html.replace(str, DerbyTrackManager.getInstance().getRunnerName(i));
                }
                String search = "No1";
                if (val == 0) {
                    html.replace(search, "");
                } else {
                    html.replace(search, val);
                    player.setRace(0, val);
                }
            } else if (val < 20) {
                if (player.getRace(0) == 0)
                    return;
                html.setFile(getHtmlPath(npcId, 3));
                html.replace("0place", player.getRace(0));
                String search = "Mob1";
                String replace = DerbyTrackManager.getInstance().getRunnerName(player.getRace(0) - 1);
                html.replace(search, replace);
                search = "0adena";
                if (val == 10) {
                    html.replace(search, "");
                } else {
                    html.replace(search, TICKET_PRICES[val - 11]);
                    player.setRace(1, val - 10);
                }
            } else if (val == 20) {
                if (player.getRace(0) == 0 || player.getRace(1) == 0)
                    return;
                html.setFile(getHtmlPath(npcId, 4));
                html.replace("0place", player.getRace(0));
                String search = "Mob1";
                String replace = DerbyTrackManager.getInstance().getRunnerName(player.getRace(0) - 1);
                html.replace(search, replace);
                search = "0adena";
                int price = TICKET_PRICES[player.getRace(1) - 1];
                html.replace(search, price);
                search = "0tax";
                int tax = 0;
                html.replace(search, tax);
                search = "0total";
                int total = price + tax;
                html.replace(search, total);
            } else {
                if (player.getRace(0) == 0 || player.getRace(1) == 0)
                    return;
                int ticket = player.getRace(0);
                int priceId = player.getRace(1);
                if (!player.reduceAdena("Race", TICKET_PRICES[priceId - 1], this, true))
                    return;
                player.setRace(0, 0);
                player.setRace(1, 0);
                ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4443);
                item.setCount(1);
                item.setEnchantLevel(DerbyTrackManager.getInstance().getRaceNumber());
                item.setCustomType1(ticket);
                item.setCustomType2(TICKET_PRICES[priceId - 1] / 100);
                player.addItem("Race", item, player, false);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(DerbyTrackManager.getInstance().getRaceNumber()).addItemName(4443));
                DerbyTrackManager.getInstance().setBetOnLane(ticket, TICKET_PRICES[priceId - 1], true);
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            html.replace("1race", DerbyTrackManager.getInstance().getRaceNumber());
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (command.equals("ShowOdds")) {
            if (DerbyTrackManager.getInstance().getCurrentRaceState() == DerbyTrackManager.RaceState.ACCEPTING_BETS) {
                player.sendPacket(SystemMessageId.MONSRACE_NO_PAYOUT_INFO);
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(getHtmlPath(getTemplate().getNpcId(), 5));
            for (int i = 0; i < 8; i++) {
                int n = i + 1;
                html.replace("Mob" + n, DerbyTrackManager.getInstance().getRunnerName(i));
                double odd = DerbyTrackManager.getInstance().getOdds().get(i);
                html.replace("Odd" + n, (odd > 0.0D) ? String.format(Locale.ENGLISH, "%.1f", Double.valueOf(odd)) : "&$804;");
            }
            html.replace("1race", DerbyTrackManager.getInstance().getRaceNumber());
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (command.equals("ShowInfo")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(getHtmlPath(getTemplate().getNpcId(), 6));
            for (int i = 0; i < 8; i++) {
                int n = i + 1;
                String search = "Mob" + n;
                html.replace(search, DerbyTrackManager.getInstance().getRunnerName(i));
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (command.equals("ShowTickets")) {
            StringBuilder sb = new StringBuilder();
            for (ItemInstance ticket : player.getInventory().getAllItemsByItemId(4443)) {
                if (ticket.getEnchantLevel() != DerbyTrackManager.getInstance().getRaceNumber())
                    StringUtil.append(sb, "<tr><td><a action=\"bypass -h npc_%objectId%_ShowTicket ", Integer.valueOf(ticket.getObjectId()), "\">", Integer.valueOf(ticket.getEnchantLevel()), " Race Number</a></td><td align=right><font color=\"LEVEL\">", Integer.valueOf(ticket.getCustomType1()), "</font> Number</td><td align=right><font color=\"LEVEL\">", Integer.valueOf(ticket.getCustomType2() * 100), "</font> Adena</td></tr>");
            }
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(getHtmlPath(getTemplate().getNpcId(), 7));
            html.replace("%tickets%", sb.toString());
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else if (command.startsWith("ShowTicket")) {
            int val = Integer.parseInt(command.substring(11));
            if (val == 0) {
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            ItemInstance ticket = player.getInventory().getItemByObjectId(val);
            if (ticket == null) {
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            int raceId = ticket.getEnchantLevel();
            int lane = ticket.getCustomType1();
            int bet = ticket.getCustomType2() * 100;
            HistoryInfo info = DerbyTrackManager.getInstance().getHistoryInfo(raceId);
            if (info == null) {
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(getHtmlPath(getTemplate().getNpcId(), 8));
            html.replace("%raceId%", raceId);
            html.replace("%lane%", lane);
            html.replace("%bet%", bet);
            html.replace("%firstLane%", info.getFirst() + 1);
            html.replace("%odd%", (lane == info.getFirst() + 1) ? String.format(Locale.ENGLISH, "%.2f", Double.valueOf(info.getOddRate())) : "0.01");
            html.replace("%objectId%", getObjectId());
            html.replace("%ticketObjectId%", val);
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            if (command.startsWith("CalculateWin")) {
                int val = Integer.parseInt(command.substring(13));
                if (val == 0) {
                    super.onBypassFeedback(player, "Chat 0");
                    return;
                }
                ItemInstance ticket = player.getInventory().getItemByObjectId(val);
                if (ticket == null) {
                    super.onBypassFeedback(player, "Chat 0");
                    return;
                }
                int raceId = ticket.getEnchantLevel();
                int lane = ticket.getCustomType1();
                int bet = ticket.getCustomType2() * 100;
                HistoryInfo info = DerbyTrackManager.getInstance().getHistoryInfo(raceId);
                if (info == null) {
                    super.onBypassFeedback(player, "Chat 0");
                    return;
                }
                if (player.destroyItem("MonsterTrack", ticket, this, true))
                    player.addAdena("MonsterTrack", (int) (bet * ((lane == info.getFirst() + 1) ? info.getOddRate() : 0.01D)), this, true);
                super.onBypassFeedback(player, "Chat 0");
                return;
            }
            if (command.equals("ViewHistory")) {
                StringBuilder sb = new StringBuilder();
                int raceNumber = DerbyTrackManager.getInstance().getRaceNumber();
                for (HistoryInfo info : DerbyTrackManager.getInstance().getLastHistoryEntries()) {
                    StringUtil.append(sb, "<tr><td><font color=\"LEVEL\">", Integer.valueOf(info.getRaceId()), "</font> th</td><td><font color=\"LEVEL\">", Integer.valueOf((raceNumber == info.getRaceId()) ? 0 : (info.getFirst() + 1)), "</font> Lane </td><td><font color=\"LEVEL\">", Integer.valueOf((raceNumber == info.getRaceId()) ? 0 : (info.getSecond() + 1)), "</font> Lane</td><td align=right><font color=00ffff>", String.format(Locale.ENGLISH, "%.2f", Double.valueOf(info.getOddRate())), "</font> Times</td></tr>");
                }
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile(getHtmlPath(getTemplate().getNpcId(), 9));
                html.replace("%infos%", sb.toString());
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else {
                super.onBypassFeedback(player, command);
            }
        }
    }

    public void addKnownObject(WorldObject object) {
        if (object instanceof Player)
            ((Player) object).sendPacket(DerbyTrackManager.getInstance().getRacePacket());
    }

    public void removeKnownObject(WorldObject object) {
        super.removeKnownObject(object);
        if (object instanceof Player player) {
            for (Npc npc : DerbyTrackManager.getInstance().getRunners())
                player.sendPacket(new DeleteObject(npc));
        }
    }
}
