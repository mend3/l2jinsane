package net.sf.l2j.gameserver.network;

public class SessionKey {
    public int playOkID1;

    public int playOkID2;

    public int loginOkID1;

    public int loginOkID2;

    public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2) {
        this.playOkID1 = playOK1;
        this.playOkID2 = playOK2;
        this.loginOkID1 = loginOK1;
        this.loginOkID2 = loginOK2;
    }

    public String toString() {
        return "PlayOk: " + this.playOkID1 + " " + this.playOkID2 + " LoginOk:" + this.loginOkID1 + " " + this.loginOkID2;
    }
}
