/**/
package net.sf.l2j.gameserver.enums;

public enum IntentionType {
    IDLE,
    ACTIVE,
    REST,
    ATTACK,
    CAST,
    MOVE_TO,
    FOLLOW,
    PICK_UP,
    INTERACT;

    // $FF: synthetic method
    private static IntentionType[] $values() {
        return new IntentionType[]{IDLE, ACTIVE, REST, ATTACK, CAST, MOVE_TO, FOLLOW, PICK_UP, INTERACT};
    }
}
