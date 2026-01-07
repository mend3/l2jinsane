package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.communitybbs.CommunityBoard;

public class RequestBBSwrite extends L2GameClientPacket {
    private String _url;

    private String _arg1;

    private String _arg2;

    private String _arg3;

    private String _arg4;

    private String _arg5;

    protected void readImpl() {
        this._url = readS();
        this._arg1 = readS();
        this._arg2 = readS();
        this._arg3 = readS();
        this._arg4 = readS();
        this._arg5 = readS();
    }

    protected void runImpl() {
        CommunityBoard.getInstance().handleWriteCommands(getClient(), this._url, this._arg1, this._arg2, this._arg3, this._arg4, this._arg5);
    }
}
