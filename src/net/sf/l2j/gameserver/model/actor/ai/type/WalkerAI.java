package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.data.xml.WalkerRouteData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Walker;
import net.sf.l2j.gameserver.model.location.WalkerLocation;
import net.sf.l2j.gameserver.taskmanager.WalkerTaskManager;

import java.util.List;

public class WalkerAI extends CreatureAI {
    private final List<WalkerLocation> _route;

    private int _index = 1;

    public WalkerAI(Creature creature) {
        super(creature);
        this._route = WalkerRouteData.getInstance().getWalkerRoute(getActor().getNpcId());
        if (!this._route.isEmpty())
            setIntention(IntentionType.MOVE_TO, this._route.get(this._index));
    }

    public Walker getActor() {
        return (Walker) super.getActor();
    }

    protected void onEvtArrived() {
        WalkerLocation node = this._route.get(this._index);
        if (node.getChat() != null)
            getActor().broadcastNpcSay(node.getChat());
        if (node.getDelay() > 0) {
            WalkerTaskManager.getInstance().add(getActor(), node.getDelay());
        } else {
            moveToNextPoint();
        }
    }

    public void moveToNextPoint() {
        if (this._index < this._route.size() - 1) {
            this._index++;
        } else {
            this._index = 0;
        }
        WalkerLocation node = this._route.get(this._index);
        if (node.doesNpcMustRun()) {
            getActor().setRunning();
        } else {
            getActor().setWalking();
        }
        setIntention(IntentionType.MOVE_TO, node);
    }
}
