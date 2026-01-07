/**/
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class Attack extends L2GameServerPacket {
    public static final int HITFLAG_USESS = 16;
    public static final int HITFLAG_CRIT = 32;
    public static final int HITFLAG_SHLD = 64;
    public static final int HITFLAG_MISS = 128;
    public final boolean soulshot;
    public final int _ssGrade;
    private final int _attackerObjId;
    private final int _x;
    private final int _y;
    private final int _z;
    private Attack.Hit[] _hits;

    public Attack(Creature attacker, boolean useShots, int ssGrade) {
        this._attackerObjId = attacker.getObjectId();
        this.soulshot = useShots;
        this._ssGrade = ssGrade;
        this._x = attacker.getX();
        this._y = attacker.getY();
        this._z = attacker.getZ();
    }

    public Attack.Hit createHit(WorldObject target, int damage, boolean miss, boolean crit, byte shld) {
        return new Attack.Hit(this, target, damage, miss, crit, shld);
    }

    public void hit(Attack.Hit... hits) {
        if (this._hits == null) {
            this._hits = hits;
        } else {
            Attack.Hit[] tmp = new Attack.Hit[hits.length + this._hits.length];
            System.arraycopy(this._hits, 0, tmp, 0, this._hits.length);
            System.arraycopy(hits, 0, tmp, this._hits.length, hits.length);
            this._hits = tmp;
        }
    }

    public boolean hasHits() {
        return this._hits != null;
    }

    protected final void writeImpl() {
        this.writeC(5);
        this.writeD(this._attackerObjId);
        this.writeD(this._hits[0]._targetId);
        this.writeD(this._hits[0]._damage);
        this.writeC(this._hits[0]._flags);
        this.writeD(this._x);
        this.writeD(this._y);
        this.writeD(this._z);
        this.writeH(this._hits.length - 1);
        if (this._hits.length > 1) {
            for (int i = 1; i < this._hits.length; ++i) {
                this.writeD(this._hits[i]._targetId);
                this.writeD(this._hits[i]._damage);
                this.writeC(this._hits[i]._flags);
            }
        }

    }

    public class Hit {
        protected final int _targetId;
        protected final int _damage;
        protected int _flags;

        Hit(final Attack param1, WorldObject target, int damage, boolean miss, boolean crit, byte shld) {
            this._targetId = target.getObjectId();
            this._damage = damage;
            if (miss) {
                this._flags = 128;
            } else {
                if (soulshot) {
                    this._flags = 16 | _ssGrade;
                }

                if (crit) {
                    this._flags |= 32;
                }

                if (shld > 0 && (!(target instanceof Player) || !((Player) target).isInOlympiadMode())) {
                    this._flags |= 64;
                }

            }
        }
    }
}