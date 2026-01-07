package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.CouponsManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestPCCafeCouponUse extends L2GameClientPacket {

    private String _code;

    @Override
    protected void readImpl() {
        _code = readS();
    }

    @Override
    protected String readS() {
        getStringBuffer().clear();

        while (_buf.remaining() >= 2) {
            char ch = _buf.getChar();
            if (ch == 0) {
                break;
            }
            getStringBuffer().append(ch);
        }

        return getStringBuffer().toString();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        if (_code == null || _code.length() != 20) {
            activeChar.sendMessage("Your code cannot be empty or less than 20 letters/digits");
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (CouponsManager.getInstance().tryUseCoupon(activeChar, _code))
            activeChar.sendMessage("Your coupon was activated, thank you for playing!");

    }

}
