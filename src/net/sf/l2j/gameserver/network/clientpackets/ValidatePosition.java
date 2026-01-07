package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class ValidatePosition extends L2GameClientPacket {
    private int _x;

    private int _y;

    private int _z;

    private int _heading;

    private int _boatId;

    protected void readImpl() {
        this._x = readD();
        this._y = readD();
        this._z = readD();
        this._heading = readD();
        this._boatId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null || player.isTeleporting() || player.isInObserverMode())
            return;
        int realX = player.getX();
        int realY = player.getY();
        int realZ = player.getZ();
        if (this._x == 0 && this._y == 0)
            if (realX != 0)
                return;
        if (player.isInBoat()) {
            int i = this._x - player.getBoatPosition().getX();
            int j = this._y - player.getBoatPosition().getY();
            int k = this._z - player.getBoatPosition().getZ();
            double d = (i * i + j * j);
            if (d > 250000.0D)
                sendPacket(new GetOnVehicle(player.getObjectId(), this._boatId, player.getBoatPosition()));
            return;
        }
        if (player.isFalling(this._z))
            return;
        int dx = this._x - realX;
        int dy = this._y - realY;
        int dz = this._z - realZ;
        double diffSq = (dx * dx + dy * dy);
        if (player.isFlying() || player.isInsideZone(ZoneId.WATER)) {
            player.setXYZ(realX, realY, this._z);
            if (diffSq > 90000.0D)
                player.sendPacket(new ValidateLocation(player));
        } else if (diffSq < 360000.0D) {
            if (diffSq > 250000.0D || Math.abs(dz) > 200)
                if (Math.abs(dz) > 200 && Math.abs(dz) < 1500 && Math.abs(this._z - player.getClientZ()) < 800) {
                    player.setXYZ(realX, realY, this._z);
                    realZ = this._z;
                } else {
                    player.sendPacket(new ValidateLocation(player));
                }
        }
        player.setClientX(this._x);
        player.setClientY(this._y);
        player.setClientZ(this._z);
    }
}
