package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestMagicSkillUse extends L2GameClientPacket {
    private boolean _ctrlPressed;
    private boolean _shiftPressed;
    private int _skillId;

    protected void readImpl() {
        this._skillId = readD();
        this._ctrlPressed = (readD() != 0);
        this._shiftPressed = (readC() != 0);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        L2Skill skill = player.getSkill(this._skillId);
        if (skill == null) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (skill.isToggle() && player.isMounted()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isOutOfControl()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isAttackingNow()) {
            player.getAI().setNextAction(new NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, () -> player.useMagic(skill, this._ctrlPressed, this._shiftPressed)));
        } else {
            player.useMagic(skill, this._ctrlPressed, this._shiftPressed);
        }
    }
}
