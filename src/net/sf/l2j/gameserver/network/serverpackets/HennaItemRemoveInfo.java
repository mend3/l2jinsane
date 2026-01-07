package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;

public class HennaItemRemoveInfo extends L2GameServerPacket {
    private final Henna _henna;

    private final int _adena;

    private final int _int;

    private final int _str;

    private final int _con;

    private final int _men;

    private final int _dex;

    private final int _wit;

    public HennaItemRemoveInfo(Henna henna, Player player) {
        this._henna = henna;
        this._adena = player.getAdena();
        this._int = player.getINT();
        this._str = player.getSTR();
        this._con = player.getCON();
        this._men = player.getMEN();
        this._dex = player.getDEX();
        this._wit = player.getWIT();
    }

    protected final void writeImpl() {
        writeC(230);
        writeD(this._henna.getSymbolId());
        writeD(this._henna.getDyeId());
        writeD(5);
        writeD(this._henna.getRemovePrice());
        writeD(1);
        writeD(this._adena);
        writeD(this._int);
        writeC(this._int - this._henna.getINT());
        writeD(this._str);
        writeC(this._str - this._henna.getSTR());
        writeD(this._con);
        writeC(this._con - this._henna.getCON());
        writeD(this._men);
        writeC(this._men - this._henna.getMEN());
        writeD(this._dex);
        writeC(this._dex - this._henna.getDEX());
        writeD(this._wit);
        writeC(this._wit - this._henna.getWIT());
    }
}
