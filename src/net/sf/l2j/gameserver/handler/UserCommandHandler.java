package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.handler.usercommandhandlers.*;

import java.util.HashMap;
import java.util.Map;

public class UserCommandHandler {
    private final Map<Integer, IUserCommandHandler> _entries = new HashMap<>();

    protected UserCommandHandler() {
        registerHandler(new ChannelDelete());
        registerHandler(new ChannelLeave());
        registerHandler(new ChannelListUpdate());
        registerHandler(new ClanPenalty());
        registerHandler(new ClanWarsList());
        registerHandler(new DisMount());
        registerHandler(new Escape());
        registerHandler(new Loc());
        registerHandler(new Mount());
        registerHandler(new OlympiadStat());
        registerHandler(new PartyInfo());
        registerHandler(new SiegeStatus());
        registerHandler(new Time());
    }

    public static UserCommandHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void registerHandler(IUserCommandHandler handler) {
        for (int id : handler.getUserCommandList())
            this._entries.put(id, handler);
    }

    public IUserCommandHandler getHandler(int userCommand) {
        return this._entries.get(userCommand);
    }

    public int size() {
        return this._entries.size();
    }

    private static class SingletonHolder {
        protected static final UserCommandHandler INSTANCE = new UserCommandHandler();
    }
}
