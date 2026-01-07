package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;

import java.nio.BufferUnderflowException;

public class MoveBackwardToLocation extends L2GameClientPacket {
    private int _targetX;

    private int _targetY;

    private int _targetZ;

    private int _originX;

    private int _originY;

    private int _originZ;

    private int _moveMovement;

    protected void readImpl() {
        this._targetX = readD();
        this._targetY = readD();
        this._targetZ = readD();
        this._originX = readD();
        this._originY = readD();
        this._originZ = readD();
        try {
            this._moveMovement = readD();
        } catch (BufferUnderflowException e) {
            if (Config.L2WALKER_PROTECTION) {
                Player player = getClient().getPlayer();
                if (player != null)
                    player.logout(false);
            }
        }
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isOutOfControl()) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activeChar.getActiveEnchantItem() != null) {
            activeChar.setActiveEnchantItem(null);
            activeChar.sendPacket(EnchantResult.CANCELLED);
            activeChar.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ) {
            activeChar.sendPacket(new StopMove(activeChar));
            return;
        }
        this._targetZ = (int) (this._targetZ + activeChar.getCollisionHeight());
        if (activeChar.getTeleMode() > 0) {
            if (activeChar.getTeleMode() == 1)
                activeChar.setTeleMode(0);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            activeChar.teleportTo(this._targetX, this._targetY, this._targetZ, 0);
            return;
        }
        double dx = (this._targetX - this._originX);
        double dy = (this._targetY - this._originY);
        if (dx * dx + dy * dy > 9.801E7D) {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        activeChar.getAI().setIntention(IntentionType.MOVE_TO, new Location(this._targetX, this._targetY, this._targetZ));
    }
}
