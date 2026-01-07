package net.sf.l2j.gameserver.model.actor.ai;

import net.sf.l2j.gameserver.enums.IntentionType;

public class Desire {
    private IntentionType _intention = IntentionType.IDLE;

    private Object _firstParameter;

    private Object _secondParameter;

    public String toString() {
        return "Desire " + this._intention.toString() + ", with following parameters: " + this._firstParameter + " and " + this._secondParameter;
    }

    public IntentionType getIntention() {
        return this._intention;
    }

    public Object getFirstParameter() {
        return this._firstParameter;
    }

    public Object getSecondParameter() {
        return this._secondParameter;
    }

    public synchronized void update(IntentionType intention, Object firstParameter, Object secondParameter) {
        this._intention = intention;
        this._firstParameter = firstParameter;
        this._secondParameter = secondParameter;
    }

    public synchronized void update(Desire desire) {
        this._intention = desire.getIntention();
        this._firstParameter = desire.getFirstParameter();
        this._secondParameter = desire.getSecondParameter();
    }

    public synchronized void reset() {
        this._intention = IntentionType.IDLE;
        this._firstParameter = null;
        this._secondParameter = null;
    }

    public boolean isBlank() {
        return (this._intention == IntentionType.IDLE && this._firstParameter == null && this._secondParameter == null);
    }

    public boolean equals(IntentionType intention, Object param1, Object param2) {
        return (this._intention == intention && this._firstParameter == param1 && this._secondParameter == param2);
    }
}
