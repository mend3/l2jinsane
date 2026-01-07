package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.CursedWeapon;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ExCursedWeaponLocation;

import java.util.ArrayList;
import java.util.List;

public final class RequestCursedWeaponLocation extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        List<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<>();
        for (CursedWeapon cw : CursedWeaponManager.getInstance().getCursedWeapons()) {
            if (!cw.isActive())
                continue;
            Location loc = cw.getWorldPosition();
            if (loc != null)
                list.add(new ExCursedWeaponLocation.CursedWeaponInfo(loc, cw.getItemId(), cw.isActivated() ? 1 : 0));
        }
        if (!list.isEmpty())
            player.sendPacket(new ExCursedWeaponLocation(list));
    }
}
