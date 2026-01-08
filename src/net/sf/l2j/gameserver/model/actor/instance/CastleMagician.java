package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleMagician extends Folk {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;

    public CastleMagician(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static boolean validateGateCondition(Player clanLeader, Player player) {
        if (!clanLeader.isAlikeDead() && !clanLeader.isInStoreMode() && !clanLeader.isRooted() && !clanLeader.isInCombat() && !clanLeader.isInOlympiadMode() && !clanLeader.isFestivalParticipant() && !clanLeader.isInObserverMode() && !clanLeader.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            if (player.isIn7sDungeon()) {
                CabalType targetCabal = SevenSignsManager.getInstance().getPlayerCabal(clanLeader.getObjectId());
                if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    if (targetCabal != SevenSignsManager.getInstance().getCabalHighestScore()) {
                        player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
                        return false;
                    }
                } else if (targetCabal == CabalType.NORMAL) {
                    player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
                    return false;
                }
            }

            return true;
        } else {
            player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
            return false;
        }
    }

    public void showChatWindow(Player player, int val) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/castlemagician/magician-no.htm";
        int condition = this.validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "data/html/castlemagician/magician-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "data/html/castlemagician/magician.htm";
                } else {
                    filename = "data/html/castlemagician/magician-" + val + ".htm";
                }
            }
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("Chat")) {
            int val = 0;

            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            }

            this.showChatWindow(player, val);
        } else if (command.equals("gotoleader")) {
            if (player.getClan() != null) {
                Player clanLeader = player.getClan().getLeader().getPlayerInstance();
                if (clanLeader == null) {
                    return;
                }

                if (clanLeader.getFirstEffect(L2EffectType.CLAN_GATE) != null) {
                    if (!validateGateCondition(clanLeader, player)) {
                        return;
                    }

                    player.teleportTo(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), 0);
                    return;
                }

                String filename = "data/html/castlemagician/magician-nogate.htm";
                this.showChatWindow(player, filename);
            }

        } else {
            super.onBypassFeedback(player, command);
        }
    }

    protected int validateCondition(Player player) {
        if (this.getCastle() != null && player.getClan() != null) {
            if (this.getCastle().getSiegeZone().isActive()) {
                return 1;
            }

            if (this.getCastle().getOwnerId() == player.getClanId()) {
                return 2;
            }
        }

        return 0;
    }
}
