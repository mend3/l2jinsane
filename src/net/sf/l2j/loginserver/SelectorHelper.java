/**/
package net.sf.l2j.loginserver;

import net.sf.l2j.commons.mmocore.*;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.serverpackets.Init;
import net.sf.l2j.util.IPv4Filter;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SelectorHelper implements IMMOExecutor<LoginClient>, IClientFactory<LoginClient>, IAcceptFilter {
    private final ThreadPoolExecutor _generalPacketsThreadPool;
    private final IPv4Filter _ipv4filter;

    public SelectorHelper() {
        this._generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        this._ipv4filter = new IPv4Filter();
    }

    public void execute(ReceivablePacket<LoginClient> packet) {
        this._generalPacketsThreadPool.execute(packet);
    }

    public LoginClient create(MMOConnection<LoginClient> con) {
        LoginClient client = new LoginClient(con);
        client.sendPacket(new Init(client));
        return client;
    }

    public boolean accept(SocketChannel sc) {
        return this._ipv4filter.accept(sc) && !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
    }
}