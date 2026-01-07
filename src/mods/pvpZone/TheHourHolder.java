package mods.pvpZone;

public class TheHourHolder {
    private final int _objId;
    private String _name = "";
    private int _kills;

    public TheHourHolder(String name, int kills, int objId) {
        this._name = name;
        this._kills = kills;
        this._objId = objId;
    }

    public void setPvpKills() {
        this._kills++;
    }

    public String getName() {
        return this._name;
    }

    public int getObj() {
        return this._objId;
    }

    public int getKills() {
        return this._kills;
    }
}
