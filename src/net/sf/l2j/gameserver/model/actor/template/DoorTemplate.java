package net.sf.l2j.gameserver.model.actor.template;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.DoorType;
import net.sf.l2j.gameserver.enums.OpenType;

public class DoorTemplate extends CreatureTemplate {
    private final String _name;

    private final int _id;

    private final DoorType _type;

    private final int _level;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _geoX;

    private final int _geoY;

    private final int _geoZ;

    private final byte[][] _geoData;

    private final int _castle;

    private final int _triggeredId;

    private final boolean _opened;

    private final OpenType _openType;

    private final int _openTime;

    private final int _randomTime;

    private final int _closeTime;

    public DoorTemplate(StatSet stats) {
        super(stats);
        this._name = stats.getString("name");
        this._id = stats.getInteger("id");
        this._type = stats.getEnum("type", DoorType.class);
        this._level = stats.getInteger("level");
        this._x = stats.getInteger("posX");
        this._y = stats.getInteger("posY");
        this._z = stats.getInteger("posZ");
        this._geoX = stats.getInteger("geoX");
        this._geoY = stats.getInteger("geoY");
        this._geoZ = stats.getInteger("geoZ");
        this._geoData = stats.getObject("geoData", byte[][].class);
        this._castle = stats.getInteger("castle", 0);
        this._triggeredId = stats.getInteger("triggeredId", 0);
        this._opened = stats.getBool("opened", false);
        this._openType = stats.getEnum("openType", OpenType.class, OpenType.NPC);
        this._openTime = stats.getInteger("openTime", 0);
        this._randomTime = stats.getInteger("randomTime", 0);
        this._closeTime = stats.getInteger("closeTime", 0);
    }

    public final String getName() {
        return this._name;
    }

    public final int getId() {
        return this._id;
    }

    public final DoorType getType() {
        return this._type;
    }

    public final int getLevel() {
        return this._level;
    }

    public final int getPosX() {
        return this._x;
    }

    public final int getPosY() {
        return this._y;
    }

    public final int getPosZ() {
        return this._z;
    }

    public final int getGeoX() {
        return this._geoX;
    }

    public final int getGeoY() {
        return this._geoY;
    }

    public final int getGeoZ() {
        return this._geoZ;
    }

    public final byte[][] getGeoData() {
        return this._geoData;
    }

    public final int getCastle() {
        return this._castle;
    }

    public final int getTriggerId() {
        return this._triggeredId;
    }

    public final boolean isOpened() {
        return this._opened;
    }

    public final OpenType getOpenType() {
        return this._openType;
    }

    public final int getOpenTime() {
        return this._openTime;
    }

    public final int getRandomTime() {
        return this._randomTime;
    }

    public final int getCloseTime() {
        return this._closeTime;
    }
}
