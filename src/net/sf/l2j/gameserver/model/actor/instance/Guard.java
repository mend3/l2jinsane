package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.List;

public final class Guard extends Attackable {
    public Guard(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isAutoAttackable(Creature attacker) {
        return attacker instanceof Monster;
    }

    public void onSpawn() {
        this.setIsNoRndWalk(true);
        super.onSpawn();
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + val;
        }

        return "data/html/guard/" + filename + ".htm";
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            switch (this.getNpcId()) {
                case 30733:
                case 31032:
                case 31033:
                case 31034:
                case 31035:
                case 31036:
                case 31671:
                case 31672:
                case 31673:
                case 31674:
                    return;
            }

            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            List<Quest> scripts = this.getTemplate().getEventQuests(ScriptEventType.QUEST_START);
            if (scripts != null && !scripts.isEmpty()) {
                player.setLastQuestNpcObject(this.getObjectId());
            }

            scripts = this.getTemplate().getEventQuests(ScriptEventType.ON_FIRST_TALK);
            if (scripts != null && scripts.size() == 1) {
                scripts.getFirst().notifyFirstTalk(this, player);
            } else {
                this.showChatWindow(player);
            }
        }

    }

    public boolean isGuard() {
        return true;
    }

    public int getDriftRange() {
        return 20;
    }
}
