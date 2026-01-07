package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.enums.Paperdoll;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CharSelectSlot {
    private static final CLogger LOGGER = new CLogger(CharSelectSlot.class.getName());

    private static final String RESTORE_PAPERDOLLS = "SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'";
    public final Player _player;
    public final PcInventory _inv;
    private final int _objectId;
    private final String _name;
    private final int[][] _paperdoll;
    private int _charId = 199546;
    private long _exp = 0L;
    private int _sp = 0;
    private int _clanId = 0;
    private int _race = 0;
    private int _classId = 0;
    private int _baseClassId = 0;
    private long _deleteTimer = 0L;
    private long _lastAccess = 0L;
    private int _face = 0;
    private int _hairStyle = 0;
    private int _hairColor = 0;
    private int _sex = 0;
    private int _level = 1;
    private int _maxHp = 0;
    private double _currentHp = 0.0D;
    private int _maxMp = 0;
    private double _currentMp = 0.0D;
    private int _karma = 0;
    private int _pkKills = 0;
    private int _pvpKills = 0;
    private int _augmentationId = 0;
    private int _x = 0;
    private int _y = 0;
    private int _z = 0;
    private int _accessLevel = 0;

    public CharSelectSlot(int objectId, String name) {
        this._objectId = objectId;
        this._name = name;

        _player = Player.restoreCharInfo(objectId);
        _inv = _player.getInventory();
        this._paperdoll = restoreVisibleInventory(objectId);
    }

    private static int[][] restoreVisibleInventory(int objectId) {
        int[][] paperdoll = new int[18][3];
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
                try {
                    ps.setInt(1, objectId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int slot = rs.getInt("loc_data");
                            paperdoll[slot][0] = rs.getInt("object_id");
                            paperdoll[slot][1] = rs.getInt("item_id");
                            paperdoll[slot][2] = rs.getInt("enchant_level");
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
            LOGGER.error("Couldn't restore paperdolls for {}.", e, Integer.valueOf(objectId));
        }
        return paperdoll;
    }

    public int getObjectId() {
        return this._objectId;
    }

    public int getAccessLevel() {
        return this._accessLevel;
    }

    public void setAccessLevel(int level) {
        this._accessLevel = level;
    }

    public int getCharId() {
        return this._charId;
    }

    public void setCharId(int charId) {
        this._charId = charId;
    }

    public int getClanId() {
        return this._clanId;
    }

    public void setClanId(int clanId) {
        this._clanId = clanId;
    }

    public int getClassId() {
        return this._classId;
    }

    public void setClassId(int classId) {
        this._classId = classId;
    }

    public int getBaseClassId() {
        return this._baseClassId;
    }

    public void setBaseClassId(int baseClassId) {
        this._baseClassId = baseClassId;
    }

    public double getCurrentHp() {
        return this._currentHp;
    }

    public void setCurrentHp(double currentHp) {
        this._currentHp = currentHp;
    }

    public double getCurrentMp() {
        return this._currentMp;
    }

    public void setCurrentMp(double currentMp) {
        this._currentMp = currentMp;
    }

    public long getDeleteTimer() {
        return this._deleteTimer;
    }

    public void setDeleteTimer(long deleteTimer) {
        this._deleteTimer = deleteTimer;
    }

    public long getLastAccess() {
        return this._lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this._lastAccess = lastAccess;
    }

    public long getExp() {
        return this._exp;
    }

    public void setExp(long exp) {
        this._exp = exp;
    }

    public int getFace() {
        return this._face;
    }

    public void setFace(int face) {
        this._face = face;
    }

    public int getHairColor() {
        return this._hairColor;
    }

    public void setHairColor(int hairColor) {
        this._hairColor = hairColor;
    }

    public int getHairStyle() {
        return this._hairStyle;
    }

    public void setHairStyle(int hairStyle) {
        this._hairStyle = hairStyle;
    }


    public int getPaperdollObjectId(Paperdoll slot) {
        return getPaperdollObjectId(slot.getId());
    }

    public int getPaperdollObjectId(int slot) {
        return _paperdoll[slot][0];
    }

    public int getPaperdollItemId(Paperdoll slot) {
        return getPaperdollItemId(slot.getId());
    }

    public int getPaperdollItemId(int slot) {
        return _paperdoll[slot][1];
    }

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public int getMaxHp() {
        return this._maxHp;
    }

    public void setMaxHp(int maxHp) {
        this._maxHp = maxHp;
    }

    public int getMaxMp() {
        return this._maxMp;
    }

    public void setMaxMp(int maxMp) {
        this._maxMp = maxMp;
    }

    public String getName() {
        return this._name;
    }

    public int getRace() {
        return this._race;
    }

    public void setRace(int race) {
        this._race = race;
    }

    public int getSex() {
        return this._sex;
    }

    public void setSex(int sex) {
        this._sex = sex;
    }

    public int getSp() {
        return this._sp;
    }

    public void setSp(int sp) {
        this._sp = sp;
    }

    public int getEnchantEffect() {
        return _paperdoll[Paperdoll.RHAND.getId()][2];
    }

    public int getKarma() {
        return this._karma;
    }

    public void setKarma(int k) {
        this._karma = k;
    }

    public int getAugmentationId() {
        return this._augmentationId;
    }

    public void setAugmentationId(int augmentationId) {
        this._augmentationId = augmentationId;
    }

    public int getPkKills() {
        return this._pkKills;
    }

    public void setPkKills(int PkKills) {
        this._pkKills = PkKills;
    }

    public int getPvPKills() {
        return this._pvpKills;
    }

    public void setPvPKills(int PvPKills) {
        this._pvpKills = PvPKills;
    }

    public int getX() {
        return this._x;
    }

    public void setX(int x) {
        this._x = x;
    }

    public int getY() {
        return this._y;
    }

    public void setY(int y) {
        this._y = y;
    }

    public int getZ() {
        return this._z;
    }

    public void setZ(int z) {
        this._z = z;
    }

}
