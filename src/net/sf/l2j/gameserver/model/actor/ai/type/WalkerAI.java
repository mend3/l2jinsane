package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.data.xml.WalkerRouteData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Walker;
import net.sf.l2j.gameserver.model.location.WalkerLocation;
import net.sf.l2j.gameserver.taskmanager.WalkerTaskManager;

import java.util.List;

public class WalkerAI extends CreatureAI {
    private final List<WalkerLocation> _route = WalkerRouteData.getInstance().getWalkerRoute(this.getActor().getNpcId());
    private int _index = 1;

    public WalkerAI(Creature creature) {
        super(creature);
        if (!this._route.isEmpty()) {
            this.setIntention(IntentionType.MOVE_TO, this._route.get(this._index));
        }

    }

    public Walker getActor() {
        return (Walker) super.getActor();
    }

    protected void onEvtArrived() {
        WalkerLocation node = this._route.get(this._index);
        if (node.getChat() != null) {
            this.getActor().broadcastNpcSay(node.getChat());
        }

        if (node.getDelay() > 0) {
            WalkerTaskManager.getInstance().add(this.getActor(), node.getDelay());
        } else {
            this.moveToNextPoint();
        }

    }

    public void moveToNextPoint() {
        if (this._index < this._route.size() - 1) {
            ++this._index;
        } else {
            this._index = 0;
        }

        WalkerLocation node = this._route.get(this._index);
        if (node.doesNpcMustRun()) {
            this.getActor().setRunning();
        } else {
            this.getActor().setWalking();
        }

        this.setIntention(IntentionType.MOVE_TO, node);
    }
}
