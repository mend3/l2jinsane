package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

import java.util.ArrayList;
import java.util.List;

public class ExOlympiadSpelledInfo extends L2GameServerPacket {
    private final int _playerID;

    private final List<Effect> _effects;

    public ExOlympiadSpelledInfo(Player player) {
        this._effects = new ArrayList<>();
        this._playerID = player.getObjectId();
    }

    public void addEffect(int skillId, int level, int duration) {
        this._effects.add(new Effect(skillId, level, duration));
    }

    protected final void writeImpl() {
        writeC(254);
        writeH(42);
        writeD(this._playerID);
        writeD(this._effects.size());
        for (Effect temp : this._effects) {
            writeD(temp._skillId);
            writeH(temp._level);
            writeD(temp._duration / 1000);
        }
    }

    private record Effect(int _skillId, int _level, int _duration) {
    }
}
