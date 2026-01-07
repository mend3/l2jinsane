package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.ClassId;

public final class SubClass {
    private final int _classIndex;
    private ClassId _class;
    private long _exp;

    private int _sp;

    private byte _level;

    public SubClass(int classId, int classIndex, long exp, int sp, byte level) {
        this._class = ClassId.VALUES[classId];
        this._classIndex = classIndex;
        this._exp = exp;
        this._sp = sp;
        this._level = level;
    }

    public SubClass(int classId, int classIndex) {
        this._class = ClassId.VALUES[classId];
        this._classIndex = classIndex;
        this._exp = Experience.LEVEL[40];
        this._sp = 0;
        this._level = 40;
    }

    public ClassId getClassDefinition() {
        return this._class;
    }

    public int getClassId() {
        return this._class.getId();
    }

    public void setClassId(int classId) {
        this._class = ClassId.VALUES[classId];
    }

    public int getClassIndex() {
        return this._classIndex;
    }

    public long getExp() {
        return this._exp;
    }

    public void setExp(long exp) {
        if (exp > Experience.LEVEL[Config.SUBCLASS_MAX_LEVEL] - 1L)
            exp = Experience.LEVEL[Config.SUBCLASS_MAX_LEVEL] - 1L;
        this._exp = exp;
    }

    public int getSp() {
        return this._sp;
    }

    public void setSp(int sp) {
        this._sp = sp;
    }

    public byte getLevel() {
        return this._level;
    }

    public void setLevel(byte level) {
        if (level > Config.SUBCLASS_MAX_LEVEL - 1) {
            level = (byte) (Config.SUBCLASS_MAX_LEVEL - 1);
        } else if (level < 40) {
            level = 40;
        }
        this._level = level;
    }
}
