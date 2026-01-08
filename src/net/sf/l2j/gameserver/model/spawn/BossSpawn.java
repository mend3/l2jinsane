package net.sf.l2j.gameserver.model.spawn;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.RaidBossInfoManager;
import net.sf.l2j.gameserver.enums.BossStatus;
import net.sf.l2j.gameserver.model.actor.Npc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.concurrent.ScheduledFuture;

public class BossSpawn {
    protected static final CLogger LOGGER = new CLogger(BossSpawn.class.getName());
    private static final String DELETE_RAIDBOSS = "DELETE FROM raidboss_spawnlist WHERE boss_id=?";
    private static final String UPDATE_RAIDBOSS = "UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?";
    private L2Spawn _spawn;
    private BossStatus _status;
    private ScheduledFuture<?> _task;
    private double _currentHp;
    private double _currentMp;
    private long _respawnTime;

    public BossSpawn() {
        this._status = BossStatus.UNDEFINED;
    }

    public L2Spawn getSpawn() {
        return this._spawn;
    }

    public void setSpawn(L2Spawn spawn) {
        this._spawn = spawn;
    }

    public BossStatus getStatus() {
        return this._status;
    }

    public void setStatus(BossStatus status) {
        this._status = status;
    }

    public ScheduledFuture<?> getTask() {
        return this._task;
    }

    public void setTask(ScheduledFuture<?> task) {
        this._task = task;
    }

    public void cancelTask() {
        if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
        }

    }

    public double getCurrentHp() {
        return this._currentHp;
    }

    public void setCurrentHp(double currentHp) {
        this._currentHp = currentHp;
    }

    public double getCurrentMp() {
        return this._currentMp;
    }

    public void setCurrentMp(double currentMp) {
        this._currentMp = currentMp;
    }

    public long getRespawnTime() {
        return this._respawnTime;
    }

    public void setRespawnTime(long respawnTime) {
        this._respawnTime = respawnTime;
    }

    public Npc getBoss() {
        return this._spawn.getNpc();
    }

    public void onDeath() {
        int respawnDelay = this._spawn.getRespawnMinDelay() + Rnd.get(-this._spawn.getRespawnMaxDelay(), this._spawn.getRespawnMaxDelay());
        long respawnTime = System.currentTimeMillis() + (long) (respawnDelay * 3600000);
        this._status = BossStatus.DEAD;
        this._currentHp = 0.0F;
        this._currentMp = 0.0F;
        this._respawnTime = respawnTime;
        this.cancelTask();
        this._task = ThreadPool.schedule(this::onSpawn, respawnDelay * 3600000);
        this.updateOnDb();
        LOGGER.info("Raid boss: {} - {} ({}h).", new Object[]{this._spawn.getNpc().getName(), (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(respawnTime), respawnDelay});
        if (Config.LIST_RAID_BOSS_IDS.contains(this._spawn.getNpc().getNpcId())) {
            RaidBossInfoManager.getInstance().updateRaidBossInfo(this._spawn.getNpc().getNpcId(), respawnTime);
        }

    }

    public void onSpawn() {
        Npc npc = this._spawn.doSpawn(false);
        this._status = BossStatus.ALIVE;
        this._currentHp = npc.getMaxHp();
        this._currentMp = npc.getMaxMp();
        this._respawnTime = 0L;
        this.cancelTask();
        this.updateOnDb();
        LOGGER.info("{} raid boss has spawned.", new Object[]{npc.getName()});
        if (Config.LIST_RAID_BOSS_IDS.contains(npc.getNpcId())) {
            RaidBossInfoManager.getInstance().updateRaidBossInfo(npc.getNpcId(), 0L);
        }

    }

    public void onDespawn() {
        this.cancelTask();
        Npc npc = this._spawn.getNpc();
        if (npc != null && !npc.isDecayed()) {
            npc.deleteMe();
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?");
        ) {
            ps.setInt(1, this._spawn.getNpcId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't remove raid boss #{}.", e, new Object[]{this._spawn.getNpcId()});
        }

        this._spawn = null;
    }

    private void updateOnDb() {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?");
        ) {
            ps.setLong(1, this._respawnTime);
            ps.setDouble(2, this._currentHp);
            ps.setDouble(3, this._currentMp);
            ps.setInt(4, this._spawn.getNpcId());
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't update raid boss #{}.", e, new Object[]{this._spawn.getNpcId()});
        }

    }
}
