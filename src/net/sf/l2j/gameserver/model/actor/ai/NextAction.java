package net.sf.l2j.gameserver.model.actor.ai;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;

public class NextAction {
    private final AiEventType _event;

    private final IntentionType _intention;

    private final Runnable _runnable;

    public NextAction(AiEventType event, IntentionType intention, Runnable runnable) {
        this._event = event;
        this._intention = intention;
        this._runnable = runnable;
    }

    public AiEventType getEvent() {
        return this._event;
    }

    public IntentionType getIntention() {
        return this._intention;
    }

    public void run() {
        this._runnable.run();
    }
}
