package net.sf.l2j.gameserver.model.location;

import net.sf.l2j.commons.util.StatSet;

public class WalkerLocation extends Location {
    private final int _delay;
    private final boolean _run;
    private final String _chat;

    public WalkerLocation(StatSet set, boolean run) {
        super(set.getInteger("X"), set.getInteger("Y"), set.getInteger("Z"));
        this._run = run;
        this._delay = set.getInteger("delay", 0) * 1000;
        this._chat = set.getString("chat", null);
    }

    public boolean doesNpcMustRun() {
        return this._run;
    }

    public int getDelay() {
        return this._delay;
    }

    public String getChat() {
        return this._chat;
    }
}
