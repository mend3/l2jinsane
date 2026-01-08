package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public abstract class PlayableAI extends CreatureAI {
    public PlayableAI(Playable playable) {
        super(playable);
    }

    protected void onIntentionAttack(Creature target) {
        if (target instanceof Playable) {
            Player targetPlayer = target.getActingPlayer();
            Player actorPlayer = this._actor.getActingPlayer();
            if (!target.isInsideZone(ZoneId.PVP)) {
                if (targetPlayer.getProtectionBlessing() && actorPlayer.getLevel() - targetPlayer.getLevel() >= 10 && actorPlayer.getKarma() > 0) {
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                    this.clientActionFailed();
                    return;
                }

                if (actorPlayer.getProtectionBlessing() && targetPlayer.getLevel() - actorPlayer.getLevel() >= 10 && targetPlayer.getKarma() > 0) {
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                    this.clientActionFailed();
                    return;
                }
            }

            if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                this.clientActionFailed();
                return;
            }

            if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                this.clientActionFailed();
                return;
            }
        }

        super.onIntentionAttack(target);
    }

    protected void onIntentionCast(L2Skill skill, WorldObject target) {
        if (target instanceof Playable && skill.isOffensive()) {
            Player targetPlayer = target.getActingPlayer();
            Player actorPlayer = this._actor.getActingPlayer();
            if (!target.isInsideZone(ZoneId.PVP)) {
                if (targetPlayer.getProtectionBlessing() && actorPlayer.getLevel() - targetPlayer.getLevel() >= 10 && actorPlayer.getKarma() > 0) {
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                    this.clientActionFailed();
                    this._actor.setIsCastingNow(false);
                    return;
                }

                if (actorPlayer.getProtectionBlessing() && targetPlayer.getLevel() - actorPlayer.getLevel() >= 10 && targetPlayer.getKarma() > 0) {
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                    this.clientActionFailed();
                    this._actor.setIsCastingNow(false);
                    return;
                }
            }

            if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                this.clientActionFailed();
                this._actor.setIsCastingNow(false);
                return;
            }

            if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                this.clientActionFailed();
                this._actor.setIsCastingNow(false);
                return;
            }
        }

        super.onIntentionCast(skill, target);
    }
}
