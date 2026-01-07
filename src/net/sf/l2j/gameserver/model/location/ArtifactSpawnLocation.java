package net.sf.l2j.gameserver.model.location;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

/**
 * A datatype extending {@link SpawnLocation}, which handles a single HolyThing spawn point and its parameters.
 */
public class ArtifactSpawnLocation extends SpawnLocation {
    private final int _npcId;
    private final Castle _castle;

    private Npc _npc;

    public ArtifactSpawnLocation(int npcId, Castle castle) {
        super(SpawnLocation.DUMMY_SPAWNLOC);

        _npcId = npcId;
        _castle = castle;
    }

    public int getNpcId() {
        return _npcId;
    }

    public Npc getNpc() {
        return _npc;
    }

    public void spawnMe() {
        try {
            final L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(_npcId));
            spawn.setLoc(this);

            _npc = spawn.doSpawn(false);
            if (_npc != null)
                _npc.setResidence(_castle);
        } catch (Exception e) {
            // Do nothing.
        }
    }
}