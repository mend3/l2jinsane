package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.ReceivablePacket;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public abstract class L2GameClientPacket extends ReceivablePacket<GameClient> {
    protected static final CLogger LOGGER = new CLogger(L2GameClientPacket.class.getName());

    protected abstract void readImpl();

    protected abstract void runImpl();

    protected boolean read() {
        if (Config.PACKET_HANDLER_DEBUG)
            LOGGER.info(getType());
        try {
            readImpl();
            return true;
        } catch (Exception e) {
            if (e instanceof java.nio.BufferUnderflowException) {
                getClient().onBufferUnderflow();
                return false;
            }
            LOGGER.error("Failed reading {} for {}. ", e, getType(), getClient().toString());
            return false;
        }
    }

    public void run() {
        try {
            runImpl();
            if (triggersOnActionRequest())
                if (getClient() != null) {
                    Player actor = getClient().getActiveChar();
                    if (actor != null && actor.isSpawnProtected()) {
                        actor.onActionRequest();
                        if (Config.DEBUG_PATH)
                            LOGGER.info("Spawn protection for player " + actor.getName() + " removed by packet: " + getType());
                    }
                }
        } catch (Throwable t) {
            if (getClient() != null) {
                LOGGER.log(new LogRecord(Level.SEVERE, "Client: " + getClient() + " - Failed reading: " + getType() + " ; " + t));
            } else {
                LOGGER.log(new LogRecord(Level.SEVERE, " - Failed reading: " + getType() + " ; " + t));
            }
            if (this instanceof EnterWorld && getClient() != null)
                getClient().closeNow();
        }
    }

    protected final void sendPacket(L2GameServerPacket gsp) {
        if (getClient() != null)
            getClient().sendPacket(gsp);
    }

    public String getType() {
        return "[C] " + getClass().getSimpleName();
    }

    protected boolean triggersOnActionRequest() {
        return true;
    }
}
