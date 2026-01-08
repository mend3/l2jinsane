package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class DawnPriest extends SignsPriest {
    public DawnPriest(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("Chat")) {
            this.showChatWindow(player);
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/seven_signs/";
        CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
        switch (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId())) {
            case DAWN:
                if (SevenSignsManager.getInstance().isCompResultsPeriod()) {
                    filename = filename + "dawn_priest_5.htm";
                } else if (SevenSignsManager.getInstance().isRecruitingPeriod()) {
                    filename = filename + "dawn_priest_6.htm";
                } else if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    if (winningCabal == CabalType.DAWN) {
                        if (winningCabal != SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS)) {
                            filename = filename + "dawn_priest_2c.htm";
                        } else {
                            filename = filename + "dawn_priest_2a.htm";
                        }
                    } else if (winningCabal == CabalType.NORMAL) {
                        filename = filename + "dawn_priest_2d.htm";
                    } else {
                        filename = filename + "dawn_priest_2b.htm";
                    }
                } else {
                    filename = filename + "dawn_priest_1b.htm";
                }
                break;
            case DUSK:
                if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    filename = filename + "dawn_priest_3a.htm";
                } else {
                    filename = filename + "dawn_priest_3b.htm";
                }
                break;
            default:
                if (SevenSignsManager.getInstance().isCompResultsPeriod()) {
                    filename = filename + "dawn_priest_5.htm";
                } else if (SevenSignsManager.getInstance().isRecruitingPeriod()) {
                    filename = filename + "dawn_priest_6.htm";
                } else if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
                    if (winningCabal == CabalType.DAWN) {
                        filename = filename + "dawn_priest_4.htm";
                    } else if (winningCabal == CabalType.NORMAL) {
                        filename = filename + "dawn_priest_2d.htm";
                    } else {
                        filename = filename + "dawn_priest_2b.htm";
                    }
                } else {
                    filename = filename + "dawn_priest_1a.htm";
                }
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }
}
