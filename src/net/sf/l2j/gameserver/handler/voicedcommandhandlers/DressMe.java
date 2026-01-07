/**/
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.handler.ICustomByPassHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DressMe implements IVoicedCommandHandler, ICustomByPassHandler {
    private static final String[] _voicedCommands;
    static SimpleDateFormat sdf;

    static {
        _voicedCommands = new String[]{Config.DRESS_ME_COMMAND};
        sdf = new SimpleDateFormat("HH:mm");
    }

    private static void showHtm(Player player) {
        NpcHtmlMessage htm = new NpcHtmlMessage(1);
        String text = HtmCache.getInstance().getHtm("data/html/dressme/index.htm");
        htm.setHtml(text);
        htm.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
        htm.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
        player.sendPacket(htm);
    }

    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if (command.startsWith(Config.DRESS_ME_COMMAND)) {
            showHtm(activeChar);
        }

        return true;
    }

    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }

    public String[] getByPassCommands() {
        return new String[]{"dressme_back"};
    }

    public void handleCommand(String command, Player player, String parameters) {
        DressMe.CommandEnum comm = DressMe.CommandEnum.valueOf(command);
        if (comm != null) {
            switch (comm.ordinal()) {
                case 0:
                    showHtm(player);
                default:
            }
        }
    }

    private enum CommandEnum {
        dressme_back;

        // $FF: synthetic method
        private static DressMe.CommandEnum[] $values() {
            return new DressMe.CommandEnum[]{dressme_back};
        }
    }
}