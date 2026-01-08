package net.sf.l2j.loginserver.network.serverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class ServerBasePacket {
    final ByteArrayOutputStream _bao = new ByteArrayOutputStream();

    protected void writeD(int value) {
        this._bao.write(value & 0xFF);
        this._bao.write(value >> 8 & 0xFF);
        this._bao.write(value >> 16 & 0xFF);
        this._bao.write(value >> 24 & 0xFF);
    }

    protected void writeH(int value) {
        this._bao.write(value & 0xFF);
        this._bao.write(value >> 8 & 0xFF);
    }

    protected void writeC(int value) {
        this._bao.write(value & 0xFF);
    }

    protected void writeF(double org) {
        long value = Double.doubleToRawLongBits(org);
        this._bao.write((int) (value & 0xFFL));
        this._bao.write((int) (value >> 8L & 0xFFL));
        this._bao.write((int) (value >> 16L & 0xFFL));
        this._bao.write((int) (value >> 24L & 0xFFL));
        this._bao.write((int) (value >> 32L & 0xFFL));
        this._bao.write((int) (value >> 40L & 0xFFL));
        this._bao.write((int) (value >> 48L & 0xFFL));
        this._bao.write((int) (value >> 56L & 0xFFL));
    }

    protected void writeS(String text) {
        try {
            if (text != null)
                this._bao.write(text.getBytes(StandardCharsets.UTF_16LE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this._bao.write(0);
        this._bao.write(0);
    }

    protected void writeB(byte[] array) {
        try {
            this._bao.write(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLength() {
        return this._bao.size() + 2;
    }

    public byte[] getBytes() {
        writeD(0);
        int padding = this._bao.size() % 8;
        if (padding != 0)
            for (int i = padding; i < 8; i++)
                writeC(0);
        return this._bao.toByteArray();
    }

    public abstract byte[] getContent();
}
