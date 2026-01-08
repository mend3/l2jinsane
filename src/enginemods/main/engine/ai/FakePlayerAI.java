package enginemods.main.engine.ai;

import enginemods.main.data.PlayerData;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.instance.Gatekeeper;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.StaticObject;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.*;
import java.util.concurrent.Future;

public class FakePlayerAI extends CreatureAI implements Runnable {
    private static final List<Npc> _lastTalk = new ArrayList<>(5);
    protected Future<?> _aiTask = null;
    private boolean _thinking = false;
    private Desire _nextIntention = null;
    private Location _posToFarm = null;
    private Future<?> _taskReturnFarm = null;
    private boolean _isNeedBackToFarm = false;
    private int _randomGoToCityTime = 0;
    private int _randomWalkTime = 0;

    public FakePlayerAI(Player player) {
        super(player);
        this.startTasks();
        System.out.println("FakePlayerAI");
    }

    public static void toSelfAndKnownPlayersInRadius(Playable character, L2GameServerPacket mov, int radius) {
        if (radius < 0) {
            radius = 600;
        }

        if (character instanceof Player) {
            character.sendPacket(mov);
        }

        for (Player player : character.getKnownTypeInRadius(Player.class, radius)) {
            player.sendPacket(mov);
        }

    }

    public Player getActor() {
        return (Player) this._actor;
    }

    public void run() {
        this.onEvtThink();
    }

