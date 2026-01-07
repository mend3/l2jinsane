package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.CastleZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public class FlameTower extends Npc {
    private int _upgradeLevel;

    private List<Integer> _zoneList;

    public FlameTower(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isAttackable() {
        return (getCastle() != null && getCastle().getSiege().isInProgress());
    }

    public boolean isAutoAttackable(Creature attacker) {
        return (attacker instanceof Player && getCastle() != null && getCastle().getSiege().isInProgress() && getCastle().getSiege().checkSide(((Player) attacker).getClan(), SiegeSide.ATTACKER));
    }

    public void onForcedAttack(Player player) {
        onAction(player);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoEngine.getInstance().canSeeTarget(player, this)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public boolean doDie(Creature killer) {
        enableZones(false);
        if (getCastle() != null) {
            if (this._zoneList != null && this._upgradeLevel != 0)
                getCastle().getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED), false);
            try {
                L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(13005));
                spawn.setLoc(getPosition());
                Npc tower = spawn.doSpawn(false);
                tower.setCastle(getCastle());
                getCastle().getSiege().getDestroyedTowers().add(tower);
            } catch (Exception e) {
                LOGGER.error("Couldn't spawn the flame tower.", e);
            }
        }
        return super.doDie(killer);
    }

    public boolean hasRandomAnimation() {
        return false;
    }

    public void deleteMe() {
        enableZones(false);
        super.deleteMe();
    }

    public final void enableZones(boolean state) {
        if (this._zoneList != null && this._upgradeLevel != 0) {
            int maxIndex = this._upgradeLevel * 2;
            for (int i = 0; i < maxIndex; i++) {
                ZoneType zone = ZoneManager.getInstance().getZoneById(this._zoneList.get(i));
                if (zone != null && zone instanceof CastleZoneType)
                    ((CastleZoneType) zone).setEnabled(state);
            }
        }
    }

    public final void setUpgradeLevel(int level) {
        this._upgradeLevel = level;
    }

    public final void setZoneList(List<Integer> list) {
        this._zoneList = list;
        enableZones(true);
    }
}
