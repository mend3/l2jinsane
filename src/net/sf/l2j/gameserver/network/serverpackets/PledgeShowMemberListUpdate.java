package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.ClanMember;

public final class PledgeShowMemberListUpdate extends L2GameServerPacket {
    private final int _pledgeType;

    private final int _hasSponsor;

    private final String _name;

    private final int _level;

    private final int _classId;

    private final int _isOnline;

    private final int _race;

    private final int _sex;

    public PledgeShowMemberListUpdate(Player player) {
        this._pledgeType = player.getPledgeType();
        this._hasSponsor = (player.getSponsor() != 0 || player.getApprentice() != 0) ? 1 : 0;
        this._name = player.getName();
        this._level = player.getLevel();
        this._classId = player.getClassId().getId();
        this._race = player.getRace().ordinal();
        this._sex = player.getAppearance().getSex().ordinal();
        this._isOnline = player.isOnline() ? player.getObjectId() : 0;
    }

    public PledgeShowMemberListUpdate(ClanMember member) {
        this._name = member.getName();
        this._level = member.getLevel();
        this._classId = member.getClassId();
        this._isOnline = member.isOnline() ? member.getObjectId() : 0;
        this._pledgeType = member.getPledgeType();
        this._hasSponsor = (member.getSponsor() != 0 || member.getApprentice() != 0) ? 1 : 0;
        if (this._isOnline != 0) {
            this._race = member.getPlayerInstance().getRace().ordinal();
            this._sex = member.getPlayerInstance().getAppearance().getSex().ordinal();
        } else {
            this._sex = 0;
            this._race = 0;
        }
    }

    protected void writeImpl() {
        writeC(84);
        writeS(this._name);
        writeD(this._level);
        writeD(this._classId);
        writeD(this._sex);
        writeD(this._race);
        writeD(this._isOnline);
        writeD(this._pledgeType);
        writeD(this._hasSponsor);
    }
}
