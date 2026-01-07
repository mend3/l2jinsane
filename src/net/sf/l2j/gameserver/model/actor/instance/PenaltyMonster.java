package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class PenaltyMonster extends Monster {
    private Player _ptk;

    public PenaltyMonster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public Creature getMostHated() {
        if (this._ptk != null)
            return this._ptk;
        return super.getMostHated();
    }

    public void setPlayerToKill(Player ptk) {
        if (Rnd.get(100) <= 80)
            broadcastNpcSay("Your bait was too delicious! Now, I will kill you!");
        this._ptk = ptk;
        getAI().notifyEvent(AiEventType.AGGRESSION, this._ptk, Integer.valueOf(Rnd.get(1, 100)));
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer))
            return false;
        if (Rnd.get(100) <= 75)
            broadcastNpcSay("I will tell fish not to take your bait!");
        return true;
    }
}
