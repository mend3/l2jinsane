package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.handler.chathandlers.*;

import java.util.HashMap;
import java.util.Map;

public class ChatHandler {
    private final Map<Integer, IChatHandler> _entries = new HashMap<>();

    protected ChatHandler() {
        registerHandler(new ChatAll());
        registerHandler(new ChatAlliance());
        registerHandler(new ChatClan());
        registerHandler(new ChatHeroVoice());
        registerHandler(new ChatParty());
        registerHandler(new ChatPartyMatchRoom());
        registerHandler(new ChatPartyRoomAll());
        registerHandler(new ChatPartyRoomCommander());
        registerHandler(new ChatPetition());
        registerHandler(new ChatShout());
        registerHandler(new ChatTell());
        registerHandler(new ChatTrade());
    }

    public static ChatHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void registerHandler(IChatHandler handler) {
        for (int id : handler.getChatTypeList())
            this._entries.put(id, handler);
    }

    public IChatHandler getHandler(int chatType) {
        return this._entries.get(chatType);
    }

    public int size() {
        return this._entries.size();
    }

    private static class SingletonHolder {
        protected static final ChatHandler INSTANCE = new ChatHandler();
    }
}
