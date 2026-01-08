/**/
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartySpelled extends L2GameServerPacket {
    private final List<PartySpelled.Effect> _effects = new ArrayList<>();
    private final Creature _activeChar;

    public PartySpelled(Creature cha) {
        this._activeChar = cha;
    }

    protected final void writeImpl() {
        if (this._activeChar != null) {
            this.writeC(238);
            this.writeD(this._activeChar instanceof Servitor ? 2 : (this._activeChar instanceof Pet ? 1 : 0));
            this.writeD(this._activeChar.getObjectId());
            this.writeD(this._effects.size());

            for (Effect temp : this._effects) {
                this.writeD(temp._skillId);
                this.writeH(temp._dat);
                this.writeD(temp._duration / 1000);
            }

        }
    }

    public void addPartySpelledEffect(int skillId, int dat, int duration) {
        this._effects.add(new Effect(this, skillId, dat, duration));
    }

    private static class Effect {
        protected int _skillId;
        protected int _dat;
        protected int _duration;

        public Effect(final PartySpelled param1, int pSkillId, int pDat, int pDuration) {
            this._skillId = pSkillId;
            this._dat = pDat;
            this._duration = pDuration;
        }
    }
}