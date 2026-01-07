package net.sf.l2j.gameserver.network.serverpackets;

import mods.autofarm.AutofarmPlayerRoutine;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.pledge.Clan;

public class Die extends L2GameServerPacket {
    private final Creature _activeChar;

    private final int _charObjId;

    private final boolean _fake;

    private boolean _sweepable;

    private boolean _allowFixedRes;

    private Clan _clan;

    private boolean _canTeleport;

    public Die(Creature cha) {
        this._activeChar = cha;
        this._charObjId = cha.getObjectId();
        this._fake = !cha.isDead();
        if (cha instanceof Player player) {
            AutofarmPlayerRoutine bot = player.getBot();
            this._allowFixedRes = player.getAccessLevel().allowFixedRes();
            this._clan = player.getClan();
            this._canTeleport = (!player.isInsideZone(ZoneId.MULTI_FUNCTION) || !Config.REVIVE);
            if (player.isAutoFarm()) {
                bot.stop();
                player.setAutoFarm(false);
            }
        } else if (cha instanceof Monster) {
            this._sweepable = ((Monster) cha).isSpoiled();
        }
    }

    protected final void writeImpl() {
        if (this._fake)
            return;
        writeC(6);
        writeD(this._charObjId);
        if ((this._activeChar instanceof Player && !EventListener.allowDiePacket((Player) this._activeChar)) || (this._activeChar instanceof Player && ((Player) this._activeChar).getDungeon() != null)) {
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            if (this._activeChar instanceof Player player && player.getDungeon() != null) {
                player.getDungeon().onPlayerDeath(player);
            }
            return;
        }
        writeD(this._canTeleport ? 1 : 0);
        if (this._canTeleport && this._clan != null) {
            SiegeSide side = null;
            Siege siege = CastleManager.getInstance().getActiveSiege(this._activeChar);
            if (siege != null)
                side = siege.getSide(this._clan);
            writeD(this._clan.hasClanHall() ? 1 : 0);
            writeD((this._clan.hasCastle() || side == SiegeSide.OWNER || side == SiegeSide.DEFENDER) ? 1 : 0);
            writeD((side == SiegeSide.ATTACKER && this._clan.getFlag() != null) ? 1 : 0);
        } else {
            writeD(0);
            writeD(0);
            writeD(0);
        }
        writeD(this._sweepable ? 1 : 0);
        writeD(this._allowFixedRes ? 1 : 0);
    }
}
