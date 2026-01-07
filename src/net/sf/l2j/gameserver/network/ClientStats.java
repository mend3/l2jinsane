package net.sf.l2j.gameserver.network;

import net.sf.l2j.Config;

public class ClientStats {
    private final int[] _packetsInSecond;
    private final int BUFFER_SIZE;
    public int processedPackets = 0;
    public int droppedPackets = 0;
    public int unknownPackets = 0;
    public int totalQueueSize = 0;
    public int maxQueueSize = 0;
    public int totalBursts = 0;
    public int maxBurstSize = 0;
    public int shortFloods = 0;
    public int longFloods = 0;
    public int totalQueueOverflows = 0;
    public int totalUnderflowExceptions = 0;
    private long _packetCountStartTick = 0L;
    private int _head;
    private int _totalCount = 0;
    private int _floodsInMin = 0;
    private long _floodStartTick = 0L;
    private int _unknownPacketsInMin = 0;
    private long _unknownPacketStartTick = 0L;
    private int _overflowsInMin = 0;
    private long _overflowStartTick = 0L;
    private int _underflowReadsInMin = 0;
    private long _underflowReadStartTick = 0L;
    private volatile boolean _floodDetected = false;
    private volatile boolean _queueOverflowDetected = false;

    public ClientStats() {
        this.BUFFER_SIZE = Config.CLIENT_PACKET_QUEUE_MEASURE_INTERVAL;
        this._packetsInSecond = new int[this.BUFFER_SIZE];
        this._head = this.BUFFER_SIZE - 1;
    }

    protected final boolean dropPacket() {
        boolean result = (this._floodDetected || this._queueOverflowDetected);
        if (result)
            this.droppedPackets++;
        return result;
    }

    protected final boolean countPacket(int queueSize) {
        this.processedPackets++;
        this.totalQueueSize += queueSize;
        if (this.maxQueueSize < queueSize)
            this.maxQueueSize = queueSize;
        if (this._queueOverflowDetected && queueSize < 2)
            this._queueOverflowDetected = false;
        return countPacket();
    }

    protected final boolean countUnknownPacket() {
        this.unknownPackets++;
        long tick = System.currentTimeMillis();
        if (tick - this._unknownPacketStartTick > 60000L) {
            this._unknownPacketStartTick = tick;
            this._unknownPacketsInMin = 1;
            return false;
        }
        this._unknownPacketsInMin++;
        return (this._unknownPacketsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN);
    }

    protected final boolean countBurst(int count) {
        if (count > this.maxBurstSize)
            this.maxBurstSize = count;
        if (count < Config.CLIENT_PACKET_QUEUE_MAX_BURST_SIZE)
            return false;
        this.totalBursts++;
        return true;
    }

    protected final boolean countQueueOverflow() {
        this._queueOverflowDetected = true;
        this.totalQueueOverflows++;
        long tick = System.currentTimeMillis();
        if (tick - this._overflowStartTick > 60000L) {
            this._overflowStartTick = tick;
            this._overflowsInMin = 1;
            return false;
        }
        this._overflowsInMin++;
        return (this._overflowsInMin > Config.CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN);
    }

    protected final boolean countUnderflowException() {
        this.totalUnderflowExceptions++;
        long tick = System.currentTimeMillis();
        if (tick - this._underflowReadStartTick > 60000L) {
            this._underflowReadStartTick = tick;
            this._underflowReadsInMin = 1;
            return false;
        }
        this._underflowReadsInMin++;
        return (this._underflowReadsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN);
    }

    protected final boolean countFloods() {
        return (this._floodsInMin > Config.CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN);
    }

    private final boolean longFloodDetected() {
        return (this._totalCount / this.BUFFER_SIZE > Config.CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND);
    }

    private final synchronized boolean countPacket() {
        this._totalCount++;
        long tick = System.currentTimeMillis();
        if (tick - this._packetCountStartTick > 1000L) {
            this._packetCountStartTick = tick;
            if (this._floodDetected && !longFloodDetected() && this._packetsInSecond[this._head] < Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND / 2)
                this._floodDetected = false;
            if (this._head <= 0)
                this._head = this.BUFFER_SIZE;
            this._head--;
            this._totalCount -= this._packetsInSecond[this._head];
            this._packetsInSecond[this._head] = 1;
            return this._floodDetected;
        }
        int count = this._packetsInSecond[this._head] = this._packetsInSecond[this._head] + 1;
        if (!this._floodDetected) {
            if (count > Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND) {
                this.shortFloods++;
            } else if (longFloodDetected()) {
                this.longFloods++;
            } else {
                return false;
            }
            this._floodDetected = true;
            if (tick - this._floodStartTick > 60000L) {
                this._floodStartTick = tick;
                this._floodsInMin = 1;
            } else {
                this._floodsInMin++;
            }
            return true;
        }
        return false;
    }
}
