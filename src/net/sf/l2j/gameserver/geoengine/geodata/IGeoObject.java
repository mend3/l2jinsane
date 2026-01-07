/**/
package net.sf.l2j.gameserver.geoengine.geodata;

public interface IGeoObject {
    int getGeoX();

    int getGeoY();

    int getGeoZ();

    int getHeight();

    byte[][] getObjectGeoData();
}