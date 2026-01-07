package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.model.actor.Player;

public class EtcStatusUpdate extends L2GameServerPacket {
    private final Player _player;

    public EtcStatusUpdate(Player player) {
        this._player = player;
    }

    protected void writeImpl() {
        writeC(243);
        writeD(this._player.getCharges());
        writeD(this._player.getWeightPenalty());
        writeD((this._player.isInRefusalMode() || this._player.isChatBanned()) ? 1 : 0);
        writeD(this._player.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
        writeD((this._player.getExpertiseWeaponPenalty() || this._player.getExpertiseArmorPenalty() > 0) ? 1 : 0);
        writeD(this._player.isAffected(L2EffectFlag.CHARM_OF_COURAGE) ? 1 : 0);
        writeD(this._player.getDeathPenaltyBuffLevel());
    }
}
