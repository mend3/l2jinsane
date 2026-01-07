/**/
package net.sf.l2j.commons.mmocore;

public final class NioNetStackList<E> {
    private final NioNetStackList<E>.NioNetStackNode _start = new NioNetStackList.NioNetStackNode(this);
    private final NioNetStackList<E>.NioNetStackNodeBuf _buf = new NioNetStackList.NioNetStackNodeBuf();
    private NioNetStackList<E>.NioNetStackNode _end = new NioNetStackList.NioNetStackNode(this);

    public NioNetStackList() {
        this.clear();
    }

    public void addLast(E elem) {
        NioNetStackList<E>.NioNetStackNode newEndNode = this._buf.removeFirst();
        this._end._value = elem;
        this._end._next = newEndNode;
        this._end = newEndNode;
    }

    public E removeFirst() {
        NioNetStackList<E>.NioNetStackNode old = this._start._next;
        E value = old._value;
        this._start._next = old._next;
        this._buf.addLast(old);
        return value;
    }

    public boolean isEmpty() {
        return this._start._next == this._end;
    }

    public void clear() {
        this._start._next = this._end;
    }

    private final class NioNetStackNode {
        private NioNetStackList<E>.NioNetStackNode _next;
        private E _value;

        private NioNetStackNode(final NioNetStackList param1) {
        }
    }

    private final class NioNetStackNodeBuf {
        private final NioNetStackList<E>.NioNetStackNode _start = NioNetStackList.this.new NioNetStackNode(NioNetStackList.this);
        private NioNetStackList<E>.NioNetStackNode _end = NioNetStackList.this.new NioNetStackNode(NioNetStackList.this);

        NioNetStackNodeBuf() {
            this._start._next = this._end;
        }

        void addLast(NioNetStackList<E>.NioNetStackNode node) {
            node._next = null;
            node._value = null;
            this._end._next = node;
            this._end = node;
        }

        NioNetStackList<E>.NioNetStackNode removeFirst() {
            if (this._start._next == this._end) {
                return NioNetStackList.this.new NioNetStackNode(NioNetStackList.this);
            } else {
                NioNetStackList<E>.NioNetStackNode old = this._start._next;
                this._start._next = old._next;
                return old;
            }
        }
    }
}