package net.sf.l2j.commons.mmocore;

import java.nio.channels.SocketChannel;

public interface IAcceptFilter {
    boolean accept(SocketChannel paramSocketChannel);
}
