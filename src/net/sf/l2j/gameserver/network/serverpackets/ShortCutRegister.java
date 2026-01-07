package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.Shortcut;

public class ShortCutRegister extends L2GameServerPacket {
    private final Shortcut _shortcut;

    public ShortCutRegister(Shortcut shortcut) {
        this._shortcut = shortcut;
    }

    protected final void writeImpl() {
        writeC(68);
        writeD(this._shortcut.getType().ordinal());
        writeD(this._shortcut.getSlot() + this._shortcut.getPage() * 12);
        switch (this._shortcut.getType()) {
            case ITEM:
                writeD(this._shortcut.getId());
                writeD(this._shortcut.getCharacterType());
                writeD(this._shortcut.getSharedReuseGroup());
                return;
            case SKILL:
                writeD(this._shortcut.getId());
                writeD(this._shortcut.getLevel());
                writeC(0);
                writeD(this._shortcut.getCharacterType());
                return;
        }
        writeD(this._shortcut.getId());
        writeD(this._shortcut.getCharacterType());
    }
}
