/**/
package net.sf.l2j.gameserver.enums;

public enum ScriptEventType {
    ON_FIRST_TALK(false),
    QUEST_START(true),
    ON_TALK(true),
    ON_ATTACK(true),
    ON_ATTACK_ACT(true),
    ON_KILL(true),
    ON_SPAWN(true),
    ON_DECAY(true),
    ON_CREATURE_SEE(true),
    ON_SKILL_SEE(true),
    ON_FACTION_CALL(true),
    ON_AGGRO(true),
    ON_SPELL_FINISHED(true),
    ON_ENTER_ZONE(true),
    ON_EXIT_ZONE(true);

    private final boolean _allowMultipleRegistration;

    ScriptEventType(boolean allowMultipleRegistration) {
        this._allowMultipleRegistration = allowMultipleRegistration;
    }

    public boolean isMultipleRegistrationAllowed() {
        return this._allowMultipleRegistration;
    }
}
