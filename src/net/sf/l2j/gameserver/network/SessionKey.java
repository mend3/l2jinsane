package net.sf.l2j.gameserver.network;

public record SessionKey(int loginOkID1, int loginOkID2, int playOkID1, int playOkID2) {

    public String toString() {
        return "PlayOk: " + this.playOkID1 + " " + this.playOkID2 + " LoginOk:" + this.loginOkID1 + " " + this.loginOkID2;
    }
}
