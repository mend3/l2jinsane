package net.sf.l2j.gameserver.hwid.hwidmanager;

public class HwidBanList {
    private final int _id;

    private String _hwid;

    public HwidBanList(int id) {
        this._id = id;
    }

    public int getId() {
        return this._id;
    }

    public String getHWID() {
        return this._hwid;
    }

    public void setHWIDBan(String hwid) {
        this._hwid = hwid;
    }
}
