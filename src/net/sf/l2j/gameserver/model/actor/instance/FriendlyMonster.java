package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class FriendlyMonster extends Attackable {
    public FriendlyMonster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isAutoAttackable(Creature attacker) {
        return attacker instanceof Player && ((Player) attacker).getKarma() > 0;
    }

    public boolean isAggressive() {
        return true;
    }
}
