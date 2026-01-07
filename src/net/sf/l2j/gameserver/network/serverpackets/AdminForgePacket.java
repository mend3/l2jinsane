/**/
package net.sf.l2j.gameserver.network.serverpackets;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdminForgePacket extends L2GameServerPacket {
    private final List<AdminForgePacket.Part> _parts = new ArrayList();

    protected void writeImpl() {
        Iterator var1 = this._parts.iterator();

        while (var1.hasNext()) {
            AdminForgePacket.Part p = (AdminForgePacket.Part) var1.next();
            this.generate(p.b, p.str);
        }

    }

    public boolean generate(byte b, String string) {
        if (b != 67 && b != 99) {
            if (b != 68 && b != 100) {
                if (b != 72 && b != 104) {
                    if (b != 70 && b != 102) {
                        if (b != 83 && b != 115) {
                            if (b != 66 && b != 98 && b != 88 && b != 120) {
                                return false;
                            } else {
                                this.writeB((new BigInteger(string)).toByteArray());
                                return true;
                            }
                        } else {
                            this.writeS(string);
                            return true;
                        }
                    } else {
                        this.writeF(Double.parseDouble(string));
                        return true;
                    }
                } else {
                    this.writeH(Integer.decode(string));
                    return true;
                }
            } else {
                this.writeD(Integer.decode(string));
                return true;
            }
        } else {
            this.writeC(Integer.decode(string));
            return true;
        }
    }

    public void addPart(byte b, String string) {
        this._parts.add(new Part(this, b, string));
    }

    private static class Part {
        public byte b;
        public String str;

        public Part(final AdminForgePacket param1, byte bb, String string) {
            this.b = bb;
            this.str = string;
        }
    }
}