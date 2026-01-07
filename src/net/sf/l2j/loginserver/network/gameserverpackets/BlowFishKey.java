package net.sf.l2j.loginserver.network.gameserverpackets;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.loginserver.network.clientpackets.ClientBasePacket;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

public class BlowFishKey extends ClientBasePacket {
    private static final CLogger LOGGER = new CLogger(BlowFishKey.class.getName());

    byte[] _key;

    public BlowFishKey(byte[] decrypt, RSAPrivateKey privateKey) {
        super(decrypt);
        int size = readD();
        byte[] tempKey = readB(size);
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(2, privateKey);
            byte[] tempDecryptKey = rsaCipher.doFinal(tempKey);
            int i = 0;
            int len = tempDecryptKey.length;
            for (; i < len; i++) {
                if (tempDecryptKey[i] != 0)
                    break;
            }
            this._key = new byte[len - i];
            System.arraycopy(tempDecryptKey, i, this._key, 0, len - i);
        } catch (GeneralSecurityException e) {
            LOGGER.error("Couldn't decrypt blowfish key (RSA)", e);
        }
    }

    public byte[] getKey() {
        return this._key;
    }
}
