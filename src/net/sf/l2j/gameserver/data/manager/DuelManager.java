package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DuelManager {
    private final Map<Integer, Duel> _duels = new ConcurrentHashMap<>();

    public static DuelManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Duel getDuel(int duelId) {
        return this._duels.get(duelId);
    }

    public void addDuel(Player playerA, Player playerB, boolean isPartyDuel) {
        if (playerA == null || playerB == null)
            return;
        int duelId = IdFactory.getInstance().getNextId();
        this._duels.put(duelId, new Duel(playerA, playerB, isPartyDuel, duelId));
    }

    public void removeDuel(int duelId) {
        IdFactory.getInstance().releaseId(duelId);
        this._duels.remove(duelId);
    }

    public void doSurrender(Player player) {
        if (player == null || !player.isInDuel())
            return;
        Duel duel = getDuel(player.getDuelId());
        if (duel != null)
            duel.doSurrender(player);
    }

    public void onPlayerDefeat(Player player) {
        if (player == null || !player.isInDuel())
            return;
        Duel duel = getDuel(player.getDuelId());
        if (duel != null)
            duel.onPlayerDefeat(player);
    }

    public void onBuff(Player player, L2Effect buff) {
        if (player == null || !player.isInDuel() || buff == null)
            return;
        Duel duel = getDuel(player.getDuelId());
        if (duel != null)
            duel.onBuff(player, buff);
    }

    public void onPartyEdit(Player player) {
        if (player == null || !player.isInDuel())
            return;
        Duel duel = getDuel(player.getDuelId());
        if (duel != null)
            duel.onPartyEdit();
    }

    public void broadcastToOppositeTeam(Player player, L2GameServerPacket packet) {
        if (player == null || !player.isInDuel())
            return;
        Duel duel = getDuel(player.getDuelId());
        if (duel == null)
            return;
        if (duel.getPlayerA() == player) {
            duel.broadcastToTeam2(packet);
        } else if (duel.getPlayerB() == player) {
            duel.broadcastToTeam1(packet);
        } else if (duel.isPartyDuel()) {
            if (duel.getPlayerA().getParty() != null && duel.getPlayerA().getParty().containsPlayer(player)) {
                duel.broadcastToTeam2(packet);
            } else if (duel.getPlayerB().getParty() != null && duel.getPlayerB().getParty().containsPlayer(player)) {
                duel.broadcastToTeam1(packet);
            }
        }
    }

    private static class SingletonHolder {
        protected static final DuelManager INSTANCE = new DuelManager();
    }
}
