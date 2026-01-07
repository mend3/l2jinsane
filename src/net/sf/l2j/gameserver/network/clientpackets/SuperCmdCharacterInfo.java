package net.sf.l2j.gameserver.network.clientpackets;

public final class SuperCmdCharacterInfo extends L2GameClientPacket {
    private String _characterName;

    protected void readImpl() {
        this._characterName = readS();
    }

    protected void runImpl() {
    }
}
