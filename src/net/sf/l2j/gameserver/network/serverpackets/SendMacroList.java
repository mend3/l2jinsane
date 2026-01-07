package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.Macro;

public class SendMacroList extends L2GameServerPacket {
    private final int _rev;

    private final int _count;

    private final Macro _macro;

    public SendMacroList(int rev, int count, Macro macro) {
        this._rev = rev;
        this._count = count;
        this._macro = macro;
    }

    protected final void writeImpl() {
        writeC(231);
        writeD(this._rev);
        writeC(0);
        writeC(this._count);
        writeC((this._macro != null) ? 1 : 0);
        if (this._macro != null) {
            writeD(this._macro.id);
            writeS(this._macro.name);
            writeS(this._macro.descr);
            writeS(this._macro.acronym);
            writeC(this._macro.icon);
            writeC(this._macro.commands.length);
            for (int i = 0; i < this._macro.commands.length; i++) {
                Macro.MacroCmd cmd = this._macro.commands[i];
                writeC(i + 1);
                writeC(cmd.type());
                writeD(cmd.d1());
                writeC(cmd.d2());
                writeS(cmd.cmd());
            }
        }
    }
}
