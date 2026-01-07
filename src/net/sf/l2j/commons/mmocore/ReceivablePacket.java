package net.sf.l2j.commons.mmocore;

import java.nio.ByteBuffer;

public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable {
    NioNetStringBuffer _sbuf;

    protected NioNetStringBuffer getStringBuffer() {
        return _sbuf;
    }

    protected abstract boolean read();

    public abstract void run();

    protected final void readB(byte[] dst) {
        this._buf.get(dst);
    }

    protected final void readB(byte[] dst, int offset, int len) {
        this._buf.get(dst, offset, len);
    }

    protected final int readC() {
        return this._buf.get() & 0xFF;
    }

    protected final int readH() {
        return this._buf.getShort() & 0xFFFF;
    }

    protected final int readD() {
        return this._buf.getInt();
    }

    protected final long readQ() {
        return this._buf.getLong();
    }

    protected final double readF() {
        return this._buf.getDouble();
    }


    protected String readS() {
        this._sbuf.clear();
        char ch;
        while ((ch = this._buf.getChar()) != '\000')
            this._sbuf.append(ch);
        return this._sbuf.toString();
    }

    public void setBuffers(ByteBuffer data, T client, NioNetStringBuffer sBuffer) {
        this._buf = data;
        this._client = client;
        this._sbuf = sBuffer;
    }
}
