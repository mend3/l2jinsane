package net.sf.l2j.commons.mmocore;

public interface IClientFactory<T extends MMOClient<?>> {
    T create(MMOConnection<T> paramMMOConnection);
}
