package net.sf.l2j.gameserver.hwid.crypt;

public interface ProtectionCrypt {
    void setup(byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2);

    void crypt(byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
}
