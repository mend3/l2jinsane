package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.enums.DoorType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.OpenType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.geoengine.geodata.IGeoObject;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.DoorAI;
import net.sf.l2j.gameserver.model.actor.stat.DoorStat;
import net.sf.l2j.gameserver.model.actor.status.DoorStatus;
import net.sf.l2j.gameserver.model.actor.template.DoorTemplate;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

public class Door extends Creature implements IGeoObject {
    private final Castle _castle;
    private final ClanHall _clanHall;
    private boolean _open;

    public Door(int objectId, DoorTemplate template) {
        super(objectId, template);
        this._castle = CastleManager.getInstance().getCastleById(template.getCastle());
        if (this._castle != null) {
            this._castle.getDoors().add(this);
        }

        this._clanHall = ClanHallManager.getInstance().getNearestClanHall(template.getPosX(), template.getPosY(), 500);
        if (this._clanHall != null) {
            this._clanHall.getDoors().add(this);
        }

        this._open = !this.getTemplate().isOpened();
        this.setName(template.getName());
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null) {
            synchronized (this) {
                if (this._ai == null) {
                    this._ai = new DoorAI(this);
                }

                return this._ai;
            }
        } else {
            return ai;
        }
    }

    public void initCharStat() {
        this.setStat(new DoorStat(this));
    }

    public final DoorStat getStat() {
        return (DoorStat) super.getStat();
    }

    public void initCharStatus() {
        this.setStatus(new DoorStatus(this));
    }

    public final DoorStatus getStatus() {
        return (DoorStatus) super.getStatus();
    }

    public final DoorTemplate getTemplate() {
        return (DoorTemplate) super.getTemplate();
    }

    public void addFuncsToNewCharacter() {
    }

    public final int getLevel() {
        return this.getTemplate().getLevel();
    }

    public void updateAbnormalEffect() {
    }

    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    public Weapon getActiveWeaponItem() {
        return null;
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    public Weapon getSecondaryWeaponItem() {
        return null;
    }

    public boolean isAttackable() {
        return this._castle != null && this._castle.getSiege().isInProgress();
    }

    public boolean isAutoAttackable(Creature attacker) {
        if (!(attacker instanceof Playable)) {
            return false;
        } else if (this.isUnlockable()) {
            return true;
        } else {
            boolean isCastle = this._castle != null && this._castle.getSiege().isInProgress();
            if (isCastle) {
                Clan clan = attacker.getActingPlayer().getClan();
                if (clan != null && clan.getClanId() == this._castle.getOwnerId()) {
                    return false;
                }
            }

            return isCastle;
        }
    }

    public void onForcedAttack(Player player) {
        this.onAction(player);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
            player.sendPacket(new DoorStatusUpdate(this));
        } else if (this.isAutoAttackable(player)) {
            if (Math.abs(player.getZ() - this.getZ()) < 400) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
            }
        } else if (!this.isInsideRadius(player, 150, false, false)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else if (player.getClan() != null && this._clanHall != null && player.getClanId() == this._clanHall.getOwnerId()) {
            player.setRequestedGate(this);
            player.sendPacket(new ConfirmDlg(!this.isOpened() ? 1140 : 1141));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/admin/doorinfo.htm");
            html.replace("%name%", this.getName());
            html.replace("%objid%", this.getObjectId());
            html.replace("%doorid%", this.getTemplate().getId());
            html.replace("%doortype%", this.getTemplate().getType().toString());
            html.replace("%doorlvl%", this.getTemplate().getLevel());
            html.replace("%castle%", this._castle != null ? this._castle.getName() : "none");
            html.replace("%opentype%", this.getTemplate().getOpenType().toString());
            html.replace("%initial%", this.getTemplate().isOpened() ? "Opened" : "Closed");
            html.replace("%ot%", this.getTemplate().getOpenTime());
            html.replace("%ct%", this.getTemplate().getCloseTime());
            html.replace("%rt%", this.getTemplate().getRandomTime());
            html.replace("%controlid%", this.getTemplate().getTriggerId());
            html.replace("%hp%", (int) this.getCurrentHp());
            html.replace("%hpmax%", this.getMaxHp());
            html.replace("%hpratio%", this.getStat().getUpgradeHpRatio());
            html.replace("%pdef%", this.getPDef(null));
            html.replace("%mdef%", this.getMDef(null, null));
            int var10002 = this.getX();
            html.replace("%spawn%", var10002 + " " + this.getY() + " " + this.getZ());
            html.replace("%height%", this.getTemplate().getCollisionHeight());
            player.sendPacket(html);
        }

        if (player.getTarget() != this) {
            player.setTarget(this);
            if (this.isAutoAttackable(player)) {
                player.sendPacket(new DoorStatusUpdate(this));
            }
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (this._castle != null && this._castle.getSiege().isInProgress()) {
            if (attacker instanceof SiegeSummon || this.getTemplate().getType() != DoorType.WALL && skill == null) {
                super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
            }
        }
    }

    public void reduceCurrentHpByDOT(double i, Creature attacker, L2Skill skill) {
    }

    public void onSpawn() {
        this.changeState(this.getTemplate().isOpened(), false);
        super.onSpawn();
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (!this._open) {
                GeoEngine.getInstance().removeGeoObject(this);
            }

            if (this._castle != null && this._castle.getSiege().isInProgress()) {
                this._castle.getSiege().announceToPlayers(SystemMessage.getSystemMessage(this.getTemplate().getType() == DoorType.WALL ? SystemMessageId.CASTLE_WALL_DAMAGED : SystemMessageId.CASTLE_GATE_BROKEN_DOWN), false);
            }

            return true;
        }
    }

    public void doRevive() {
        this._open = this.getTemplate().isOpened();
        if (!this._open) {
            GeoEngine.getInstance().addGeoObject(this);
        }

        super.doRevive();
    }

    public void broadcastStatusUpdate() {
        this.broadcastPacket(new DoorStatusUpdate(this));
    }

    public void moveToLocation(int x, int y, int z, int offset) {
    }

    public void stopMove(SpawnLocation loc) {
    }

    public synchronized void doAttack(Creature target) {
    }

    public void doCast(L2Skill skill) {
    }

    public void sendInfo(Player activeChar) {
        activeChar.sendPacket(new DoorInfo(this));
        activeChar.sendPacket(new DoorStatusUpdate(this));
    }

    public int getGeoX() {
        return this.getTemplate().getGeoX();
    }

    public int getGeoY() {
        return this.getTemplate().getGeoY();
    }

    public int getGeoZ() {
        return this.getTemplate().getGeoZ();
    }

    public int getHeight() {
        return (int) this.getTemplate().getCollisionHeight();
    }

    public byte[][] getObjectGeoData() {
        return this.getTemplate().getGeoData();
    }

    public double getCollisionHeight() {
        return this.getTemplate().getCollisionHeight() / (double) 2.0F;
    }

    public final int getDoorId() {
        return this.getTemplate().getId();
    }

    public final boolean isOpened() {
        return this._open;
    }

    public final boolean isUnlockable() {
        return this.getTemplate().getOpenType() == OpenType.SKILL;
    }

    public final int getDamage() {
        return Math.max(0, Math.min(6, 6 - (int) Math.ceil(this.getCurrentHp() / (double) this.getMaxHp() * (double) 6.0F)));
    }

    public final void openMe() {
        this.changeState(true, false);
    }

    public final void closeMe() {
        this.changeState(false, false);
    }

    public final void changeState(boolean open, boolean triggered) {
        if (!this.isDead() && this._open != open) {
            this._open = open;
            if (open) {
                GeoEngine.getInstance().removeGeoObject(this);
            } else {
                GeoEngine.getInstance().addGeoObject(this);
            }

            this.broadcastStatusUpdate();
            int triggerId = this.getTemplate().getTriggerId();
            if (triggerId > 0) {
                Door door = DoorData.getInstance().getDoor(triggerId);
                if (door != null) {
                    door.changeState(open, true);
                }
            }

            if (!triggered) {
                int time = open ? this.getTemplate().getCloseTime() : this.getTemplate().getOpenTime();
                if (this.getTemplate().getRandomTime() > 0) {
                    time += Rnd.get(this.getTemplate().getRandomTime());
                }

                if (time > 0) {
                    ThreadPool.schedule(() -> this.changeState(!open, false), time * 1000L);
                }
            }

        }
    }

    public final Castle getCastle() {
        return this._castle;
    }
}
