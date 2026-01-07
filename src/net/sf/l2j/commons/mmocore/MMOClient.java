package net.sf.l2j.commons.mmocore;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection<?>> {
    private final T _con;

    public MMOClient(T con) {
        this._con = con;
    }

    public T getConnection() {
        return this._con;
    }

    public abstract boolean decrypt(ByteBuffer paramByteBuffer, int paramInt);

    public abstract boolean encrypt(ByteBuffer paramByteBuffer, int paramInt);

    protected abstract void onDisconnection();

    protected abstract void onForcedDisconnection();
}
