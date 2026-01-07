package net.sf.l2j.commons.mmocore;

import java.nio.BufferOverflowException;

public final class NioNetStringBuffer {
    private final char[] _buf;

    private final int _size;

    private int _len;

    public NioNetStringBuffer(int size) {
        this._buf = new char[size];
        this._size = size;
        this._len = 0;
    }

    public void clear() {
        this._len = 0;
    }

    public void append(char c) {
        if (this._len < this._size) {
            this._buf[this._len++] = c;
        } else {
            throw new BufferOverflowException();
        }
    }

    public String toString() {
        return new String(this._buf, 0, this._len);
    }
}
