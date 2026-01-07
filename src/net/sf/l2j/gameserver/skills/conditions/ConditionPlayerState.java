package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.enums.skills.PlayerState;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerState extends Condition {
    private final PlayerState _check;

    private final boolean _required;

    public ConditionPlayerState(PlayerState check, boolean required) {
        this._check = check;
        this._required = required;
    }

    public boolean testImpl(Env env) {
        Creature character = env.getCharacter();
        Player player = env.getPlayer();
        switch (this._check) {
            case RESTING:
                return (player == null) ? (!this._required) : ((player.isSitting() == this._required));
            case MOVING:
                return (character.isMoving() == this._required);
            case RUNNING:
                return (character.isMoving() == this._required && character.isRunning() == this._required);
            case RIDING:
                return (character.isRiding() == this._required);
            case FLYING:
                return (character.isFlying() == this._required);
            case BEHIND:
                return (character.isBehindTarget() == this._required);
            case FRONT:
                return (character.isInFrontOfTarget() == this._required);
            case OLYMPIAD:
                return (player == null) ? (!this._required) : ((player.isInOlympiadMode() == this._required));
        }
        return !this._required;
    }
}
