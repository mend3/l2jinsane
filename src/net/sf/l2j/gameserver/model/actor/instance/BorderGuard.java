package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.rift.DimensionalRift;

public class BorderGuard extends Folk {
    public BorderGuard(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        Party party = player.getParty();
        if (party != null) {
            DimensionalRift rift = party.getDimensionalRift();
            if (rift != null) {
                if (command.startsWith("ChangeRiftRoom")) {
                    rift.manualTeleport(player, this);
                } else if (command.startsWith("ExitRift")) {
                    rift.manualExitRift(player, this);
                }

            }
        }
    }

    public String getHtmlPath(int npcId, int val) {
        return "data/html/seven_signs/rift/GuardianOfBorder.htm";
    }
}
