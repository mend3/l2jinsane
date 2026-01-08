/**/
package net.sf.l2j.gameserver.hwid.hwidmanager;

public class HWIDInfoList {
    private final int _id;
    private String HWID;
    private int count;
    private int playerID;
    private String login;
    private HWIDInfoList.LockType lockType;

    public HWIDInfoList(int id) {
        this._id = id;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getHWID() {
        return this.HWID;
    }

    public void setHWID(String HWID) {
        this.HWID = HWID;
    }

    public HWIDInfoList.LockType getLockType() {
        return this.lockType;
    }

    public void setLockType(HWIDInfoList.LockType lockType) {
        this.lockType = lockType;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int get_id() {
        return this._id;
    }

    public void setHwids(String hwid) {
        this.HWID = hwid;
        this.count = 1;
    }

    public enum LockType {
        PLAYER_LOCK,
        ACCOUNT_LOCK,
        NONE;
    }
}