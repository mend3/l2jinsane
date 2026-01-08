package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.util.concurrent.CopyOnWriteArrayList;

public class L2AutoFarmZone extends SpawnZoneType {
    final CopyOnWriteArrayList<String> _playerAllowed = new CopyOnWriteArrayList<>();

    public L2AutoFarmZone(int id) {
        super(id);
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player activeChar) {
            if (activeChar.isInParty()) {
                activeChar.teleportTo(82725, 148596, -3468, 0);
                activeChar.sendMessage("No party zone !! .");
                return;
            }
            if (HwidConfig.PROTECT_HWID_AUTOFARM_ZONE)
                if (this._playerAllowed != null && this._playerAllowed.contains(activeChar.getHWID())) {
                    activeChar.teleportTo(82725, 148596, -3468, 0);
                    activeChar.sendMessage("You already have a character in the area !! .");
                    return;
                }
            if (!this._playerAllowed.contains(activeChar.getHWID()))
                this._playerAllowed.add(activeChar.getHWID());
            activeChar.addSpoilSkillinZone();
            activeChar.sendPacket(new CreatureSay(0, 2, activeChar.getName(), ": Remember for exit use command .exit"));
            activeChar.sendPacket(new CreatureSay(0, 2, activeChar.getName(), ": You can use AutoFarm inside this zone."));
        }
        character.setInsideZone(ZoneId.AUTOFARMZONE, true);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.NO_RESTART, true);
        character.setInsideZone(ZoneId.NO_STORE, true);
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.AUTOFARMZONE, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.NO_RESTART, false);
        character.setInsideZone(ZoneId.NO_STORE, false);
        if (character instanceof Player activeChar) {
            this._playerAllowed.remove(activeChar.getHWID());
            activeChar.removeSpoilSkillinZone();
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
}
