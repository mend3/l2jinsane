package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;

public final class RequestShowBoard extends L2GameClientPacket {
    private int _unknown;

    protected void readImpl() {
        this._unknown = readD();
    }

    protected void runImpl() {
        CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
    }
}
