package net.sf.l2j.commons.mmocore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

public class MMOConnection<T extends MMOClient<?>> {
    private final SelectorThread<T> _selectorThread;

    private final Socket _socket;

    private final InetAddress _address;

    private final ReadableByteChannel _readableByteChannel;

    private final WritableByteChannel _writableByteChannel;

    private final int _port;

    private final NioNetStackList<SendablePacket<T>> _sendQueue;

    private final SelectionKey _selectionKey;

    private ByteBuffer _readBuffer;

    private ByteBuffer _primaryWriteBuffer;

    private ByteBuffer _secondaryWriteBuffer;

    private volatile boolean _pendingClose;

    private T _client;

    public MMOConnection(SelectorThread<T> selectorThread, Socket socket, SelectionKey key, boolean tcpNoDelay) {
        this._selectorThread = selectorThread;
        this._socket = socket;
        this._address = socket.getInetAddress();
        this._readableByteChannel = socket.getChannel();
        this._writableByteChannel = socket.getChannel();
        this._port = socket.getPort();
        this._selectionKey = key;
        this._sendQueue = new NioNetStackList<>();
        try {
            this._socket.setTcpNoDelay(tcpNoDelay);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public final T getClient() {
        return this._client;
    }

    final void setClient(T client) {
        this._client = client;
    }

    public final void sendPacket(SendablePacket<T> sp) {
        sp._client = this._client;
        if (this._pendingClose)
            return;
        synchronized (getSendQueue()) {
            this._sendQueue.addLast(sp);
        }
        if (!this._sendQueue.isEmpty())
            try {
                this._selectionKey.interestOps(this._selectionKey.interestOps() | 0x4);
            } catch (CancelledKeyException ignored) {
            }
    }

    final SelectionKey getSelectionKey() {
        return this._selectionKey;
    }

    public final InetAddress getInetAddress() {
        return this._address;
    }

    public final int getPort() {
        return this._port;
    }

    final void close() throws IOException {
        this._socket.close();
    }

    final int read(ByteBuffer buf) throws IOException {
        return this._readableByteChannel.read(buf);
    }

    final int write(ByteBuffer buf) throws IOException {
        return this._writableByteChannel.write(buf);
    }

    final void createWriteBuffer(ByteBuffer buf) {
        if (this._primaryWriteBuffer == null) {
            this._primaryWriteBuffer = this._selectorThread.getPooledBuffer();
            this._primaryWriteBuffer.put(buf);
        } else {
            ByteBuffer temp = this._selectorThread.getPooledBuffer();
            temp.put(buf);
            int remaining = temp.remaining();
            this._primaryWriteBuffer.flip();
            int limit = this._primaryWriteBuffer.limit();
            if (remaining >= this._primaryWriteBuffer.remaining()) {
                temp.put(this._primaryWriteBuffer);
                this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
                this._primaryWriteBuffer = temp;
            } else {
                this._primaryWriteBuffer.limit(remaining);
                temp.put(this._primaryWriteBuffer);
                this._primaryWriteBuffer.limit(limit);
                this._primaryWriteBuffer.compact();
                this._secondaryWriteBuffer = this._primaryWriteBuffer;
                this._primaryWriteBuffer = temp;
            }
        }
    }

    final boolean hasPendingWriteBuffer() {
        return (this._primaryWriteBuffer != null);
    }

    final void movePendingWriteBufferTo(ByteBuffer dest) {
        this._primaryWriteBuffer.flip();
        dest.put(this._primaryWriteBuffer);
        this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
        this._primaryWriteBuffer = this._secondaryWriteBuffer;
        this._secondaryWriteBuffer = null;
    }

    final ByteBuffer getReadBuffer() {
        return this._readBuffer;
    }

    final void setReadBuffer(ByteBuffer buf) {
        this._readBuffer = buf;
    }

    public final boolean isClosed() {
        return this._pendingClose;
    }

    final NioNetStackList<SendablePacket<T>> getSendQueue() {
        return this._sendQueue;
    }

    public final void close(SendablePacket<T> sp) {
        if (this._pendingClose)
            return;
        synchronized (getSendQueue()) {
            if (!this._pendingClose) {
                this._pendingClose = true;
                this._sendQueue.clear();
                this._sendQueue.addLast(sp);
            }
        }
        try {
            this._selectionKey.interestOps(this._selectionKey.interestOps() & 0xFFFFFFFB);
        } catch (CancelledKeyException ignored) {
        }
        this._selectorThread.closeConnection(this);
    }

    final void releaseBuffers() {
        if (this._primaryWriteBuffer != null) {
            this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
            this._primaryWriteBuffer = null;
            if (this._secondaryWriteBuffer != null) {
                this._selectorThread.recycleBuffer(this._secondaryWriteBuffer);
                this._secondaryWriteBuffer = null;
            }
        }
        if (this._readBuffer != null) {
            this._selectorThread.recycleBuffer(this._readBuffer);
            this._readBuffer = null;
        }
    }
}
