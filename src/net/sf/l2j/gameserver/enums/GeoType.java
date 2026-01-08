/**/
package net.sf.l2j.gameserver.enums;

public enum GeoType {
    L2J("%d_%d.l2j"),
    L2OFF("%d_%d_conv.dat"),
    L2D("%d_%d.l2d");

    private final String _filename;

    GeoType(String filename) {
        this._filename = filename;
    }

    public String getFilename() {
        return this._filename;
    }
}
