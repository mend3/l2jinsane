package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.xml.HennaData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HennaList {
    public static final int MAX_HENNAS_AMOUNT = 3;
    private static final CLogger LOGGER = new CLogger(HennaList.class.getName());
    private static final int MAX_HENNA_STAT_VALUE = 5;
    private static final int HENNA_FIRST_SLOT_ID = 1;
    private final Player _owner;

    private final Henna[] _hennas = new Henna[3];

    private final int[] _stats = new int[(HennaType.values()).length];

    public HennaList(Player owner) {
        this._owner = owner;
    }

    private void recalculateStats() {
        int i;
        for (i = 0; i < this._stats.length; i++)
            this._stats[i] = 0;
        for (Henna henna : this._hennas) {
            if (henna != null) {
                this._stats[0] = this._stats[0] + henna.getINT();
                this._stats[1] = this._stats[1] + henna.getSTR();
                this._stats[2] = this._stats[2] + henna.getCON();
                this._stats[3] = this._stats[3] + henna.getMEN();
                this._stats[4] = this._stats[4] + henna.getDEX();
                this._stats[5] = this._stats[5] + henna.getWIT();
            }
        }
        for (i = 0; i < this._stats.length; i++)
            this._stats[i] = Math.min(this._stats[i], 5);
    }

    private int indexOf(Henna henna) {
        if (henna == null)
            return -1;
        for (int i = 0; i < this._hennas.length; i++) {
            Henna h = this._hennas[i];
            if (h != null && h.getSymbolId() == henna.getSymbolId())
                return i;
        }
        return -1;
    }

    private int getEmptySlotIndex() {
        for (int i = 0; i < getMaxSize(); i++) {
            if (this._hennas[i] == null)
                return i;
        }
        return -1;
    }

    public List<Henna> getHennas() {
        return Arrays.stream(this._hennas).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public int getStat(HennaType hennaType) {
        return this._stats[hennaType.ordinal()];
    }

    public void restore() {
        Henna[] hennas = new Henna[3];
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT slot, symbol_id FROM character_hennas WHERE char_obj_id = ? AND class_index = ?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, this._owner.getClassIndex());
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int slot = rs.getInt("slot");
                            int symbolId = rs.getInt("symbol_id");
                            if (slot < 1 || slot > 4) {
                                LOGGER.warn("{} has Henna on invalid slot {}.", this._owner.toString(), slot);
                                continue;
                            }
                            Henna henna = HennaData.getInstance().getHenna(symbolId);
                            if (henna == null) {
                                LOGGER.warn("{} has unknown Henna Symbol Id: {} in slot {}.", this._owner.toString(), symbolId, slot);
                                continue;
                            }
                            hennas[slot - 1] = henna;
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore hennas.", e);
        }
        System.arraycopy(hennas, 0, this._hennas, 0, hennas.length);
        recalculateStats();
    }

    public boolean isEmpty() {
        for (Henna h : this._hennas) {
            if (h != null)
                return false;
        }
        return true;
    }

    public int size() {
        int result = 0;
        for (Henna henna : this._hennas) {
            if (henna != null)
                result++;
        }
        return result;
    }

    public Henna getBySymbolId(int symbolId) {
        for (Henna h : this._hennas) {
            if (h != null && h.getSymbolId() == symbolId)
                return h;
        }
        return null;
    }

    public boolean isFull() {
        return (getEmptySlotsAmount() <= 0);
    }

    public int getEmptySlotsAmount() {
        int usedSlots = size();
        int maxSlots = getMaxSize();
        return Math.max(maxSlots - usedSlots, 0);
    }

    public int getMaxSize() {
        ClassId classId = this._owner.getClassId();
        if (classId.level() < 1)
            return 0;
        if (classId.level() == 1)
            return 2;
        return 3;
    }

    public boolean add(Henna henna) {
        int slot = getEmptySlotIndex();
        if (slot < 0)
            return false;
        this._hennas[slot] = henna;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, henna.getSymbolId());
                    ps.setInt(3, slot + 1);
                    ps.setInt(4, this._owner.getClassIndex());
                    ps.execute();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't add Henna.", e);
        }
        recalculateStats();
        return true;
    }

    public boolean remove(Henna henna) {
        int slot = indexOf(henna);
        if (slot < 0)
            return false;
        this._hennas[slot] = null;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, slot + 1);
                    ps.setInt(3, this._owner.getClassIndex());
                    ps.execute();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't remove Henna.", e);
        }
        recalculateStats();
        return true;
    }

    public boolean canBeUsedBy(Henna henna) {
        return henna.canBeUsedBy(this._owner);
    }

    public String toString() {
        return "HennaList{_owner=" + this._owner.toString() + ", _hennas=" + Arrays.toString(this._hennas) + ", _stats=" + Arrays.toString(this._stats) + "}";
    }
}
