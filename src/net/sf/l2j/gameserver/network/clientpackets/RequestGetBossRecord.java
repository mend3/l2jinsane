package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExGetBossRecord;

import java.util.Map;

public class RequestGetBossRecord extends L2GameClientPacket {
    private int _bossId;

    protected void readImpl() {
        this._bossId = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        int points = RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId());
        int ranking = RaidPointManager.getInstance().calculateRanking(player.getObjectId());
        Map<Integer, Integer> list = RaidPointManager.getInstance().getList(player);
        player.sendPacket(new ExGetBossRecord(ranking, points, list));
    }
}
