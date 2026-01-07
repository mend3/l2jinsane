package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class GatekeeperZombies extends L2AttackableAIScript {
    public GatekeeperZombies() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addAggroRangeEnterId(22136);
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        if (player.getInventory().hasAtLeastOneItem(8064, 8065, 8067))
            return null;
        return super.onAggro(npc, player, isPet);
    }
}
