package net.sf.l2j.commons.mmocore;

import java.nio.ByteBuffer;

public interface IPacketHandler<T extends MMOClient<?>> {
    ReceivablePacket<T> handlePacket(ByteBuffer paramByteBuffer, T paramT);
}
