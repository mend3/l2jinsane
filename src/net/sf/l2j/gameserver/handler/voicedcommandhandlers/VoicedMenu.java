package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.events.bossevent.NextBossEvent;
import net.sf.l2j.gameserver.events.eventengine.NextEventsInfo;
import net.sf.l2j.gameserver.events.partyfarm.InitialPartyFarm;
import net.sf.l2j.gameserver.events.pvpevent.PvPEventNext;
import net.sf.l2j.gameserver.events.tournament.ArenaEvent;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedMenu implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = new String[]{"menu", "EventTime", "setPartyRefuse", "setTradeRefuse", "setbuffsRefuse", "setMessageRefuse", "showRegisteHtml", "showInfoHtml"};

    private static final String ACTIVED = "<font color=00FF00>ON</font>";

    private static final String DESATIVED = "<font color=FF0000>OFF</font>";

    private static void showHtml(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/menu.htm");
        html.replace("%online%", World.getInstance().getPlayers().size());
        html.replace("%partyRefusal%", activeChar.isPartyInRefuse() ? "<font color=00FF00>ON</font>" : "<font color=FF0000>OFF</font>");
        html.replace("%tradeRefusal%", activeChar.getTradeRefusal() ? "<font color=00FF00>ON</font>" : "<font color=FF0000>OFF</font>");
        html.replace("%buffsRefusal%", activeChar.isBuffProtected() ? "<font color=00FF00>ON</font>" : "<font color=FF0000>OFF</font>");
        html.replace("%messageRefusal%", activeChar.isInRefusalMode() ? "<font color=00FF00>ON</font>" : "<font color=FF0000>OFF</font>");
        html.replace("%html_Party%", activeChar.isPartyInRefuse() ? "<img src=\"l2ui.CheckBox_checked\" width=\"16\" height=\"16\">" : "<img src=\"l2ui.CheckBox\" width=\"16\" height=\"16\">");
        html.replace("%html_trade%", activeChar.getTradeRefusal() ? "<img src=\"l2ui.CheckBox_checked\" width=\"16\" height=\"16\">" : "<img src=\"l2ui.CheckBox\" width=\"16\" height=\"16\">");
        html.replace("%html_Buffs%", activeChar.isBuffProtected() ? "<img src=\"l2ui.CheckBox_checked\" width=\"16\" height=\"16\">" : "<img src=\"l2ui.CheckBox\" width=\"16\" height=\"16\">");
        html.replace("%html_Message%", activeChar.isInRefusalMode() ? "<img src=\"l2ui.CheckBox_checked\" width=\"16\" height=\"16\">" : "<img src=\"l2ui.CheckBox\" width=\"16\" height=\"16\">");
        activeChar.sendPacket(html);
    }

    private static void showEventTimeHtml(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/EventTime.htm");
        html.replace("%tvtTime%", NextEventsInfo.getInstance().NextTvtEvent());
        html.replace("%ctfTime%", NextEventsInfo.getInstance().NextCtfEvent());
        html.replace("%dmTime%", NextEventsInfo.getInstance().NextDMEvent());
        html.replace("%arenaTime%", ArenaEvent.getInstance().NextArenaEvent());
        html.replace("%PartyZoneTime%", InitialPartyFarm.getInstance().getRestartNextTime());
        html.replace("%KtbEventTime%", NextBossEvent.getInstance().NextKTBEvent());
        html.replace("%pvpEventTime%", PvPEventNext.getInstance().NextPvPEvent());
        activeChar.sendPacket(html);
    }

    private static void showRegisterHtml(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/Register.htm");
        html.replace("%tvtTime%", NextEventsInfo.getInstance().NextTvtEvent());
        html.replace("%ctfTime%", NextEventsInfo.getInstance().NextCtfEvent());
        html.replace("%dmTime%", NextEventsInfo.getInstance().NextDMEvent());
        html.replace("%arenaTime%", ArenaEvent.getInstance().NextArenaEvent());
        html.replace("%PartyZoneTime%", InitialPartyFarm.getInstance().getRestartNextTime());
        html.replace("%KtbEventTime%", NextBossEvent.getInstance().NextKTBEvent());
        html.replace("%pvpEventTime%", PvPEventNext.getInstance().NextPvPEvent());
        activeChar.sendPacket(html);
    }

    private static void showInfoHtml(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/Info.htm");
        html.replace("%tvtTime%", NextEventsInfo.getInstance().NextTvtEvent());
        html.replace("%ctfTime%", NextEventsInfo.getInstance().NextCtfEvent());
        html.replace("%dmTime%", NextEventsInfo.getInstance().NextDMEvent());
        html.replace("%arenaTime%", ArenaEvent.getInstance().NextArenaEvent());
        html.replace("%PartyZoneTime%", InitialPartyFarm.getInstance().getRestartNextTime());
        html.replace("%KtbEventTime%", NextBossEvent.getInstance().NextKTBEvent());
        html.replace("%pvpEventTime%", PvPEventNext.getInstance().NextPvPEvent());
        activeChar.sendPacket(html);
    }

    public void useVoicedCommand(String command, Player activeChar, String target) {
        if (command.equals("menu")) {
            showHtml(activeChar);
        } else if (command.equals("setPartyRefuse")) {
            if (activeChar.isPartyInRefuse()) {
                activeChar.setIsPartyInRefuse(false);
                activeChar.sendMessage("Use Party is disabled.");
            } else {
                activeChar.setIsPartyInRefuse(true);
                activeChar.sendMessage("Use Party is enabled.");
            }
            showHtml(activeChar);
        } else if (command.equals("setTradeRefuse")) {
            if (activeChar.getTradeRefusal()) {
                activeChar.setTradeRefusal(false);
                activeChar.sendMessage("Use Trade is disabled.");
            } else {
                activeChar.setTradeRefusal(true);
                activeChar.sendMessage("Use Trade is enabled.");
            }
            showHtml(activeChar);
        } else if (command.equals("setMessageRefuse")) {
            if (activeChar.isInRefusalMode()) {
                activeChar.setInRefusalMode(false);
                activeChar.sendMessage("Use Message is disabled.");
            } else {
                activeChar.setInRefusalMode(true);
                activeChar.sendMessage("Use Message is enabled.");
            }
            showHtml(activeChar);
        } else if (command.equals("setbuffsRefuse")) {
            if (activeChar.isBuffProtected()) {
                activeChar.setIsBuffProtected(false);
                activeChar.useMagic(SkillTable.getInstance().getInfo(5221, 1), false, false);
            } else {
                activeChar.setIsBuffProtected(true);
                activeChar.useMagic(SkillTable.getInstance().getInfo(5221, 1), true, true);
            }
            showHtml(activeChar);
        } else if (command.startsWith("EventTime")) {
            showEventTimeHtml(activeChar);
        } else if (command.startsWith("showRegisteHtml")) {
            showRegisterHtml(activeChar);
        } else if (command.startsWith("showInfoHtml")) {
            showInfoHtml(activeChar);
        }
    }

    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
