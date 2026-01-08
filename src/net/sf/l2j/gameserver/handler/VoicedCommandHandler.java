package net.sf.l2j.gameserver.handler;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.*;

import java.util.HashMap;
import java.util.Map;

public class VoicedCommandHandler {
    private static final CLogger LOGGER = new CLogger(VoicedCommandHandler.class.getName());

    private static VoicedCommandHandler _instance;

    private final Map<String, IVoicedCommandHandler> _datatable;

    private VoicedCommandHandler() {
        this._datatable = new HashMap<>();
        registerVoicedCommandHandler(new DressMe());
        registerVoicedCommandHandler(new VoicedCombine());
        registerVoicedCommandHandler(new EventCommands());
        registerVoicedCommandHandler(new VoicedAchievement());
        registerVoicedCommandHandler(new VoicedAutoFarm());
        registerVoicedCommandHandler(new VoicedPvpEvent());
        registerVoicedCommandHandler(new VoicedPvPZoneExit());
        registerVoicedCommandHandler(new VoicedMenu());
        registerVoicedCommandHandler(new VoicedEpic());
        registerVoicedCommandHandler(new VoicedBossEventCMD());
        registerVoicedCommandHandler(new VoicedCoupon());
    }

    public static VoicedCommandHandler getInstance() {
        if (_instance == null)
            _instance = new VoicedCommandHandler();
        return _instance;
    }

    public void registerVoicedCommandHandler(IVoicedCommandHandler handler) {
        String[] ids = handler.getVoicedCommandList();
        for (String id : ids) {
            if (Config.PACKET_HANDLER_DEBUG)
                LOGGER.info("Adding handler for command " + id);
            this._datatable.put(id, handler);
        }
    }

    public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand) {
        String command = voicedCommand;
        if (voicedCommand.contains(" "))
            command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
        if (Config.PACKET_HANDLER_DEBUG)
            LOGGER.info("getting handler for command: " + command + " -> " + ((this._datatable.get(command) != null) ? 1 : 0));
        return this._datatable.get(command);
    }

    public int size() {
        return this._datatable.size();
    }
}
