/**/
package net.sf.l2j.gameserver.enums;

public enum PetitionState {
    PENDING,
    RESPONDER_CANCEL,
    RESPONDER_MISSING,
    RESPONDER_REJECT,
    RESPONDER_COMPLETE,
    PETITIONER_CANCEL,
    PETITIONER_MISSING,
    IN_PROCESS,
    COMPLETED;

    // $FF: synthetic method
    private static PetitionState[] $values() {
        return new PetitionState[]{PENDING, RESPONDER_CANCEL, RESPONDER_MISSING, RESPONDER_REJECT, RESPONDER_COMPLETE, PETITIONER_CANCEL, PETITIONER_MISSING, IN_PROCESS, COMPLETED};
    }
}
