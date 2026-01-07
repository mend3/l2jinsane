package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class FestivalMonster extends Monster {
    protected int _bonusMultiplier = 1;

    public FestivalMonster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void setOfferingBonus(int bonusMultiplier) {
        this._bonusMultiplier = bonusMultiplier;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return !(attacker instanceof FestivalMonster);
    }

    public boolean isAggressive() {
        return true;
    }

    public boolean hasRandomAnimation() {
        return false;
    }

    public void doItemDrop(NpcTemplate template, Creature attacker) {
        Player player = attacker.getActingPlayer();
        if (player == null || !player.isInParty())
            return;
        player.getParty().getLeader().addItem("Sign", 5901, this._bonusMultiplier, attacker, true);
        super.doItemDrop(template, attacker);
    }
}
