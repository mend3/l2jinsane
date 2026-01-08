package net.sf.l2j.gameserver.handler;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.handler.bypasshandlers.DailyRewardCBBypasses;
import net.sf.l2j.gameserver.handler.bypasshandlers.DungeonBypasses;

import java.util.HashMap;
import java.util.Map;

public class BypassHandler {
    private static final CLogger LOGGER = new CLogger(BypassHandler.class.getName());

    private final Map<Integer, IBypassHandler> _datatable = new HashMap<>();

    private BypassHandler() {
        registerBypassHandler(new DailyRewardCBBypasses());
        registerBypassHandler(new DungeonBypasses());
    }

    public static BypassHandler getInstance() {
        return SingletonHolder._instance;
    }

    public void registerBypassHandler(IBypassHandler handler) {
        String[] ids = handler.getBypassHandlersList();
        for (String id : ids) {
            if (Config.PACKET_HANDLER_DEBUG)
                LOGGER.info("Adding handler for command " + id);
            this._datatable.put(id.hashCode(), handler);
        }
    }

    public IBypassHandler getBypassHandler(String bypass) {
        String command = bypass;
        if (bypass.contains(" "))
            command = bypass.substring(0, bypass.indexOf(" "));
        if (Config.PACKET_HANDLER_DEBUG)
            LOGGER.info("getting handler for command: " + command + " -> " + ((this._datatable.get(command.hashCode()) != null) ? 1 : 0));
        return this._datatable.get(command.hashCode());
    }

    public int size() {
        return this._datatable.size();
    }

    private static class SingletonHolder {
        protected static final BypassHandler _instance = new BypassHandler();
    }
}
