package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.sendPacket(new ExCursedWeaponList(CursedWeaponManager.getInstance().getCursedWeaponsIds()));
    }
}
