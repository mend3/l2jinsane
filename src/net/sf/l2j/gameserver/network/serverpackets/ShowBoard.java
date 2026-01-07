package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.commons.lang.StringUtil;

import java.util.List;

public class ShowBoard extends L2GameServerPacket {
    public static final ShowBoard STATIC_SHOWBOARD_102 = new ShowBoard(null, "102");

    public static final ShowBoard STATIC_SHOWBOARD_103 = new ShowBoard(null, "103");

    private static final String TOP = "bypass _bbshome";

    private static final String FAV = "bypass _bbsgetfav";

    private static final String REGION = "bypass _bbsloc";

    private static final String CLAN = "bypass _bbsclan";

    private static final String MEMO = "bypass _bbsmemo";

    private static final String MAIL = "bypass _maillist_0_1_0_";

    private static final String FRIENDS = "bypass _friendlist_0_";

    private static final String ADDFAV = "bypass bbs_add_fav";

    private final StringBuilder _htmlCode = new StringBuilder();

    public ShowBoard(String htmlCode, String id) {
        StringUtil.append(this._htmlCode, id, "\b", htmlCode);
    }

    public ShowBoard(List<String> arg) {
        this._htmlCode.append("1002\b");
        for (String str : arg) {
            StringUtil.append(this._htmlCode, str, " \b");
        }
    }

    protected final void writeImpl() {
        writeC(110);
        writeC(1);
        writeS("bypass _bbshome");
        writeS("bypass _bbsgetfav");
        writeS("bypass _bbsloc");
        writeS("bypass _bbsclan");
        writeS("bypass _bbsmemo");
        writeS("bypass _maillist_0_1_0_");
        writeS("bypass _friendlist_0_");
        writeS("bypass bbs_add_fav");
        writeS(this._htmlCode.toString());
    }
}
