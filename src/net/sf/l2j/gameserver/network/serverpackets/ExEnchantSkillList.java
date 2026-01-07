package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.holder.skillnode.EnchantSkillNode;

import java.util.List;

public class ExEnchantSkillList extends L2GameServerPacket {
    private final List<EnchantSkillNode> _skills;

    public ExEnchantSkillList(List<EnchantSkillNode> skills) {
        this._skills = skills;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(23);
        writeD(this._skills.size());
        for (EnchantSkillNode esn : this._skills) {
            writeD(esn.getId());
            writeD(esn.getValue());
            writeD(esn.getSp());
            writeQ(esn.getExp());
        }
    }
}
