package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.Timestamp;

import java.util.List;
import java.util.stream.Collectors;

public class SkillCoolTime extends L2GameServerPacket {
    public final List<Timestamp> _reuseTimeStamps;

    public SkillCoolTime(Player cha) {
        this._reuseTimeStamps = cha.getReuseTimeStamps().stream().filter(Timestamp::hasNotPassed).collect(Collectors.toList());
    }

    protected void writeImpl() {
        writeC(193);
        writeD(this._reuseTimeStamps.size());
        for (Timestamp ts : this._reuseTimeStamps) {
            writeD(ts.getId());
            writeD(ts.getValue());
            writeD((int) ts.getReuse() / 1000);
            writeD((int) ts.getRemaining() / 1000);
        }
    }
}
