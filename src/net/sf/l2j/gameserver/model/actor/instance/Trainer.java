package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public final class Trainer extends Folk {
    public Trainer(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/trainer/" + filename + ".htm";
    }
}
