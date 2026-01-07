package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class CharSelected extends L2GameServerPacket {
    private final Player _activeChar;

    private final int _sessionId;

    public CharSelected(Player cha, int sessionId) {
        this._activeChar = cha;
        this._sessionId = sessionId;
    }

    protected final void writeImpl() {
        writeC(21);
        writeS(this._activeChar.getName());
        writeD(this._activeChar.getObjectId());
        writeS(this._activeChar.getTitle());
        writeD(this._sessionId);
        writeD(this._activeChar.getClanId());
        writeD(0);
        writeD(this._activeChar.getAppearance().getSex().ordinal());
        writeD(this._activeChar.getRace().ordinal());
        writeD(this._activeChar.getClassId().getId());
        writeD(1);
        writeD(this._activeChar.getX());
        writeD(this._activeChar.getY());
        writeD(this._activeChar.getZ());
        writeF(this._activeChar.getCurrentHp());
        writeF(this._activeChar.getCurrentMp());
        writeD(this._activeChar.getSp());
        writeQ(this._activeChar.getExp());
        writeD(this._activeChar.getLevel());
        writeD(this._activeChar.getKarma());
        writeD(this._activeChar.getPkKills());
        writeD(this._activeChar.getINT());
        writeD(this._activeChar.getSTR());
        writeD(this._activeChar.getCON());
        writeD(this._activeChar.getMEN());
        writeD(this._activeChar.getDEX());
        writeD(this._activeChar.getWIT());
        for (int i = 0; i < 30; i++)
            writeD(0);
        writeD(0);
        writeD(0);
        writeD(GameTimeTaskManager.getInstance().getGameTime());
        writeD(0);
        writeD(this._activeChar.getClassId().getId());
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
    }
}
