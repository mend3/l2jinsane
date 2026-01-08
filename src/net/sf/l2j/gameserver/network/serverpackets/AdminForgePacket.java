/**/
package net.sf.l2j.gameserver.network.serverpackets;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AdminForgePacket extends L2GameServerPacket {
    private final List<AdminForgePacket.Part> _parts = new ArrayList<>();

    protected void writeImpl() {

        for (Part p : this._parts) {
            this.generate(p.b, p.str);
        }

    }

    public void generate(byte b, String string) {
        if (b != 67 && b != 99) {
            if (b != 68 && b != 100) {
                if (b != 72 && b != 104) {
                    if (b != 70 && b != 102) {
                        if (b != 83 && b != 115) {
                            if (b != 66 && b != 98 && b != 88 && b != 120) {
                            } else {
                                this.writeB((new BigInteger(string)).toByteArray());
                            }
                        } else {
                            this.writeS(string);
                        }
                    } else {
                        this.writeF(Double.parseDouble(string));
                    }
                } else {
                    this.writeH(Integer.decode(string));
                }
            } else {
                this.writeD(Integer.decode(string));
            }
        } else {
            this.writeC(Integer.decode(string));
        }
    }

    public void addPart(byte b, String string) {
        this._parts.add(new Part(this, b, string));
    }

    private static class Part {
        public final byte b;
        public final String str;

        public Part(final AdminForgePacket param1, byte bb, String string) {
            this.b = bb;
            this.str = string;
        }
    }
}