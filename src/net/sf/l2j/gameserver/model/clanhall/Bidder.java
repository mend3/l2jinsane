package net.sf.l2j.gameserver.model.clanhall;

public class Bidder extends Seller {
    private long _time;

    public Bidder(String name, String clanName, int bid, long time) {
        super(name, clanName, bid);
        this._time = time;
    }

    public long getTime() {
        return this._time;
    }

    public void setTime(long time) {
        this._time = time;
    }
}
