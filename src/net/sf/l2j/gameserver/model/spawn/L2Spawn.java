package net.sf.l2j.gameserver.model.spawn;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

public final class L2Spawn implements Runnable {
    private static final Logger _log = Logger.getLogger(L2Spawn.class.getName());

    private final NpcTemplate _template;

    private Constructor<?> _constructor;

    private Npc _npc;

    private SpawnLocation _loc;

    private int _respawnDelay;

    private int _respawnRandom;

    private boolean _respawnEnabled;

    private int _respawnMinDelay;

    private int _respawnMaxDelay;

    public L2Spawn(NpcTemplate template) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
        this._template = template;
        if (this._template == null)
            return;
        Class<?>[] parameters = new Class[]{int.class, Class.forName("net.sf.l2j.gameserver.model.actor.template.NpcTemplate")};
        this._constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + this._template.getType()).getConstructor(parameters);
    }

    public NpcTemplate getTemplate() {
        return this._template;
    }

    public int getNpcId() {
        return this._template.getNpcId();
    }

    public Npc getNpc() {
        return this._npc;
    }

    public void setLoc(int locX, int locY, int locZ, int heading) {
        this._loc = new SpawnLocation(locX, locY, locZ, heading);
    }

    public SpawnLocation getLoc() {
        return this._loc;
    }

    public void setLoc(SpawnLocation loc) {
        this._loc = loc;
    }

    public int getLocX() {
        return this._loc.getX();
    }

    public int getLocY() {
        return this._loc.getY();
    }

    public int getLocZ() {
        return this._loc.getZ();
    }

    public int getHeading() {
        return this._loc.getHeading();
    }

    public int getRespawnDelay() {
        return this._respawnDelay;
    }

    public void setRespawnDelay(int delay) {
        this._respawnDelay = Math.max(1, delay);
    }

    public int getRespawnRandom() {
        return this._respawnRandom;
    }

    public void setRespawnRandom(int random) {
        this._respawnRandom = Math.min(this._respawnDelay, random);
    }

    public int calculateRespawnTime() {
        int respawnTime = this._respawnDelay;
        if (this._respawnRandom > 0)
            respawnTime += Rnd.get(-this._respawnRandom, this._respawnRandom);
        return respawnTime;
    }

    public void setRespawnState(boolean state) {
        this._respawnEnabled = state;
    }

    public int getRespawnMinDelay() {
        return this._respawnMinDelay;
    }

    public void setRespawnMinDelay(int date) {
        this._respawnMinDelay = date;
    }

    public int getRespawnMaxDelay() {
        return this._respawnMaxDelay;
    }

    public void setRespawnMaxDelay(int date) {
        this._respawnMaxDelay = date;
    }

    public Npc doSpawn(boolean isSummonSpawn) {
        try {
            if (this._template.isType("Pet"))
                return null;
            Object[] parameters = {IdFactory.getInstance().getNextId(), this._template};
            Object tmp = this._constructor.newInstance(parameters);
            if (isSummonSpawn && tmp instanceof Creature)
                ((Creature) tmp).setShowSummonAnimation(isSummonSpawn);
            if (!(tmp instanceof Npc))
                return null;
            this._npc = (Npc) tmp;
            this._npc.setSpawn(this);
            initializeAndSpawn();
            return this._npc;
        } catch (Exception e) {
            _log.warning("L2Spawn: Error during spawn, NPC id=" + this._template.getNpcId());
            return null;
        }
    }

    public void doRespawn() {
        if (this._respawnEnabled) {
            int respawnTime = calculateRespawnTime() * 1000;
            ThreadPool.schedule(this, respawnTime);
        }
    }

    public void run() {
        if (this._respawnEnabled) {
            this._npc.refreshID();
            initializeAndSpawn();
        }
    }

    private void initializeAndSpawn() {
        if (this._loc == null) {
            _log.warning("L2Spawn : the following npcID: " + this._template.getNpcId() + " misses location informations.");
            return;
        }
        this._npc.stopAllEffects();
        this._npc.setIsDead(false);
        this._npc.setDecayed(false);
        this._npc.setScriptValue(0);
        int locx = this._loc.getX();
        int locy = this._loc.getY();
        int locz = GeoEngine.getInstance().getHeight(locx, locy, this._loc.getZ());
        if (Math.abs(locz - this._loc.getZ()) > 200)
            locz = this._loc.getZ();
        this._npc.setCurrentHpMp(this._npc.getMaxHp(), this._npc.getMaxMp());
        if (Config.CHAMPION_FREQUENCY > 0)
            if (this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Monster && !getTemplate().cantBeChampion() && this._npc.getLevel() >= Config.CHAMP_MIN_LVL && this._npc.getLevel() <= Config.CHAMP_MAX_LVL && !this._npc.isRaidRelated() && !this._npc.isMinion())
                this._npc.setChampion((Rnd.get(100) < Config.CHAMPION_FREQUENCY));
        EngineModsManager.onSpawn(this._npc);
        this._npc.spawnMe(locx, locy, locz, (this._loc.getHeading() < 0) ? Rnd.get(65536) : this._loc.getHeading());
    }

    public String toString() {
        return "L2Spawn [id=" + this._template.getNpcId() + ", loc=" + this._loc.toString() + "]";
    }
}
