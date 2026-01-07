package net.sf.l2j.gameserver.network.serverpackets;

public class Ride extends L2GameServerPacket {
    public static final int ACTION_MOUNT = 1;

    public static final int ACTION_DISMOUNT = 0;

    private final int _id;

    private final int _bRide;
    private final int _rideClassID;
    private int _rideType;

    public Ride(int id, int action, int rideClassId) {
        this._id = id;
        this._bRide = action;
        this._rideClassID = rideClassId + 1000000;
        switch (rideClassId) {
            case 12526:
            case 12527:
            case 12528:
                this._rideType = 1;
                break;
            case 12621:
                this._rideType = 2;
                break;
        }
    }

    public int getMountType() {
        return this._rideType;
    }

    protected final void writeImpl() {
        writeC(134);
        writeD(this._id);
        writeD(this._bRide);
        writeD(this._rideType);
        writeD(this._rideClassID);
    }
}
