package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;

public final class ServerObjectInfo extends L2GameServerPacket {
    private final Npc _npc;

    private final int _idTemplate;

    private final String _name;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _heading;

    private final double _collisionHeight;

    private final double _collisionRadius;

    private final boolean _isAttackable;

    public ServerObjectInfo(Npc npc, Creature actor) {
        this._npc = npc;
        this._idTemplate = this._npc.getTemplate().getIdTemplate();
        this._name = this._npc.getName();
        this._x = this._npc.getX();
        this._y = this._npc.getY();
        this._z = this._npc.getZ();
        this._heading = this._npc.getHeading();
        this._collisionHeight = this._npc.getCollisionHeight();
        this._collisionRadius = this._npc.getCollisionRadius();
        this._isAttackable = this._npc.isAutoAttackable(actor);
    }

    protected void writeImpl() {
        writeC(140);
        writeD(this._npc.getObjectId());
        writeD(this._idTemplate + 1000000);
        writeS(this._name);
        writeD(this._isAttackable ? 1 : 0);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._heading);
        writeF(1.0D);
        writeF(1.0D);
        writeF(this._collisionRadius);
        writeF(this._collisionHeight);
        writeD((int) (this._isAttackable ? this._npc.getCurrentHp() : 0.0D));
        writeD(this._isAttackable ? this._npc.getMaxHp() : 0);
        writeD(1);
        writeD(0);
    }
}
