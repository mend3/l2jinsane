package net.sf.l2j.commons.mmocore;

import net.sf.l2j.gameserver.model.location.Location;

public abstract class SendablePacket<T extends MMOClient<?>> extends AbstractPacket<T> {
    protected final void putInt(int value) {
        this._buf.putInt(value);
    }

    protected final void putDouble(double value) {
        this._buf.putDouble(value);
    }

    protected final void putFloat(float value) {
        this._buf.putFloat(value);
    }

    protected final void writeC(int data) {
        this._buf.put((byte) data);
    }

    protected final void writeF(double value) {
        this._buf.putDouble(value);
    }

    protected final void writeH(int value) {
        this._buf.putShort((short) value);
    }

    protected final void writeD(int value) {
        this._buf.putInt(value);
    }

    protected final void writeQ(long value) {
        this._buf.putLong(value);
    }

    protected final void writeB(byte[] data) {
        this._buf.put(data);
    }

    protected final void writeS(String text) {
        if (text != null) {
            int len = text.length();

            for (int i = 0; i < len; ++i) {
                this._buf.putChar(text.charAt(i));
            }
        }

        this._buf.putChar('\u0000');
    }

    protected final void writeLoc(Location loc) {
        this._buf.putInt(loc.getX());
        this._buf.putInt(loc.getY());
        this._buf.putInt(loc.getZ());
    }

    protected abstract void write();
}