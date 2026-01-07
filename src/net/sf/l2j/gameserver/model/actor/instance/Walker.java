package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.WalkerAI;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class Walker extends Folk {
    public Walker(int objectId, NpcTemplate template) {
        super(objectId, template);
        setAI(new WalkerAI(this));
    }

    public WalkerAI getAI() {
        return (WalkerAI) this._ai;
    }

    public void setAI(CreatureAI newAI) {
        if (!(this._ai instanceof WalkerAI))
            this._ai = newAI;
    }

    public void detachAI() {
    }
}
