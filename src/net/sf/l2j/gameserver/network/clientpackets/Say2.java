package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.handler.ChatHandler;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class Say2 extends L2GameClientPacket {
    public static final int ALL = 0;
    public static final int SHOUT = 1;
    public static final int TELL = 2;
    public static final int PARTY = 3;
    public static final int CLAN = 4;
    public static final int GM = 5;
    public static final int PETITION_PLAYER = 6;
    public static final int PETITION_GM = 7;
    public static final int TRADE = 8;
    public static final int ALLIANCE = 9;
    public static final int ANNOUNCEMENT = 10;
    public static final int BOAT = 11;
    public static final int L2FRIEND = 12;
    public static final int MSNCHAT = 13;
    public static final int PARTYMATCH_ROOM = 14;
    public static final int PARTYROOM_COMMANDER = 15;
    public static final int PARTYROOM_ALL = 16;
    public static final int HERO_VOICE = 17;
    public static final int CRITICAL_ANNOUNCE = 18;
    private static final Logger CHAT_LOG = Logger.getLogger("chat");
    private static final String[] CHAT_NAMES = new String[]{
            "ALL", "SHOUT", "TELL", "PARTY", "CLAN", "GM", "PETITION_PLAYER", "PETITION_GM", "TRADE", "ALLIANCE",
            "ANNOUNCEMENT", "BOAT", "WILLCRASHCLIENT:)", "FAKEALL?", "PARTYMATCH_ROOM", "PARTYROOM_COMMANDER", "PARTYROOM_ALL", "HERO_VOICE", "CRITICAL_ANNOUNCEMENT"};

    private static final String[] WALKER_COMMAND_LIST = new String[]{
            "USESKILL", "USEITEM", "BUYITEM", "SELLITEM", "SAVEITEM", "LOADITEM", "MSG", "DELAY", "LABEL", "JMP",
            "CALL", "RETURN", "MOVETO", "NPCSEL", "NPCDLG", "DLGSEL", "CHARSTATUS", "POSOUTRANGE", "POSINRANGE", "GOHOME",
            "SAY", "EXIT", "PAUSE", "STRINDLG", "STRNOTINDLG", "CHANGEWAITTYPE", "FORCEATTACK", "ISMEMBER", "REQUESTJOINPARTY", "REQUESTOUTPARTY",
            "QUITPARTY", "MEMBERSTATUS", "CHARBUFFS", "ITEMCOUNT", "FOLLOWTELEPORT"};

    private String _text;

    private int _type;

    private String _target;

    private static boolean checkBot(String text) {
        for (String botCommand : WALKER_COMMAND_LIST) {
            if (text.startsWith(botCommand))
                return true;
        }
        return false;
    }

    protected void readImpl() {
        this._text = readS();
        this._type = readD();
        this._target = (this._type == 2) ? readS() : null;
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._type < 0 || this._type >= CHAT_NAMES.length)
            return;
        if (this._text.isEmpty() || this._text.length() > 100)
            return;
        if (Config.L2WALKER_PROTECTION && this._type == 2 && checkBot(this._text))
            return;
        if (!player.isGM() && (this._type == 10 || this._type == 18))
            return;
        if (player.isChatBanned() || (player.isInJail() && !player.isGM())) {
            player.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
            return;
        }
        if (this._type == 6 && player.isGM())
            this._type = 7;
        if (Config.LOG_CHAT) {
            LogRecord record = new LogRecord(Level.INFO, this._text);
            record.setLoggerName("chat");
            if (this._type == 2) {
                record.setParameters(new Object[]{CHAT_NAMES[this._type], "[" + player

                        .getName() + " to " + this._target + "]"});
            } else {
                record.setParameters(new Object[]{CHAT_NAMES[this._type], "[" + player

                        .getName() + "]"});
            }
            CHAT_LOG.log(record);
        }
        this._text = this._text.replaceAll("\\\\n", "");
        boolean disguised = false;
        if (TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            disguised = TvTEventManager.getInstance().getActiveEvent().isDisguisedEvent();
        } else if (CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            disguised = CtfEventManager.getInstance().getActiveEvent().isDisguisedEvent();
        } else if (DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) {
            disguised = DmEventManager.getInstance().getActiveEvent().isDisguisedEvent();
        }
        if (disguised) {
            player.sendMessage("You cannot talk in this event.");
            return;
        }
        if (EngineModsManager.onVoiced(player, this._text))
            return;
        IChatHandler handler = ChatHandler.getInstance().getHandler(this._type);
        if (handler == null) {
            LOGGER.warn("{} tried to use unregistred chathandler type: {}.", player.getName(), this._type);
            return;
        }
        handler.handleChat(this._type, player, this._target, this._text);
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
