package enginemods.main.packets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public class PrivateCustomTitle extends L2GameServerPacket {
    private final Player _player;
    private final int _opCode;
    private final String _msg;

    public PrivateCustomTitle(Player player, PrivateCustomTitle.TitleType titleType, String msg) {
        this._player = player;
        this._opCode = titleType.getOpCode();
        this._msg = msg;
    }

    public void writeImpl() {
        this.writeC(this._opCode);
        this.writeD(this._player.getObjectId());
        this.writeS(this._msg);
    }

    public enum TitleType {
        SELL(156),
        BUY(185),
        MANUFACTURE(219);

        private final int _opCode;

        TitleType(int opCode) {
            this._opCode = opCode;
        }


        public int getOpCode() {
            return this._opCode;
        }
    }
}