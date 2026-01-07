/**/
package net.sf.l2j.gameserver.events.eventengine;

public enum EventState {
    INACTIVE,
    REGISTERING,
    TELEPORTING,
    RUNNING;

    // $FF: synthetic method
    private static EventState[] $values() {
        return new EventState[]{INACTIVE, REGISTERING, TELEPORTING, RUNNING};
    }
}