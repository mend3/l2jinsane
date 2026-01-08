package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.events.bossevent.BossEvent;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.*;

public class BossEventNpc extends Folk {
    public BossEventNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onAction(Player player) {
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(this.getObjectId(), 0));
            player.sendPacket(new ValidateLocation(this));
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            player.sendPacket(new MoveToPawn(player, this, 150));
            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            this.showMainWindow(player);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    private void showMainWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/mods/events/ktb/BossEvent.htm");
        html.replace("%objectId%", String.valueOf(this.getObjectId()));
        html.replace("%npcname%", this.getName());
        html.replace("%regCount%", String.valueOf(BossEvent.getInstance().eventPlayers.size()));
        player.sendPacket(html);
    }

    public void onBypassFeedback(Player activeChar, String command) {
        super.onBypassFeedback(activeChar, command);
        if (command.startsWith("register")) {
            if (BossEvent.getInstance().getState() != BossEvent.EventState.REGISTRATION) {
                activeChar.sendMessage("Boss Event is not running!");
                return;
            }

            if (!BossEvent.getInstance().isRegistered(activeChar)) {
                if (BossEvent.getInstance().addPlayer(activeChar)) {
                    activeChar.sendMessage("You have been successfully registered in Boss Event!");
                }
            } else if (BossEvent.getInstance().removePlayer(activeChar)) {
                activeChar.sendMessage("You have been successfully removed of Boss Event!");
            }
        }

    }
}
