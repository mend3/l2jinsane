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
        return switch (this._check) {
            case RESTING -> (player == null) ? (!this._required) : ((player.isSitting() == this._required));
            case MOVING -> (character.isMoving() == this._required);
            case RUNNING -> (character.isMoving() == this._required && character.isRunning() == this._required);
            case RIDING -> (character.isRiding() == this._required);
            case FLYING -> (character.isFlying() == this._required);
            case BEHIND -> (character.isBehindTarget() == this._required);
            case FRONT -> (character.isInFrontOfTarget() == this._required);
            case OLYMPIAD -> (player == null) ? (!this._required) : ((player.isInOlympiadMode() == this._required));
        };
    }
}
