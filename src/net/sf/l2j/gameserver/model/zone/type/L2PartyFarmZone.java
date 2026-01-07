package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.util.concurrent.CopyOnWriteArrayList;

public class L2PartyFarmZone extends SpawnZoneType {
    CopyOnWriteArrayList<String> _playerAllowed = new CopyOnWriteArrayList<>();

    public L2PartyFarmZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player activeChar) {
            if (!activeChar.isInParty()) {
                activeChar.teleportTo(82725, 148596, -3468, 0);
                activeChar.sendMessage("You're not in party !! .");
                return;
            }
            if (HwidConfig.PROTECT_HWID_PARTY_ZONE)
                if (this._playerAllowed != null && this._playerAllowed.contains(activeChar.getHWID())) {
                    activeChar.teleportTo(82725, 148596, -3468, 0);
                    activeChar.sendMessage("You already have a character in the area !! .");
                    return;
                }
            if (!this._playerAllowed.contains(activeChar.getHWID()))
                this._playerAllowed.add(activeChar.getHWID());
            ThreadPool.scheduleAtFixedRate(new KickPlayer(activeChar), 5000L, 5000L);
            activeChar.sendPacket(new CreatureSay(0, 2, activeChar.getName(), ": Remember for exit use command .exit"));
        }
        character.setInsideZone(ZoneId.PARTYFARMZONE, true);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.NO_RESTART, true);
        character.setInsideZone(ZoneId.NO_STORE, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.PARTYFARMZONE, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.NO_RESTART, false);
        character.setInsideZone(ZoneId.NO_STORE, false);
        if (character instanceof Player activeChar) {
            this._playerAllowed.remove(activeChar.getHWID());
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }

    public void respawnCharacter(Creature character) {
        if (character == null || !character.isDead() || !(character instanceof Player))
            return;
        character.doRevive();
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentCp(character.getMaxCp());
        character.setCurrentMp(character.getMaxMp());
        character.teleportTo(82635, 148798, -3464, 25);
    }

    private static final class KickPlayer implements Runnable {
        private Player _player;

        public KickPlayer(Player player) {
            this._player = player;
        }

        public void run() {
            if (this._player != null && !this._player.isInParty() && this._player.isInsideZone(ZoneId.PARTYFARMZONE)) {
                Summon summon = this._player.getSummon();
                if (summon != null)
                    summon.unSummon(this._player);
                this._player.teleportTo(82725, 148596, -3468, 0);
                this._player.sendMessage("Party zone !! .");
                this._player.setInsideZone(ZoneId.PARTYFARMZONE, false);
                this._player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
                this._player.setInsideZone(ZoneId.NO_RESTART, false);
                this._player.setInsideZone(ZoneId.NO_STORE, false);
                this._player = null;
            }
        }
    }
}
