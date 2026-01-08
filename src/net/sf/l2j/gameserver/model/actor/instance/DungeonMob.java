package net.sf.l2j.gameserver.model.actor.instance;

import mods.dungeon.Dungeon;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class DungeonMob extends Monster {
    private Dungeon dungeon;

    public DungeonMob(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this.dungeon != null) {
                ThreadPool.schedule(() -> this.dungeon.onMobKill(this), 2000L);
            }

            return true;
        }
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }
}
