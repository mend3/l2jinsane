package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.skills.FlyType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public final class FlyToLocation extends L2GameServerPacket {
    private final int _destX;

    private final int _destY;

    private final int _destZ;

    private final int _chaObjId;

    private final int _chaX;

    private final int _chaY;

    private final int _chaZ;

    private final FlyType _type;

    public FlyToLocation(Creature cha, int destX, int destY, int destZ, FlyType type) {
        this._chaObjId = cha.getObjectId();
        this._chaX = cha.getX();
        this._chaY = cha.getY();
        this._chaZ = cha.getZ();
        this._destX = destX;
        this._destY = destY;
        this._destZ = destZ;
        this._type = type;
    }

    public FlyToLocation(Creature cha, WorldObject dest, FlyType type) {
        this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
    }

    protected void writeImpl() {
        writeC(197);
        writeD(this._chaObjId);
        writeD(this._destX);
        writeD(this._destY);
        writeD(this._destZ);
        writeD(this._chaX);
        writeD(this._chaY);
        writeD(this._chaZ);
        writeD(this._type.ordinal());
    }
}
