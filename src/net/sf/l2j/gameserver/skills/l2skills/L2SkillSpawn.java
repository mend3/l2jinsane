package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

import java.util.logging.Level;

public class L2SkillSpawn extends L2Skill {
    private final int _npcId;

    private final int _despawnDelay;

    private final boolean _summonSpawn;

    private final boolean _randomOffset;

    public L2SkillSpawn(StatSet set) {
        super(set);
        this._npcId = set.getInteger("npcId", 0);
        this._despawnDelay = set.getInteger("despawnDelay", 0);
        this._summonSpawn = set.getBool("isSummonSpawn", false);
        this._randomOffset = set.getBool("randomOffset", true);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        if (caster.isAlikeDead())
            return;
        if (this._npcId == 0) {
            _log.warning("NPC ID not defined for skill ID: " + getId());
            return;
        }
        NpcTemplate template = NpcData.getInstance().getTemplate(this._npcId);
        if (template == null) {
            _log.warning("Spawn of the nonexisting NPC ID: " + this._npcId + ", skill ID: " + getId());
            return;
        }
        try {
            L2Spawn spawn = new L2Spawn(template);
            int x = caster.getX();
            int y = caster.getY();
            if (this._randomOffset) {
                x += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
                y += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
            }
            spawn.setLoc(x, y, caster.getZ() + 20, caster.getHeading());
            spawn.setRespawnState(false);
            Npc npc = spawn.doSpawn(this._summonSpawn);
            if (this._despawnDelay > 0)
                npc.scheduleDespawn(this._despawnDelay);
        } catch (Exception e) {
            _log.log(Level.WARNING, "Exception while spawning NPC ID: " + this._npcId + ", skill ID: " + getId() + ", exception: " + e.getMessage(), e);
        }
    }
}
