package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.StringTokenizer;

public class AdminHeal implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_heal"};

    public void useAdminCommand(String command, Player player) {
        if (command.startsWith("admin_heal")) {
            Player player1 = null;
            WorldObject object = player.getTarget();
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
                String nameOrRadius = st.nextToken();
                Player target = World.getInstance().getPlayer(nameOrRadius);
                if (target != null) {
                    player1 = target;
                } else if (StringUtil.isDigit(nameOrRadius)) {
                    int radius = Integer.parseInt(nameOrRadius);
                    for (Creature creature : player.getKnownTypeInRadius(Creature.class, radius)) {
                        creature.setCurrentHpMp(creature.getMaxHp(), creature.getMaxMp());
                        if (creature instanceof Player)
                            creature.setCurrentCp(creature.getMaxCp());
                    }
                    player.sendMessage("You instant healed all characters within " + radius + " unit radius.");
                    return;
                }
            }
            if (player1 == null)
                player1 = player;
            if (player1 instanceof Creature) {
                Creature creature = player1;
                creature.setCurrentHpMp(creature.getMaxHp(), creature.getMaxMp());
                if (creature instanceof Player)
                    creature.setCurrentCp(creature.getMaxCp());
                player.sendMessage("You instant healed " + creature.getName() + ".");
            } else {
                player.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
