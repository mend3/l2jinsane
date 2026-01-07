package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.FishingChampionshipManager;
import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestExFishRanking extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
            FishingChampionshipManager.getInstance().showMidResult(player);
    }
}
