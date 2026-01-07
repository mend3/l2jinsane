package net.sf.l2j.gameserver.network.serverpackets;

public class ShowCalculator extends L2GameServerPacket {
    private final int _calculatorId;

    public ShowCalculator(int calculatorId) {
        this._calculatorId = calculatorId;
    }

    protected final void writeImpl() {
        writeC(220);
        writeD(this._calculatorId);
    }
}
