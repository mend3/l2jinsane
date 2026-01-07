package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Npc;

import java.util.List;

public class MonRaceInfo extends L2GameServerPacket {
    private final int _unknown1;

    private final int _unknown2;

    private final List<Npc> _monsters;

    private final int[][] _speeds;

    public MonRaceInfo(int unknown1, int unknown2, List<Npc> monsters, int[][] speeds) {
        this._unknown1 = unknown1;
        this._unknown2 = unknown2;
        this._monsters = monsters;
        this._speeds = speeds;
    }

    protected final void writeImpl() {
        writeC(221);
        writeD(this._unknown1);
        writeD(this._unknown2);
        writeD(8);
        for (int i = 0; i < 8; i++) {
            Npc npc = this._monsters.get(i);
            writeD(npc.getObjectId());
            writeD(npc.getTemplate().getNpcId() + 1000000);
            writeD(14107);
            writeD(181875 + 58 * (7 - i));
            writeD(-3566);
            writeD(12080);
            writeD(181875 + 58 * (7 - i));
            writeD(-3566);
            writeF(npc.getCollisionHeight());
            writeF(npc.getCollisionRadius());
            writeD(120);
            for (int j = 0; j < 20; j++) {
                if (this._unknown1 == 0) {
                    writeC(this._speeds[i][j]);
                } else {
                    writeC(0);
                }
            }
            writeD(0);
        }
    }
}
