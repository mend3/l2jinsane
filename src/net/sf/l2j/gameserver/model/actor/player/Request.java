package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.L2GameClientPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.ScheduledFuture;

public class Request {
    private static final int REQUEST_TIMEOUT = 15000;

    private final Player _player;

    private Player _partner;

    private L2GameClientPacket _requestPacket;

    private ScheduledFuture<?> _requestTimer;

    public Request(Player player) {
        this._player = player;
    }

    public Player getPartner() {
        return this._partner;
    }

    private synchronized void setPartner(Player partner) {
        this._partner = partner;
    }

    public L2GameClientPacket getRequestPacket() {
        return this._requestPacket;
    }

    private synchronized void setRequestPacket(L2GameClientPacket packet) {
        this._requestPacket = packet;
    }

    private void clear() {
        this._partner = null;
        this._requestPacket = null;
    }

    public synchronized boolean setRequest(Player partner, L2GameClientPacket packet) {
        if (partner == null) {
            this._player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            return false;
        }
        if (partner.getRequest().isProcessingRequest()) {
            this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(partner));
            return false;
        }
        if (isProcessingRequest()) {
            this._player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
            return false;
        }
        this._partner = partner;
        this._requestPacket = packet;
        clearRequestOnTimeout();
        this._partner.getRequest().setPartner(this._player);
        this._partner.getRequest().setRequestPacket(packet);
        this._partner.getRequest().clearRequestOnTimeout();
        return true;
    }

    private void clearRequestOnTimeout() {
        this._requestTimer = ThreadPool.schedule(this::clear, 15000L);
    }

    public void onRequestResponse() {
        if (this._requestTimer != null) {
            this._requestTimer.cancel(true);
            this._requestTimer = null;
        }
        clear();
        if (this._partner != null)
            this._partner.getRequest().clear();
    }

    public boolean isProcessingRequest() {
        return (this._partner != null);
    }
}
