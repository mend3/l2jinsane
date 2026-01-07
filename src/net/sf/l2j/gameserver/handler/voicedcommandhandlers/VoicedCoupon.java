package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ShowPCCafeCouponShowUI;

public class VoicedCoupon implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = new String[]{"redeem"};

    private static void showHtml(Player activeChar) {
        activeChar.sendPacket(ShowPCCafeCouponShowUI.STATIC_PACKET);
        activeChar.showPcBangWindow();
    }

    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if (command.equals("redeem")) {
            showHtml(activeChar);
        }
        return true;
    }

    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
