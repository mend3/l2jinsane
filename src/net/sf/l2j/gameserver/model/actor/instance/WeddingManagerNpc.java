package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CoupleManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.StringTokenizer;

public class WeddingManagerNpc extends Folk {
    public WeddingManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public static void justMarried(Player requester, Player partner) {
        requester.setUnderMarryRequest(false);
        partner.setUnderMarryRequest(false);
        requester.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.getCurrentFolk(), true);
        partner.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.getCurrentFolk(), true);
        requester.sendMessage("Congratulations, you are now married with " + partner.getName() + " !");
        partner.sendMessage("Congratulations, you are now married with " + requester.getName() + " !");
        requester.broadcastPacket(new MagicSkillUse(requester, requester, 2230, 1, 1, 0));
        partner.broadcastPacket(new MagicSkillUse(partner, partner, 2230, 1, 1, 0));
        requester.doCast(SkillTable.FrequentSkill.LARGE_FIREWORK.getSkill());
        partner.doCast(SkillTable.FrequentSkill.LARGE_FIREWORK.getSkill());
        World.announceToOnlinePlayers("Congratulations to " + requester.getName() + " and " + partner.getName() + "! They have been married.");
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat())
                player.getAI().setIntention(IntentionType.IDLE);
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (!Config.ALLOW_WEDDING) {
                sendHtmlMessage(player, "data/html/mods/wedding/disabled.htm");
            } else if (player.getCoupleId() > 0) {
                sendHtmlMessage(player, "data/html/mods/wedding/start2.htm");
            } else if (player.isUnderMarryRequest()) {
                sendHtmlMessage(player, "data/html/mods/wedding/waitforpartner.htm");
            } else {
                sendHtmlMessage(player, "data/html/mods/wedding/start.htm");
            }
        }
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("AskWedding")) {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            if (st.hasMoreTokens()) {
                Player partner = World.getInstance().getPlayer(st.nextToken());
                if (partner == null) {
                    sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm");
                    return;
                }
                if (!weddingConditions(player, partner))
                    return;
                player.setUnderMarryRequest(true);
                partner.setUnderMarryRequest(true);
                partner.setRequesterId(player.getObjectId());
                partner.sendPacket((new ConfirmDlg(1983)).addString(player.getName() + " asked you to marry. Do you want to start a new relationship ?"));
            } else {
                sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm");
            }
        } else if (command.startsWith("Divorce")) {
            CoupleManager.getInstance().deleteCouple(player.getCoupleId());
        } else if (command.startsWith("GoToLove")) {
            int partnerId = CoupleManager.getInstance().getPartnerId(player.getCoupleId(), player.getObjectId());
            if (partnerId == 0) {
                player.sendMessage("Your partner can't be found.");
                return;
            }
            Player partner = World.getInstance().getPlayer(partnerId);
            if (partner == null) {
                player.sendMessage("Your partner is not online.");
                return;
            }
            if (partner.isInJail() || partner.isInOlympiadMode() || partner.isInDuel() || partner.isFestivalParticipant() || (partner.isInParty() && partner.getParty().isInDimensionalRift()) || partner.isInObserverMode()) {
                player.sendMessage("Due to the current partner's status, the teleportation failed.");
                return;
            }
            if (partner.getClan() != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().isInProgress()) {
                player.sendMessage("As your partner is in siege, you can't go to him/her.");
                return;
            }
            player.teleportTo(partner.getX(), partner.getY(), partner.getZ(), 20);
        }
    }

    private boolean weddingConditions(Player requester, Player partner) {
        if (partner.getObjectId() == requester.getObjectId()) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_wrongtarget.htm");
            return false;
        }
        if (!Config.WEDDING_SAMESEX && partner.getAppearance().getSex() == requester.getAppearance().getSex()) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_sex.htm");
            return false;
        }
        if (!requester.getFriendList().contains(Integer.valueOf(partner.getObjectId()))) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_friendlist.htm");
            return false;
        }
        if (partner.getCoupleId() > 0) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_alreadymarried.htm");
            return false;
        }
        if (Config.WEDDING_FORMALWEAR && (!requester.isWearingFormalWear() || !partner.isWearingFormalWear())) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_noformal.htm");
            return false;
        }
        if (requester.getAdena() < Config.WEDDING_PRICE || partner.getAdena() < Config.WEDDING_PRICE) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_adena.htm");
            return false;
        }
        return true;
    }

    private void sendHtmlMessage(Player player, String file) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(file);
        html.replace("%objectId%", getObjectId());
        html.replace("%adenasCost%", StringUtil.formatNumber(Config.WEDDING_PRICE));
        html.replace("%needOrNot%", Config.WEDDING_FORMALWEAR ? "will" : "won't");
        player.sendPacket(html);
    }
}
