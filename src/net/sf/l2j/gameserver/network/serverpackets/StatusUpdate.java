package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.ArrayList;
import java.util.List;

public class StatusUpdate extends L2GameServerPacket {
    public static final int LEVEL = 1;

    public static final int EXP = 2;

    public static final int STR = 3;

    public static final int DEX = 4;

    public static final int CON = 5;

    public static final int INT = 6;

    public static final int WIT = 7;

    public static final int MEN = 8;

    public static final int CUR_HP = 9;

    public static final int MAX_HP = 10;

    public static final int CUR_MP = 11;

    public static final int MAX_MP = 12;

    public static final int SP = 13;

    public static final int CUR_LOAD = 14;

    public static final int MAX_LOAD = 15;

    public static final int P_ATK = 17;

    public static final int ATK_SPD = 18;

    public static final int P_DEF = 19;

    public static final int EVASION = 20;

    public static final int ACCURACY = 21;

    public static final int CRITICAL = 22;

    public static final int M_ATK = 23;

    public static final int CAST_SPD = 24;

    public static final int M_DEF = 25;

    public static final int PVP_FLAG = 26;

    public static final int KARMA = 27;

    public static final int CUR_CP = 33;

    public static final int MAX_CP = 34;

    private final int _objectId;

    private final List<IntIntHolder> _attributes;

    private Player _actor;

    public StatusUpdate(WorldObject object) {
        this._attributes = new ArrayList<>();
        this._objectId = object.getObjectId();
        if (object instanceof Player)
            this._actor = (Player) object;
    }

    public void addAttribute(int id, int level) {
        this._attributes.add(new IntIntHolder(id, level));
    }

    protected final void writeImpl() {
        writeC(14);
        if (this._actor != null) {
            writeD(this._actor.getObjectId());
            writeD(28);
            writeD(1);
            writeD(this._actor.getStat().getLevel());
            writeD(2);
            writeD((int) this._actor.getStat().getExp());
            writeD(3);
            writeD(this._actor.getStat().getSTR());
            writeD(4);
            writeD(this._actor.getStat().getDEX());
            writeD(5);
            writeD(this._actor.getStat().getCON());
            writeD(6);
            writeD(this._actor.getStat().getINT());
            writeD(7);
            writeD(this._actor.getStat().getWIT());
            writeD(8);
            writeD(this._actor.getStat().getMEN());
            writeD(9);
            writeD((int) this._actor.getStatus().getCurrentHp());
            writeD(10);
            writeD(this._actor.getStat().getMaxHp());
            writeD(11);
            writeD((int) this._actor.getStatus().getCurrentMp());
            writeD(12);
            writeD(this._actor.getStat().getMaxMp());
            writeD(13);
            writeD(this._actor.getSp());
            writeD(14);
            writeD(this._actor.getInventory().getTotalWeight());
            writeD(15);
            writeD(this._actor.getMaxLoad());
            writeD(17);
            writeD(this._actor.getStat().getPAtk(null));
            writeD(18);
            writeD(this._actor.getStat().getPAtkSpd());
            writeD(19);
            writeD(this._actor.getStat().getPDef(null));
            writeD(20);
            writeD(this._actor.getStat().getEvasionRate(null));
            writeD(21);
            writeD(this._actor.getStat().getAccuracy());
            writeD(22);
            writeD(this._actor.getStat().getCriticalHit(null, null));
            writeD(23);
            writeD(this._actor.getStat().getMAtk(null, null));
            writeD(24);
            writeD(this._actor.getStat().getMAtkSpd());
            writeD(25);
            writeD(this._actor.getStat().getMDef(null, null));
            writeD(26);
            writeD(this._actor.getPvpFlag());
            writeD(27);
            writeD(this._actor.getKarma());
            writeD(33);
            writeD((int) this._actor.getStatus().getCurrentCp());
            writeD(34);
            writeD(this._actor.getStat().getMaxCp());
        } else {
            writeD(this._objectId);
            writeD(this._attributes.size());
            for (IntIntHolder temp : this._attributes) {
                writeD(temp.getId());
                writeD(temp.getValue());
            }
        }
    }
}
