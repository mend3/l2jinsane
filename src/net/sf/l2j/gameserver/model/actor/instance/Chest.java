package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public final class Chest extends Monster {
    private volatile boolean _isInteracted;

    private volatile boolean _specialDrop;

    public Chest(int objectId, NpcTemplate template) {
        super(objectId, template);
        setIsNoRndWalk(true);
        this._isInteracted = false;
        this._specialDrop = false;
    }

    public void onSpawn() {
        super.onSpawn();
        this._isInteracted = false;
        this._specialDrop = false;
    }

    public boolean isInteracted() {
        return this._isInteracted;
    }

    public void setInteracted() {
        this._isInteracted = true;
    }

    public boolean isSpecialDrop() {
        return this._specialDrop;
    }

    public void setSpecialDrop() {
        this._specialDrop = true;
    }

    public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker) {
        int id = getTemplate().getNpcId();
        if (!this._specialDrop)
            if (id >= 18265 && id <= 18286) {
                id += 3536;
            } else if (id == 18287 || id == 18288) {
                id = 21671;
            } else if (id == 18289 || id == 18290) {
                id = 21694;
            } else if (id == 18291 || id == 18292) {
                id = 21717;
            } else if (id == 18293 || id == 18294) {
                id = 21740;
            } else if (id == 18295 || id == 18296) {
                id = 21763;
            } else if (id == 18297 || id == 18298) {
                id = 21786;
            }
        super.doItemDrop(NpcData.getInstance().getTemplate(id), lastAttacker);
    }

    public boolean isMovementDisabled() {
        if (super.isMovementDisabled())
            return true;
        return !isInteracted();
    }

    public boolean hasRandomAnimation() {
        return false;
    }
}
