package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Collection;

public class GMViewSkillInfo extends L2GameServerPacket {
    private final Player _player;

    private final Collection<L2Skill> _skills;

    private final boolean _isWearingFormalWear;

    private final boolean _isClanDisabled;

    public GMViewSkillInfo(Player player) {
        this._player = player;
        this._skills = player.getSkills().values();
        this._isWearingFormalWear = player.isWearingFormalWear();
        this._isClanDisabled = (player.getClan() != null && player.getClan().getReputationScore() < 0);
    }

    protected final void writeImpl() {
        writeC(145);
        writeS(this._player.getName());
        writeD(this._skills.size());
        for (L2Skill skill : this._skills) {
            writeD(skill.isPassive() ? 1 : 0);
            writeD(skill.getLevel());
            writeD(skill.getId());
            writeC((this._isWearingFormalWear || (skill.isClanSkill() && this._isClanDisabled)) ? 1 : 0);
        }
    }
}
