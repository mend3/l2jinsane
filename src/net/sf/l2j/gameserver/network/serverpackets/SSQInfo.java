package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;

public class SSQInfo extends L2GameServerPacket {
    public static final SSQInfo REGULAR_SKY_PACKET = new SSQInfo(256);

    public static final SSQInfo DUSK_SKY_PACKET = new SSQInfo(257);

    public static final SSQInfo DAWN_SKY_PACKET = new SSQInfo(258);

    public static final SSQInfo RED_SKY_PACKET = new SSQInfo(259);

    private final int _state;

    private SSQInfo(int state) {
        this._state = state;
    }

    public static SSQInfo sendSky() {
        if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
            CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
            if (winningCabal == CabalType.DAWN)
                return DAWN_SKY_PACKET;
            if (winningCabal == CabalType.DUSK)
                return DUSK_SKY_PACKET;
        }
        return REGULAR_SKY_PACKET;
    }

    protected final void writeImpl() {
        writeC(248);
        writeH(this._state);
    }
}
