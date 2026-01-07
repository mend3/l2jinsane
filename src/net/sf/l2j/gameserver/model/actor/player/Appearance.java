package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.gameserver.enums.actors.Sex;

public final class Appearance {
    private byte _face;

    private byte _hairColor;

    private byte _hairStyle;

    private Sex _sex;

    private boolean _invisible = false;

    private int _nameColor = 16777215;

    private int _titleColor = 16777079;

    public Appearance(byte face, byte hColor, byte hStyle, Sex sex) {
        this._face = face;
        this._hairColor = hColor;
        this._hairStyle = hStyle;
        this._sex = sex;
    }

    public byte getFace() {
        return this._face;
    }

    public void setFace(int value) {
        this._face = (byte) value;
    }

    public byte getHairColor() {
        return this._hairColor;
    }

    public void setHairColor(int value) {
        this._hairColor = (byte) value;
    }

    public byte getHairStyle() {
        return this._hairStyle;
    }

    public void setHairStyle(int value) {
        this._hairStyle = (byte) value;
    }

    public Sex getSex() {
        return this._sex;
    }

    public void setSex(Sex sex) {
        this._sex = sex;
    }

    public boolean getInvisible() {
        return this._invisible;
    }

    public void setInvisible() {
        this._invisible = true;
    }

    public void setVisible() {
        this._invisible = false;
    }

    public int getNameColor() {
        return this._nameColor;
    }

    public void setNameColor(int nameColor) {
        this._nameColor = nameColor;
    }

    public void setNameColor(int red, int green, int blue) {
        this._nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
    }

    public int getTitleColor() {
        return this._titleColor;
    }

    public void setTitleColor(int titleColor) {
        this._titleColor = titleColor;
    }

    public void setTitleColor(int red, int green, int blue) {
        this._titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
    }
}
