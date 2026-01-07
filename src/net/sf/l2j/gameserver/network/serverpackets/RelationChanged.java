package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Playable;

public class RelationChanged extends L2GameServerPacket {
    public static final int RELATION_PVP_FLAG = 2;

    public static final int RELATION_HAS_KARMA = 4;

    public static final int RELATION_LEADER = 128;

    public static final int RELATION_INSIEGE = 512;

    public static final int RELATION_ATTACKER = 1024;

    public static final int RELATION_ALLY = 2048;

    public static final int RELATION_ENEMY = 4096;

    public static final int RELATION_MUTUAL_WAR = 32768;

    public static final int RELATION_1SIDED_WAR = 65536;

    private final int _objId;

    private final int _relation;

    private final int _autoAttackable;

    private final int _karma;

    private final int _pvpFlag;

    public RelationChanged(Playable cha, int relation, boolean autoattackable) {
        this._objId = cha.getObjectId();
        this._relation = relation;
        this._autoAttackable = autoattackable ? 1 : 0;
        this._karma = cha.getKarma();
        this._pvpFlag = cha.getPvpFlag();
    }

    protected final void writeImpl() {
        writeC(206);
        writeD(this._objId);
        writeD(this._relation);
        writeD(this._autoAttackable);
        writeD(this._karma);
        writeD(this._pvpFlag);
    }
}
