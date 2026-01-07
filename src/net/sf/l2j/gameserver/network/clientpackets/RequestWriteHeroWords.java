package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestWriteHeroWords extends L2GameClientPacket {
    private String _heroWords;

    protected void readImpl() {
        this._heroWords = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null || !player.isHero())
            return;
        if (this._heroWords == null || this._heroWords.length() > 300)
            return;
        HeroManager.getInstance().setHeroMessage(player, this._heroWords);
    }
}
