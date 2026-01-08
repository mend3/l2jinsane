package net.sf.l2j.gameserver.model.actor.npc;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MinionList {
    private final Map<Monster, Boolean> _minions = new ConcurrentHashMap<>();
    private final Monster _master;

    public MinionList(Monster master) {
        this._master = master;
    }

    public List<Monster> getSpawnedMinions() {
        return this._minions.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public Map<Monster, Boolean> getMinions() {
        return this._minions;
    }

    public final void spawnMinions() {
        for (MinionData data : this._master.getTemplate().getMinionData()) {
            NpcTemplate template = NpcData.getInstance().getTemplate(data.getMinionId());
            if (template != null) {
                for (int i = 0; i < data.getAmount(); ++i) {
                    Monster minion = new Monster(IdFactory.getInstance().getNextId(), template);
                    minion.setMaster(this._master);
                    minion.setMinion(this._master.isRaidBoss());
                    this.initializeNpcInstance(this._master, minion);
                }
            }
        }

    }

    public void onMasterDie() {
        if (this._master.isRaidBoss()) {
            for (Monster minion : this.getSpawnedMinions()) {
                minion.deleteMe();
            }
        } else {
            for (Monster minion : this._minions.keySet()) {
                minion.setMaster(null);
            }
        }

        this._minions.clear();
    }

    public void onMasterDeletion() {
        for (Monster minion : this._minions.keySet()) {
            minion.setMaster(null);
            minion.deleteMe();
        }

        this._minions.clear();
    }

    public void onMinionDeletion(Monster minion) {
        minion.setMaster(null);
        this._minions.remove(minion);
    }

    public void onMinionDie(Monster minion, int respawnTime) {
        this._minions.put(minion, false);
        if (minion.isRaidRelated() && respawnTime > 0 && !this._master.isAlikeDead()) {
            ThreadPool.schedule(() -> {
                if (!this._master.isAlikeDead() && this._master.isVisible() && !(Boolean) this._minions.get(minion)) {
                    minion.refreshID();
                    this.initializeNpcInstance(this._master, minion);
                }

            }, respawnTime);
        }

    }

    public void onAssist(Creature caller, Creature attacker) {
        if (attacker != null) {
            if (!this._master.isAlikeDead() && !this._master.isInCombat()) {
                this._master.addDamageHate(attacker, 0, 1);
            }

            boolean callerIsMaster = caller == this._master;
            int aggro = callerIsMaster ? 10 : 1;
            if (this._master.isRaidBoss()) {
                aggro *= 10;
            }

            for (Monster minion : this.getSpawnedMinions()) {
                if (!minion.isDead() && (callerIsMaster || !minion.isInCombat())) {
                    minion.addDamageHate(attacker, 0, aggro);
                }
            }

        }
    }

    public void onMasterTeleported() {
        for (Monster minion : this.getSpawnedMinions()) {
            if (!minion.isDead() && !minion.isMovementDisabled()) {
                minion.teleToMaster();
            }
        }

    }

    protected final Monster initializeNpcInstance(Monster master, Monster minion) {
        this._minions.put(minion, true);
        minion.setIsNoRndWalk(true);
        minion.stopAllEffects();
        minion.setIsDead(false);
        minion.setDecayed(false);
        minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
        int offset = (int) ((double) 100.0F + minion.getCollisionRadius() + master.getCollisionRadius());
        int minRadius = (int) (master.getCollisionRadius() + (double) 30.0F);
        int newX = Rnd.get(minRadius * 2, offset * 2);
        int newY = Rnd.get(newX, offset * 2);
        newY = (int) Math.sqrt(newY * newY - newX * newX);
        if (newX > offset + minRadius) {
            newX = master.getX() + newX - offset;
        } else {
            newX = master.getX() - newX + minRadius;
        }

        if (newY > offset + minRadius) {
            newY = master.getY() + newY - offset;
        } else {
            newY = master.getY() - newY + minRadius;
        }

        minion.spawnMe(newX, newY, master.getZ(), master.getHeading());
        return minion;
    }
}
