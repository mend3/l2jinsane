package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.Collection;

public class PledgeSkillList extends L2GameServerPacket {
    private final Clan _clan;

    public PledgeSkillList(Clan clan) {
        this._clan = clan;
    }

    protected void writeImpl() {
        Collection<L2Skill> skills = this._clan.getClanSkills().values();
        writeC(254);
        writeH(57);
        writeD(skills.size());
        for (L2Skill sk : skills) {
            writeD(sk.getId());
            writeD(sk.getLevel());
        }
    }
}
