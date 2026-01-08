package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public final class HalishaChest extends Monster {
    public HalishaChest(int objectId, NpcTemplate template) {
        super(objectId, template);
        this.setIsNoRndWalk(true);
        this.setShowSummonAnimation(true);
        this.disableCoreAI(true);
    }

    public boolean isMovementDisabled() {
        return true;
    }

    public boolean hasRandomAnimation() {
        return false;
    }
}
