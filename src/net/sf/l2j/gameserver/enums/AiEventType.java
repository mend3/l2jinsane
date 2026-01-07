/**/
package net.sf.l2j.gameserver.enums;

public enum AiEventType {
    THINK,
    ATTACKED,
    AGGRESSION,
    STUNNED,
    PARALYZED,
    SLEEPING,
    ROOTED,
    EVADED,
    READY_TO_ACT,
    ARRIVED,
    ARRIVED_BLOCKED,
    CANCEL,
    DEAD,
    FAKE_DEATH,
    CONFUSED,
    MUTED,
    AFRAID,
    FINISH_CASTING;

    // $FF: synthetic method
    private static AiEventType[] $values() {
        return new AiEventType[]{THINK, ATTACKED, AGGRESSION, STUNNED, PARALYZED, SLEEPING, ROOTED, EVADED, READY_TO_ACT, ARRIVED, ARRIVED_BLOCKED, CANCEL, DEAD, FAKE_DEATH, CONFUSED, MUTED, AFRAID, FINISH_CASTING};
    }
}
