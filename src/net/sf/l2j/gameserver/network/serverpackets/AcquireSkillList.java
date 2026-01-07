package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.skills.AcquireSkillType;
import net.sf.l2j.gameserver.model.holder.skillnode.ClanSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.FishingSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.SkillNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AcquireSkillList extends L2GameServerPacket {
    private final AcquireSkillType _skillType;
    private final List<? extends SkillNode> _skills;

    public AcquireSkillList(AcquireSkillType type, List<? extends SkillNode> skills) {
        this._skillType = type;
        this._skills = new ArrayList<>(skills);
    }

    protected void writeImpl() {
        writeC(138);
        writeD(this._skillType.ordinal());
        writeD(this._skills.size());
        switch (this._skillType) {
            case USUAL:
                Objects.requireNonNull(GeneralSkillNode.class);
                this._skills.stream().map(GeneralSkillNode.class::cast).forEach(gsn -> {
                    writeD(gsn.getId());
                    writeD(gsn.getValue());
                    writeD(gsn.getValue());
                    writeD(gsn.getCorrectedCost());
                    writeD(0);
                });
                break;
            case FISHING:
                Objects.requireNonNull(FishingSkillNode.class);
                this._skills.stream().map(FishingSkillNode.class::cast).forEach(gsn -> {
                    writeD(gsn.getId());
                    writeD(gsn.getValue());
                    writeD(gsn.getValue());
                    writeD(0);
                    writeD(1);
                });
                break;
            case CLAN:
                Objects.requireNonNull(ClanSkillNode.class);
                this._skills.stream().map(ClanSkillNode.class::cast).forEach(gsn -> {
                    writeD(gsn.getId());
                    writeD(gsn.getValue());
                    writeD(gsn.getValue());
                    writeD(gsn.getCost());
                    writeD(0);
                });
                break;
        }
    }
}
