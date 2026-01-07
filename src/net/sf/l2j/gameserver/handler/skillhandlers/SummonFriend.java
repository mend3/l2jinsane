package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SummonFriend implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.SUMMON_FRIEND};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        if (!player.checkSummonerStatus())
            return;
        if ((TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
            return;
        if ((CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
            return;
        if ((DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
            return;
        for (WorldObject obj : targets) {
            if (obj instanceof Player target) {
                if (activeChar != target)
                    if (player.checkSummonTargetStatus(target)) {
                        if ((TvTEventManager.getInstance().getActiveEvent() != null && TvTEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
                            return;
                        if ((CtfEventManager.getInstance().getActiveEvent() != null && CtfEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
                            return;
                        if ((DmEventManager.getInstance().getActiveEvent() != null && DmEventManager.getInstance().getActiveEvent().isInEvent(player)) || player.getDungeon() != null)
                            return;
                        if (!MathUtil.checkIfInRange(50, activeChar, target, false))
                            if (!target.teleportRequest(player, skill)) {
                                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addCharName(target));
                            } else if (skill.getId() == 1403) {
                                ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
                                confirm.addCharName(player);
                                confirm.addZoneName(activeChar.getPosition());
                                confirm.addTime(30000);
                                confirm.addRequesterId(player.getObjectId());
                                target.sendPacket(confirm);
                            } else {
                                target.teleportToFriend(player, skill);
                                target.teleportRequest(null, null);
                            }
                    }
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
