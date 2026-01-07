package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

public final class ServerRestart extends ScheduledQuest {
    private static final int PERIOD = 600;

    public ServerRestart() {
        super(-1, "tasks");
    }

    public void onStart() {
        (new Shutdown(600, true)).start();
    }

    public void onEnd() {
    }
}
