package net.sf.l2j.commons.mmocore;

public interface IMMOExecutor<T extends MMOClient<?>> {
    void execute(ReceivablePacket<T> paramReceivablePacket);
}