    private void startTasks() {
        try {
            ThreadPool.schedule(() -> {
                this._posToFarm = PlayerData.get(this.getActor()).getPosToFarm();
                if (this.isInsideCity()) {
                    this.startAITask(1000L);
                } else {
                    this._isNeedBackToFarm = true;
                    this.getActor().standUp();
                    this.getActor().setRunning();
                    this.getActor().setTarget(null);
                    this._randomGoToCityTime = Rnd.get(2, 16) * 60 * 1000;
                    this.getActor().getAI().setIntention(IntentionType.ACTIVE);
                }

            }, Rnd.get(10, 30) * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void attackTarget() {
        if (this.getActor().getCurrentMp() < (double) this.getActor().getMaxMp() * (double) 0.5F) {
            this.getActor().setCurrentMp(this.getActor().getMaxMp());
        }

        if (this.getActor().isMageClass()) {
            int skillId = -1;
            switch (this.getActor().getRace()) {
                case HUMAN -> skillId = 1230;
                case DARK_ELF -> skillId = 1239;
                case ELF -> skillId = 1235;
                case ORC -> skillId = 1245;
            }

            if (skillId != -1) {
                int level = SkillTable.getInstance().getMaxLevel(skillId);
                L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
                if (this.getActor().getSkill(skillId) == null) {
                    this.getActor().addSkill(skill, true);
                }

                if (this.maybeMoveToPawn(this.getTarget(), skill.getCastRange())) {
                    return;
                }

                this.getActor().useMagic(skill, true, true);
            }
        } else {
            if (this.getActor().getActiveWeaponItem() != null && this.getActor().getActiveWeaponItem().getItemType() == WeaponType.BOW) {
                CrystalType bow = this.getActor().getActiveWeaponItem().getCrystalType();
                int arrowId = 17;
                switch (bow) {
                    case S -> arrowId = 1345;
                    case A -> arrowId = 1344;
                    case B -> arrowId = 1343;
                    case C -> arrowId = 1342;
                    case D -> arrowId = 1341;
                    case NONE -> arrowId = 17;
                }

                if (this.getActor().getInventory().getInventoryItemCount(arrowId, -1) < 10) {
                    this.getActor().getInventory().addItem("arrow fakes", arrowId, 500, this.getActor(), this.getActor());
                }
            }

            this.setIntention(IntentionType.ATTACK, this.getActor().getTarget());
        }

    }

    private Creature searchNextAttack() {
        for (Player player : this.getActor().getKnownTypeInRadius(Player.class, 1000)) {
            if (player.getKarma() > 0 || player.getPvpFlag() > 0) {
                return player;
            }
        }

        Map<Double, Monster> listAttack = new TreeMap<>();

        for (Monster monster : this.getActor().getKnownTypeInRadius(Monster.class, 2000)) {
            if (GeoEngine.getInstance().canMoveToTarget(this.getActor().getX(), this.getActor().getY(), this.getActor().getZ(), monster.getX(), monster.getY(), monster.getZ()) && GeoEngine.getInstance().canSeeTarget(this.getActor(), monster)) {
                double distance = MathUtil.calculateDistance(this.getActor(), monster, false);
                listAttack.put(distance, monster);
            }
        }

        for (Monster monster : listAttack.values()) {
            if (monster != null && !monster.isDead()) {
                return monster;
            }
        }

        return null;
    }

    private synchronized void searchCityRandomLoc() {
        boolean searchNewLoc = true;

        while (searchNewLoc) {
            int ox = this.getActor().getX();
            int oy = this.getActor().getY();
            int oz = this.getActor().getZ();
            int tx = Rnd.get(ox - 2000, ox + 2000);
            int ty = Rnd.get(oy - 2000, oy + 2000);
            int tz = this.getActor().getZ();
            if (MapRegionData.getTown(tx, ty, tz) != null && GeoEngine.getInstance().canMoveToTarget(ox, oy, oz, tx, ty, tz)) {
                searchNewLoc = false;
                this.setIntention(IntentionType.MOVE_TO, new Location(tx, ty, tz));
            }
        }

    }

    protected void clientActionFailed() {
        this._actor.sendPacket(ActionFailed.STATIC_PACKET);
    }

    void setNextIntention(IntentionType intention, Object arg0, Object arg1) {
        this._nextIntention = new Desire();
    }

    public Desire getNextIntention() {
        return this._nextIntention;
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        if (!this.getActor().isAlikeDead() && intention == IntentionType.IDLE && !this.getActor().getKnownTypeInRadius(Monster.class, 500).isEmpty()) {
            intention = IntentionType.ACTIVE;
        }

        super.changeIntention(intention, arg0, arg1);
        this.startAITask(500L);
    }

    protected void onEvtReadyToAct() {
        if (this._nextIntention != null) {
            this.setIntention(this._nextIntention.getIntention(), this._nextIntention.getFirstParameter(), this._nextIntention.getSecondParameter());
            this._nextIntention = null;
        }

        super.onEvtReadyToAct();
    }

    protected void onEvtCancel() {
        this._nextIntention = null;
        super.onEvtCancel();
    }

    protected void onIntentionRest() {
        if (this.getActor().getAI().getDesire().getIntention() != IntentionType.REST) {
            this.changeIntention(IntentionType.REST, null, null);
            this.setTarget(null);
            this.clientStopMoving(null);
        }

    }

    protected void onIntentionActive() {
        super.onIntentionActive();
    }

    protected void onIntentionMoveTo(Location loc) {
        if (this.getActor().getAI().getDesire().getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAttackingNow()) {
            this.changeIntention(IntentionType.MOVE_TO, loc, null);
            this.stopAttackStance();
            this._actor.abortAttack();
            this.moveTo(loc.getX(), loc.getY(), loc.getZ());
        } else {
            this.clientActionFailed();
            this.setNextIntention(IntentionType.MOVE_TO, loc, null);
        }
    }

    protected void clientNotifyDead() {
        this._clientMovingToPawnOffset = 0;
        this._clientMoving = false;
        super.clientNotifyDead();
    }

    private void thinkAttack() {
        Creature target = (Creature) this.getTarget();
        if (target == null) {
            this.setTarget(null);
            this.setIntention(IntentionType.ACTIVE);
        } else if (!this.maybeMoveToPawn(target, this._actor.getPhysicalAttackRange())) {
            if (target.isAlikeDead()) {
                if (!(target instanceof Player) || !((Player) target).isFakeDeath()) {
                    this.setIntention(IntentionType.ACTIVE);
                    return;
                }

                target.stopFakeDeath(true);
            }

            this.chargeShots();
            this.clientStopMoving(null);
            this._actor.doAttack(target);
        }
    }

    private void thinkCast() {
        if (this.checkTargetLost(this.getTarget())) {
            this.setTarget(null);
        } else if (!this.maybeMoveToPawn(this.getTarget(), this._skill.getCastRange())) {
            this.clientStopMoving(null);
            this.setIntention(IntentionType.ACTIVE);
            this._actor.doCast(this._skill);
        }
    }

    protected void onIntentionIdle() {
        super.onIntentionIdle();
    }

    private void thinkPickUp() {
        if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            WorldObject target = this.getTarget();
            if (!this.checkTargetLost(target)) {
                if (!this.maybeMoveToPawn(target, 36)) {
                    this._actor.getActingPlayer().doPickupItem(target);
                    this.setIntention(IntentionType.IDLE);
                }
            }
        }
    }

    private void thinkInteract() {
        if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            WorldObject target = this.getTarget();
            if (!this.checkTargetLost(target)) {
                if (!this.maybeMoveToPawn(target, 36)) {
                    if (!(target instanceof StaticObject)) {
                        this._actor.getActingPlayer().doInteract((Creature) target);
                    }

                    this.setIntention(IntentionType.IDLE);
                }
            }
        }
    }

    protected void onEvtThink() {
        if (!this._thinking || this.getActor().getAI().getDesire().getIntention() == IntentionType.CAST) {
            if (this.getActor().getAI().getDesire().getIntention() != IntentionType.MOVE_TO) {
                if (this.isInsideCity()) {
                    this.changeIntention(IntentionType.IDLE, null, null);
                }

                this._thinking = true;

                try {
                    switch (this.getActor().getAI().getDesire().getIntention()) {
                        case IDLE -> this.thinkIdle();
                        case ACTIVE -> this.thinkActive();
                        case ATTACK -> this.thinkAttack();
                        case CAST -> this.thinkCast();
                        case PICK_UP -> this.thinkPickUp();
                        case INTERACT -> this.thinkInteract();
                    }
                } finally {
                    this._thinking = false;
                }

            }
        }
    }

    protected void thinkActive() {
        if (this._taskReturnFarm != null) {
            this._taskReturnFarm.cancel(true);
            this._taskReturnFarm = null;
        }

        if (this._randomGoToCityTime <= 0) {
            this._randomGoToCityTime = Rnd.get(2, 16) * 60 * 2;
            this.getActor().abortCast();
            this.getActor().abortAttack();
            this.getActor().setTarget(null);
            L2Skill skill = SkillTable.getInstance().getInfo(2036, 1);
            this.getActor().useMagic(skill, true, false);
            this._randomWalkTime = 6;
            this.setIntention(IntentionType.IDLE);
        } else {
            --this._randomGoToCityTime;
            if (!this.getActor().isCastingNow() && !this.getActor().isAttackingNow()) {
                this.chargeShots();
                WorldObject obj = this.getActor().getTarget();
                if (obj == null || ((Creature) obj).isDead()) {
                    Creature monster = this.searchNextAttack();
                    if (monster != null) {
                        this.getActor().setTarget(monster);
                        this.setTarget(monster);
                    }
                }

                this.attackTarget();
            }

        }
    }

    protected void thinkIdle() {
        if (this._randomWalkTime <= 0) {
            if (this.isInsideCity() && this._isNeedBackToFarm) {
                Iterator<Gatekeeper> var1 = this.getActor().getKnownTypeInRadius(Gatekeeper.class, 2000).iterator();
                if (var1.hasNext()) {
                    Gatekeeper teleport = var1.next();
                    this.setIntention(IntentionType.MOVE_TO, new Location(teleport.getX(), teleport.getY(), teleport.getZ()));
                    this._isNeedBackToFarm = false;
                    return;
                }
            }

            this._randomWalkTime = Rnd.get(5, 30);
            if (this._isNeedBackToFarm) {
                this._randomWalkTime *= 2;
            }

            Npc target = null;
            if (Rnd.nextBoolean()) {
                for (Npc npc : this.getActor().getKnownType(Npc.class)) {
                    if (npc != null && !(npc instanceof Monster) && Rnd.get(5) == 0 && GeoEngine.getInstance().canSeeTarget(this.getActor(), npc) && !GeoEngine.getInstance().canMoveToTarget(this.getActor().getX(), this.getActor().getY(), this.getActor().getZ(), npc.getX(), npc.getY(), npc.getZ()) && !_lastTalk.contains(npc)) {
                        if (_lastTalk.size() > 5) {
                            _lastTalk.clear();
                        }

                        _lastTalk.add(npc);
                        target = npc;
                        break;
                    }
                }
            }

            this.getActor().setRunning();
            if (target == null) {
                this.searchCityRandomLoc();
            } else {
                this.setIntention(IntentionType.MOVE_TO, new Location(target.getX() + 10, target.getY() + 10, target.getZ()));
            }
        }

        --this._randomWalkTime;
    }

    protected void onEvtDead() {
        super.onEvtDead();
        this.stopAITask();
        this.clientNotifyDead();
        ThreadPool.schedule(() -> {
            this.getActor().teleportTo(TeleportType.TOWN);
            this.getActor().doRevive();
        }, 5000L);
        ThreadPool.schedule(this::searchCityRandomLoc, 8000L);
        ThreadPool.schedule(() -> {
            Iterator<Gatekeeper> var1 = this.getActor().getKnownTypeInRadius(Gatekeeper.class, 1000).iterator();
            if (var1.hasNext()) {
                Gatekeeper teleport = var1.next();
                this.setIntention(IntentionType.MOVE_TO, new Location(teleport.getX(), teleport.getY(), teleport.getZ()));
            }

        }, 180000L);
        ThreadPool.schedule(() -> {
            if (this._posToFarm != null) {
                this.getActor().teleportTo(this._posToFarm.getX(), this._posToFarm.getY(), this._posToFarm.getZ(), 1000);
                this.setIntention(IntentionType.MOVE_TO, this._posToFarm);
                this._taskReturnFarm = ThreadPool.scheduleAtFixedRate(() -> {
                    if (this.getActor().isInsideRadius(this._posToFarm.getX(), this._posToFarm.getY(), 100, false)) {
                        this.startAITask(500L);
                    }

                }, 1000L, 1000L);
            }

        }, 300000L);
    }

    private void chargeShots() {
        if (!this.getActor().isChargedShot(ShotType.BLESSED_SPIRITSHOT)) {
            toSelfAndKnownPlayersInRadius(this.getActor(), new MagicSkillUse(this.getActor(), this.getActor(), 2061, 1, 0, 0), 600);
            this.getActor().setChargedShot(ShotType.BLESSED_SPIRITSHOT, true);
        }

        if (!this.getActor().isChargedShot(ShotType.SOULSHOT)) {
            toSelfAndKnownPlayersInRadius(this.getActor(), new MagicSkillUse(this.getActor(), this.getActor(), 2155, 1, 0, 0), 600);
            this.getActor().setChargedShot(ShotType.SOULSHOT, true);
        }

    }

    public void startAITask(long time) {
        if (this._aiTask == null) {
            this._aiTask = ThreadPool.scheduleAtFixedRate(this, 1000L, time);
        }

    }

    public void stopAITask() {
        if (this._aiTask != null) {
            this._aiTask.cancel(false);
            this._aiTask = null;
        }

        super.stopAITask();
    }

    private boolean isInsideCity() {
        return MapRegionData.getTown(this.getActor().getX(), this.getActor().getY(), this.getActor().getZ()) != null;
    }

    public enum CityTimeType {
        SEARCH_NEXT_POS
    }
}
