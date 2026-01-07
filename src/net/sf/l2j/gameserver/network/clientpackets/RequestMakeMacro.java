package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.Macro;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestMakeMacro extends L2GameClientPacket {
    private static final int MAX_MACRO_LENGTH = 12;

    private Macro _macro;

    private int _commandsLenght = 0;

    protected void readImpl() {
        int id = readD();
        String name = readS();
        String desc = readS();
        String acronym = readS();
        int icon = readC();
        int count = readC();
        if (count > 12)
            count = 12;
        Macro.MacroCmd[] commands = new Macro.MacroCmd[count];
        for (int i = 0; i < count; i++) {
            int entry = readC();
            int type = readC();
            int d1 = readD();
            int d2 = readC();
            String command = readS();
            this._commandsLenght += command.length();
            commands[i] = new Macro.MacroCmd(entry, type, d1, d2, command);
        }
        this._macro = new Macro(id, icon, name, desc, acronym, commands);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._commandsLenght > 255) {
            player.sendPacket(SystemMessageId.INVALID_MACRO);
            return;
        }
        if ((player.getMacroList().getMacros()).length > 24) {
            player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS);
            return;
        }
        if (this._macro.name.isEmpty()) {
            player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME);
            return;
        }
        if (this._macro.descr.length() > 32) {
            player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS);
            return;
        }
        player.getMacroList().registerMacro(this._macro);
    }
}
