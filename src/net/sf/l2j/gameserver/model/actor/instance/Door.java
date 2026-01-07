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
import net.sf.l2j.gameserver.model.Residence;
import net.sf.l2j.gameserver.model.actor.Creature;
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
    private Castle _castle;
    private boolean _open;
    private Residence _residence;

    public Door(int objectId, DoorTemplate template) {
        super(objectId, template);
        this._open = !getTemplate().isOpened();
        setName(template.getName());
    }

    public void initResidences() {
        this._castle = CastleManager.getInstance().getCastleById(getTemplate().getCastle());
        if (this._castle != null)
            this._castle.getDoors().add(this);
        this._residence = ClanHallManager.getInstance().getNearestClanHall(getTemplate().getPosX(), getTemplate().getPosY(), 500);
        if (this._residence != null)
            this._residence.getDoors().add(this);
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null)
            synchronized (this) {
                if (this._ai == null)
                    this._ai = new DoorAI(this);
                return this._ai;
            }
        return ai;
    }

    public void initCharStat() {
        setStat(new DoorStat(this));
    }

    public final DoorStat getStat() {
        return (DoorStat) super.getStat();
    }

    public void initCharStatus() {
        setStatus(new DoorStatus(this));
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
        return getTemplate().getLevel();
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
        return (this._castle != null && this._castle.getSiege().isInProgress());
    }

    public boolean isAutoAttackable(Creature attacker) {
        if (!(attacker instanceof net.sf.l2j.gameserver.model.actor.Playable))
            return false;
        if (isUnlockable())
            return true;
        boolean isCastle = (this._castle != null && this._castle.getSiege().isInProgress());
        if (isCastle) {
            Clan clan = attacker.getActingPlayer().getClan();
            if (clan != null && clan.getClanId() == this._castle.getOwnerId())
                return false;
        }
        return isCastle;
    }

    public void onForcedAttack(Player player) {
        onAction(player);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
            player.sendPacket(new DoorStatusUpdate(this));
        } else if (isAutoAttackable(player)) {
            if (Math.abs(player.getZ() - getZ()) < 400)
                player.getAI().setIntention(IntentionType.ATTACK, this);
        } else if (!isInsideRadius(player, 150, false, false)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else if (player.getClan() != null && this._residence != null && player.getClanId() == this._residence.getOwnerId()) {
            player.setRequestedGate(this);
            player.sendPacket(new ConfirmDlg(!isOpened() ? 1140 : 1141));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/admin/doorinfo.htm");
            html.replace("%name%", getName());
            html.replace("%objid%", getObjectId());
            html.replace("%doorid%", getTemplate().getId());
            html.replace("%doortype%", getTemplate().getType().toString());
            html.replace("%doorlvl%", getTemplate().getLevel());
            html.replace("%castle%", (this._castle != null) ? this._castle.getName() : "none");
            html.replace("%opentype%", getTemplate().getOpenType().toString());
            html.replace("%initial%", getTemplate().isOpened() ? "Opened" : "Closed");
            html.replace("%ot%", getTemplate().getOpenTime());
            html.replace("%ct%", getTemplate().getCloseTime());
            html.replace("%rt%", getTemplate().getRandomTime());
            html.replace("%controlid%", getTemplate().getTriggerId());
            html.replace("%hp%", (int) getCurrentHp());
            html.replace("%hpmax%", getMaxHp());
            html.replace("%hpratio%", getStat().getUpgradeHpRatio());
            html.replace("%pdef%", getPDef(null));
            html.replace("%mdef%", getMDef(null, null));
            html.replace("%spawn%", getX() + " " + getX() + " " + getY());
            html.replace("%height%", getTemplate().getCollisionHeight());
            player.sendPacket(html);
        }
        if (player.getTarget() != this) {
            player.setTarget(this);
            if (isAutoAttackable(player))
                player.sendPacket(new DoorStatusUpdate(this));
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (this._castle == null || !this._castle.getSiege().isInProgress())
            return;
        if (!(attacker instanceof SiegeSummon) && (getTemplate().getType() == DoorType.WALL || skill != null))
            return;
        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
    }

    public void reduceCurrentHpByDOT(double i, Creature attacker, L2Skill skill) {
    }

    public void onSpawn() {
        changeState(getTemplate().isOpened(), false);
        super.onSpawn();
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer))
            return false;
        if (!this._open)
            GeoEngine.getInstance().removeGeoObject(this);
        if (this._castle != null && this._castle.getSiege().isInProgress())
            this._castle.getSiege().announceToPlayers(SystemMessage.getSystemMessage((getTemplate().getType() == DoorType.WALL) ? SystemMessageId.CASTLE_WALL_DAMAGED : SystemMessageId.CASTLE_GATE_BROKEN_DOWN), false);
        return true;
    }

    public void doRevive() {
        this._open = getTemplate().isOpened();
        if (!this._open)
            GeoEngine.getInstance().addGeoObject(this);
        super.doRevive();
    }

    public void broadcastStatusUpdate() {
        broadcastPacket(new DoorStatusUpdate(this));
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
        return getTemplate().getGeoX();
    }

    public int getGeoY() {
        return getTemplate().getGeoY();
    }

    public int getGeoZ() {
        return getTemplate().getGeoZ();
    }

    public int getHeight() {
        return (int) getTemplate().getCollisionHeight();
    }

    public byte[][] getObjectGeoData() {
        return getTemplate().getGeoData();
    }

    public double getCollisionHeight() {
        return getTemplate().getCollisionHeight() / 2.0D;
    }

    public final int getDoorId() {
        return getTemplate().getId();
    }

    public final boolean isOpened() {
        return this._open;
    }

    public final boolean isUnlockable() {
        return (getTemplate().getOpenType() == OpenType.SKILL);
    }

    public final int getDamage() {
        return Math.max(0, Math.min(6, 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6.0D)));
    }

    public final void openMe() {
        changeState(true, false);
    }

    public final void closeMe() {
        changeState(false, false);
    }

    public final void changeState(boolean open, boolean triggered) {
        if (isDead() || this._open == open)
            return;
        this._open = open;
        if (open) {
            GeoEngine.getInstance().removeGeoObject(this);
        } else {
            GeoEngine.getInstance().addGeoObject(this);
        }
        broadcastStatusUpdate();
        int triggerId = getTemplate().getTriggerId();
        if (triggerId > 0) {
            Door door = DoorData.getInstance().getDoor(triggerId);
            if (door != null)
                door.changeState(open, true);
        }
        if (!triggered) {
            int time = open ? getTemplate().getCloseTime() : getTemplate().getOpenTime();
            if (getTemplate().getRandomTime() > 0)
                time += Rnd.get(getTemplate().getRandomTime());
            if (time > 0)
                ThreadPool.schedule(() -> changeState(!open, false), (time * 1000L));
        }
    }

    public final Castle getCastle() {
        return this._castle;
    }

    public final Residence getResidence() {
        return _residence;
    }

    public final void setResidence(Residence residence) {
        _residence = residence;
    }

    /**
     * @param player : The {@link Player} to test.
     * @return True if this {@link Door} can be manually opened, or false otherwise. Only used by {@link Player} upon {@link ClanHall} doors.
     */
    public boolean canBeManuallyOpenedBy(Player player) {
        return player.getClan() != null && _residence instanceof Residence ch && player.getClanId() == ch.getOwnerId() && ch.getOwnerId() == _residence.getId();
    }
}
