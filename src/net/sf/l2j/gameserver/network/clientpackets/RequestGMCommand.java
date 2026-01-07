package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.*;

public final class RequestGMCommand extends L2GameClientPacket {
    private String _targetName;

    private int _command;

    protected void readImpl() {
        this._targetName = readS();
        this._command = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (!activeChar.isGM() || !activeChar.getAccessLevel().allowAltG())
            return;
        Player target = World.getInstance().getPlayer(this._targetName);
        Clan clan = ClanTable.getInstance().getClanByName(this._targetName);
        if (target == null && (clan == null || this._command != 6))
            return;
        switch (this._command) {
            case 1:
                sendPacket(new GMViewCharacterInfo(target));
                sendPacket(new GMViewHennaInfo(target));
                break;
            case 2:
                if (target != null && target.getClan() != null)
                    sendPacket(new GMViewPledgeInfo(target.getClan(), target));
                break;
            case 3:
                sendPacket(new GMViewSkillInfo(target));
                break;
            case 4:
                sendPacket(new GMViewQuestList(target));
                break;
            case 5:
                sendPacket(new GMViewItemList(target));
                sendPacket(new GMViewHennaInfo(target));
                break;
            case 6:
                if (target != null) {
                    sendPacket(new GMViewWarehouseWithdrawList(target));
                    break;
                }
                sendPacket(new GMViewWarehouseWithdrawList(clan));
                break;
        }
    }
}
