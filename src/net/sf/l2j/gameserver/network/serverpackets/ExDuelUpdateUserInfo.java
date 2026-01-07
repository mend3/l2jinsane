package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class ExDuelUpdateUserInfo extends L2GameServerPacket {
    private final Player _player;

    public ExDuelUpdateUserInfo(Player player) {
        this._player = player;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(79);
        writeS(this._player.getName());
        writeD(this._player.getObjectId());
        writeD(this._player.getClassId().getId());
        writeD(this._player.getLevel());
        writeD((int) this._player.getCurrentHp());
        writeD(this._player.getMaxHp());
        writeD((int) this._player.getCurrentMp());
        writeD(this._player.getMaxMp());
        writeD((int) this._player.getCurrentCp());
        writeD(this._player.getMaxCp());
    }
}
