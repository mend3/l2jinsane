package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.HashMap;
import java.util.Map;

public class EventInformation implements Runnable {
    private final AbstractEvent event;

    private final String msg;

    private Map<String, Integer> replacements;

    public EventInformation(AbstractEvent event, String msg) {
        this.event = event;
        this.msg = msg;
        this.replacements = new HashMap<>();
    }

    public Map<String, Integer> getReplacements() {
        return this.replacements;
    }

    public void setReplacements(Map<String, Integer> val) {
        this.replacements = val;
    }

    public void addReplacement(String id, int value) {
        this.replacements.put(id, Integer.valueOf(value));
    }

    public void run() {
        String finalMsg = this.msg;
        for (String r : this.replacements.keySet())
            finalMsg = finalMsg.replaceAll(r, String.valueOf(this.replacements.get(r)));
        ExShowScreenMessage sm = new ExShowScreenMessage(finalMsg, 3000, 2, false);
        for (Player player : this.event.getPlayers())
            player.sendPacket(sm);
    }
}
