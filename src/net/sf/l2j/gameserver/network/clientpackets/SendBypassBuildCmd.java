package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.logging.Logger;

public final class SendBypassBuildCmd extends L2GameClientPacket {
    private static final Logger GMAUDIT_LOG = Logger.getLogger("gmaudit");

    private String _command;

    protected void readImpl() {
        this._command = readS();
        if (this._command != null)
            this._command = this._command.trim();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (EngineModsManager.onVoiced(player, "admin_" + this._command))
            return;
        String command = "admin_" + this._command.split(" ")[0];
        IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
        if (ach == null) {
            if (player.isGM())
                player.sendMessage("The command " + command.substring(6) + " doesn't exist.");
            LOGGER.warn("No handler registered for admin command '{}'.", command);
            return;
        }
        if (!AdminData.getInstance().hasAccess(command, player.getAccessLevel())) {
            player.sendMessage("You don't have the access right to use this command.");
            LOGGER.warn("{} tried to use admin command '{}', but has no access to use it.", player.getName(), command);
            return;
        }
        if (Config.GMAUDIT)
            GMAUDIT_LOG.info(player.getName() + " [" + player.getName() + "] used '" + player.getObjectId() + "' command on: " + this._command);
        ach.useAdminCommand("admin_" + this._command, player);
    }
}
