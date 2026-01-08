package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AbnormalStatusUpdate extends L2GameServerPacket {
    private final List<Effect> _effects = new ArrayList<>();

    public void addEffect(int skillId, int level, int duration) {
        this._effects.add(new Effect(skillId, level, duration));
    }

    protected final void writeImpl() {
        writeC(127);
        writeH(this._effects.size());
        for (Effect temp : this._effects) {
            writeD(temp._skillId);
            writeH(temp._level);
            if (temp._duration == -1) {
                writeD(-1);
                continue;
            }
            writeD(temp._duration / 1000);
        }
    }

    private record Effect(int _skillId, int _level, int _duration) {
    }
}
