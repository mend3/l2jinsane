/**/
package net.sf.l2j.gameserver.enums;

public enum MessageType {
    EXPELLED,
    LEFT,
    NONE,
    DISCONNECTED;

    // $FF: synthetic method
    private static MessageType[] $values() {
        return new MessageType[]{EXPELLED, LEFT, NONE, DISCONNECTED};
    }
}
