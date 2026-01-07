package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

public class SiegeZone extends SpawnZoneType {
    private int _siegableId = -1;

    private boolean _isActiveSiege = false;

    public SiegeZone(int id) {
        super(id);
    }

    public void setParameter(String name, String value) {
        if (name.equals("castleId") || name.equals("clanHallId")) {
            this._siegableId = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        if (this._isActiveSiege) {
            character.setInsideZone(ZoneId.PVP, true);
            character.setInsideZone(ZoneId.SIEGE, true);
            character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
            if (character instanceof Player activeChar) {
                activeChar.setIsInSiege(true);
                activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
                activeChar.enterOnNoLandingZone();
            }
        }
    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.PVP, false);
        character.setInsideZone(ZoneId.SIEGE, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        if (character instanceof Player activeChar) {
            if (this._isActiveSiege) {
                activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
                activeChar.exitOnNoLandingZone();
                PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
                if (activeChar.getPvpFlag() == 0)
                    activeChar.updatePvPFlag(1);
            }
            activeChar.setIsInSiege(false);
        } else if (character instanceof SiegeSummon) {
            ((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
        }
    }

    public void updateZoneStatusForCharactersInside() {
        if (this._isActiveSiege) {
            for (Creature character : this._characters.values())
                onEnter(character);
        } else {
            for (Creature character : this._characters.values()) {
                character.setInsideZone(ZoneId.PVP, false);
                character.setInsideZone(ZoneId.SIEGE, false);
                character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
                if (character instanceof Player player) {
                    player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
                    player.exitOnNoLandingZone();
                    continue;
                }
                if (character instanceof SiegeSummon)
                    ((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
            }
        }
    }

    public int getSiegeObjectId() {
        return this._siegableId;
    }

    public boolean isActive() {
        return this._isActiveSiege;
    }

    public void setIsActive(boolean val) {
        this._isActiveSiege = val;
    }

    public void announceToPlayers(String message) {
        for (Player player : getKnownTypeInside(Player.class))
            player.sendMessage(message);
    }

    public void banishForeigners(int clanId) {
        for (Player player : getKnownTypeInside(Player.class)) {
            if (player.getClanId() == clanId)
                continue;
            player.teleportTo((player.getKarma() > 0) ? getRandomChaoticLoc() : getRandomLoc(), 20);
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }
}
