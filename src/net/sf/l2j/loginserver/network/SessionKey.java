package net.sf.l2j.loginserver.network;

import net.sf.l2j.Config;

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

    public boolean checkLoginPair(int loginOk1, int loginOk2) {
        return (this.loginOkID1 == loginOk1 && this.loginOkID2 == loginOk2);
    }

    public boolean equals(SessionKey key) {
        if (Config.SHOW_LICENCE)
            return (this.playOkID1 == key.playOkID1 && this.loginOkID1 == key.loginOkID1 && this.playOkID2 == key.playOkID2 && this.loginOkID2 == key.loginOkID2);
        return (this.playOkID1 == key.playOkID1 && this.playOkID2 == key.playOkID2);
    }
}
