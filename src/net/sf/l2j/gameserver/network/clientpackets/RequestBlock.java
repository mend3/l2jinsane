package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestBlock extends L2GameClientPacket {
    private static final int BLOCK = 0;

    private static final int UNBLOCK = 1;

    private static final int BLOCKLIST = 2;

    private static final int ALLBLOCK = 3;

    private static final int ALLUNBLOCK = 4;

    private String _name;

    private int _type;

    protected void readImpl() {
        this._type = readD();
        if (this._type == 0 || this._type == 1)
            this._name = readS();
    }

    protected void runImpl() {
        int targetId;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        switch (this._type) {
            case 0:
            case 1:
                targetId = PlayerInfoTable.getInstance().getPlayerObjectId(this._name);
                if (targetId <= 0 || activeChar.getObjectId() == targetId) {
                    activeChar.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
                    return;
                }
                if (PlayerInfoTable.getInstance().getPlayerAccessLevel(targetId) > 0) {
                    activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
                    return;
                }
                if (this._type == 0) {
                    BlockList.addToBlockList(activeChar, targetId);
                } else {
                    BlockList.removeFromBlockList(activeChar, targetId);
                }
                return;
            case 2:
                BlockList.sendListToOwner(activeChar);
                return;
            case 3:
                activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
                BlockList.setBlockAll(activeChar, true);
                return;
            case 4:
                activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
                BlockList.setBlockAll(activeChar, false);
                return;
        }
        LOGGER.warn("Unknown block type detected: {}.", this._type);
    }
}
