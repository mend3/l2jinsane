package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

import java.util.concurrent.ScheduledFuture;

public class ChristmasTree extends Folk {
    public static final int SPECIAL_TREE_ID = 13007;
    private ScheduledFuture<?> _aiTask;

    public ChristmasTree(int objectId, NpcTemplate template) {
        super(objectId, template);
        if (template.getNpcId() == 13007 && !this.isInsideZone(ZoneId.TOWN)) {
            L2Skill recoveryAura = SkillTable.FrequentSkill.SPECIAL_TREE_RECOVERY_BONUS.getSkill();
            if (recoveryAura == null) {
                return;
            }

            this._aiTask = ThreadPool.scheduleAtFixedRate(() -> {
                for (Player player : this.getKnownTypeInRadius(Player.class, 200)) {
                    if (player.getFirstEffect(recoveryAura) == null) {
                        recoveryAura.getEffects(player, player);
                    }
                }

            }, 3000L, 3000L);
        }

    }

    public void deleteMe() {
        if (this._aiTask != null) {
            this._aiTask.cancel(true);
            this._aiTask = null;
        }

        super.deleteMe();
    }

    public void onAction(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
