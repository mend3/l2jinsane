package net.sf.l2j.gameserver.network.serverpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.PolyType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.pledge.Clan;

public abstract class AbstractNpcInfo extends L2GameServerPacket {
    protected int _x;

    protected int _y;

    protected int _z;

    protected int _heading;

    protected int _idTemplate;

    protected boolean _isAttackable;

    protected boolean _isSummoned;

    protected int _mAtkSpd;

    protected int _pAtkSpd;

    protected int _runSpd;

    protected int _walkSpd;

    protected int _rhand;

    protected int _lhand;

    protected int _chest;

    protected int _enchantEffect;

    protected double _collisionHeight;

    protected double _collisionRadius;

    protected int _clanCrest;

    protected int _allyCrest;

    protected int _allyId;

    protected int _clanId;

    protected String _name = "", _title = "";

    public AbstractNpcInfo(Creature cha) {
        this._isSummoned = cha.isShowSummonAnimation();
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
        this._heading = cha.getHeading();
        this._mAtkSpd = cha.getMAtkSpd();
        this._pAtkSpd = cha.getPAtkSpd();
        this._runSpd = cha.getStat().getBaseRunSpeed();
        this._walkSpd = cha.getStat().getBaseWalkSpeed();
    }

    public void write(char type, int value, int times) {
        for (int i = 0; i < times; i++) {
            switch (type) {
                case 'C':
                    writeC(value);
                    break;
                case 'D':
                    writeD(value);
                    break;
                case 'F':
                    writeF(value);
                    break;
                case 'H':
                    writeH(value);
                    break;
            }
        }
    }

    public static class NpcInfo extends AbstractNpcInfo {
        private final Npc _npc;

        public NpcInfo(Npc cha, Creature attacker) {
            super(cha);
            this._npc = cha;
            this._enchantEffect = this._npc.getEnchantEffect();
            this._isAttackable = this._npc.isAutoAttackable(attacker);
            if (this._npc.getPolyType() == PolyType.NPC) {
                this._idTemplate = this._npc.getPolyTemplate().getIdTemplate();
                this._rhand = this._npc.getPolyTemplate().getRightHand();
                this._lhand = this._npc.getPolyTemplate().getLeftHand();
                this._collisionHeight = this._npc.getPolyTemplate().getCollisionHeight();
                this._collisionRadius = this._npc.getPolyTemplate().getCollisionRadius();
            } else {
                this._idTemplate = this._npc.getTemplate().getIdTemplate();
                this._rhand = this._npc.getRightHandItemId();
                this._lhand = this._npc.getLeftHandItemId();
                this._collisionHeight = this._npc.getCollisionHeight();
                this._collisionRadius = this._npc.getCollisionRadius();
            }
            if (this._npc.getTemplate().isUsingServerSideName())
                this._name = this._npc.getName();
            if (this._npc.isChampion()) {
                this._title = "Champion";
            } else if (this._npc.getTemplate().isUsingServerSideTitle()) {
                this._title = this._npc.getTitle();
            }
            String title = EngineModsManager.onSeeNpcTitle(cha.getObjectId());
            if (title != null)
                this._title = title;
            if (Config.SHOW_NPC_LVL && this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Monster)
                this._title = "Lv " + this._npc.getLevel() + ((this._npc.getTemplate().getAggroRange() > 0) ? "* " : " ") + this._title;
            if (Config.SHOW_NPC_CREST && this._npc.getCastle() != null && this._npc.getCastle().getOwnerId() != 0) {
                Clan clan = ClanTable.getInstance().getClan(this._npc.getCastle().getOwnerId());
                this._clanCrest = clan.getCrestId();
                this._clanId = clan.getClanId();
                this._allyCrest = clan.getAllyCrestId();
                this._allyId = clan.getAllyId();
            }
        }

        protected void writeImpl() {
            FakePc fpc = this._npc.getFakePc();
            if (fpc != null) {
                writeC(3);
                writeD(this._x);
                writeD(this._y);
                writeD(this._z);
                writeD(this._heading);
                writeD(this._npc.getObjectId());
                writeS(fpc.name);
                writeD(fpc.race);
                writeD(fpc.sex);
                writeD(fpc.classId);
                writeD(0);
                writeD(0);
                writeD(fpc.rightHand);
                writeD(fpc.leftHand);
                writeD(fpc.gloves);
                writeD(fpc.chest);
                writeD(fpc.legs);
                writeD(fpc.feet);
                writeD(fpc.hair);
                writeD(fpc.rightHand);
                writeD(fpc.hair);
                writeD(fpc.hair2);
                write('H', 0, 24);
                writeD(0);
                writeD(0);
                writeD(this._mAtkSpd);
                writeD(this._pAtkSpd);
                writeD(0);
                writeD(0);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeF(this._npc.getMovementSpeedMultiplier());
                writeF(this._npc.getAttackSpeedMultiplier());
                writeF(fpc.radius);
                writeF(fpc.height);
                writeD(fpc.hairStyle);
                writeD(fpc.hairColor);
                writeD(fpc.face);
                if (this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Monster) {
                    writeS(fpc.title + " - HP " + fpc.title + "%");
                } else {
                    writeS(fpc.title);
                }
                writeD(fpc.clanId);
                writeD(fpc.clanCrest);
                writeD(fpc.allyId);
                writeD(fpc.allyCrest);
                writeD(0);
                writeC(1);
                writeC(this._npc.isRunning() ? 1 : 0);
                writeC(this._npc.isInCombat() ? 1 : 0);
                writeC(this._npc.isAlikeDead() ? 1 : 0);
                write('C', 0, 3);
                writeH(0);
                writeC(0);
                writeD(0);
                writeC(0);
                writeH(0);
                writeD(fpc.classId);
                writeD(0);
                writeD(0);
                writeC(fpc.enchant);
                writeC(0);
                writeD(0);
                writeC(0);
                writeC(fpc.hero);
                writeC(0);
                write('D', 0, 3);
                writeD(fpc.nameColor);
                writeD(this._heading);
                writeD(0);
                writeD(0);
                writeD(fpc.titleColor);
                writeD(0);
            } else {
                writeC(22);
                writeD(this._npc.getObjectId());
                writeD(this._idTemplate + 1000000);
                writeD(this._isAttackable ? 1 : 0);
                writeD(this._x);
                writeD(this._y);
                writeD(this._z);
                writeD(this._heading);
                writeD(0);
                writeD(this._mAtkSpd);
                writeD(this._pAtkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeD(this._runSpd);
                writeD(this._walkSpd);
                writeF(this._npc.getStat().getMovementSpeedMultiplier());
                writeF(this._npc.getStat().getAttackSpeedMultiplier());
                writeF(this._collisionRadius);
                writeF(this._collisionHeight);
                writeD(this._rhand);
                writeD(this._chest);
                writeD(this._lhand);
                writeC((this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Agathion) ? 0 : 1);
                writeC(this._npc.isRunning() ? 1 : 0);
                writeC(this._npc.isInCombat() ? 1 : 0);
                writeC(this._npc.isAlikeDead() ? 1 : 0);
                writeC(this._isSummoned ? 2 : 0);
                writeS((this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Agathion) ? "" : this._name);
                writeS((this._npc instanceof net.sf.l2j.gameserver.model.actor.instance.Agathion) ? "" : this._title);
                writeD(0);
                writeD(0);
                writeD(0);
                writeD(this._npc.getAbnormalEffect());
                writeD(this._clanId);
                writeD(this._clanCrest);
                writeD(this._allyId);
                writeD(this._allyCrest);
                writeC(this._npc.isInsideZone(ZoneId.WATER) ? 1 : (this._npc.isFlying() ? 2 : 0));
                writeC(this._npc.getTeam().getId());
                writeF(this._collisionRadius);
                writeF(this._collisionHeight);
                writeD(this._enchantEffect);
                writeD(this._npc.isFlying() ? 1 : 0);
            }
        }
    }

    public static class SummonInfo extends AbstractNpcInfo {
        private final Summon _summon;

        private final Player _owner;

        private int _summonAnimation = 0;

        public SummonInfo(Summon cha, Player attacker, int val) {
            super(cha);
            this._summon = cha;
            this._owner = this._summon.getOwner();
            this._summonAnimation = val;
            if (this._summon.isShowSummonAnimation())
                this._summonAnimation = 2;
            this._isAttackable = this._summon.isAutoAttackable(attacker);
            this._rhand = this._summon.getWeapon();
            this._lhand = 0;
            this._chest = this._summon.getArmor();
            this._enchantEffect = this._summon.getTemplate().getEnchantEffect();
            this._title = (this._owner == null || !this._owner.isOnline()) ? "" : this._owner.getName();
            this._idTemplate = this._summon.getTemplate().getIdTemplate();
            this._collisionHeight = this._summon.getCollisionHeight();
            this._collisionRadius = this._summon.getCollisionRadius();
            if (Config.SHOW_SUMMON_CREST && this._owner != null && this._owner.getClan() != null) {
                Clan clan = ClanTable.getInstance().getClan(this._owner.getClanId());
                this._clanCrest = clan.getCrestId();
                this._clanId = clan.getClanId();
                this._allyCrest = clan.getAllyCrestId();
                this._allyId = clan.getAllyId();
            }
        }

        protected void writeImpl() {
            if (this._owner != null && this._owner.getAppearance().getInvisible())
                return;
            writeC(22);
            writeD(this._summon.getObjectId());
            writeD(this._idTemplate + 1000000);
            writeD(this._isAttackable ? 1 : 0);
            writeD(this._x);
            writeD(this._y);
            writeD(this._z);
            writeD(this._heading);
            writeD(0);
            writeD(this._mAtkSpd);
            writeD(this._pAtkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeF(this._summon.getStat().getMovementSpeedMultiplier());
            writeF(this._summon.getStat().getAttackSpeedMultiplier());
            writeF(this._collisionRadius);
            writeF(this._collisionHeight);
            writeD(this._rhand);
            writeD(this._chest);
            writeD(this._lhand);
            writeC(1);
            writeC(this._summon.isRunning() ? 1 : 0);
            writeC(this._summon.isInCombat() ? 1 : 0);
            writeC(this._summon.isAlikeDead() ? 1 : 0);
            writeC(this._summonAnimation);
            writeS(this._name);
            writeS(this._title);
            writeD((this._summon instanceof net.sf.l2j.gameserver.model.actor.instance.Pet) ? 0 : 1);
            writeD(this._summon.getPvpFlag());
            writeD(this._summon.getKarma());
            writeD(this._summon.getAbnormalEffect());
            writeD(this._clanId);
            writeD(this._clanCrest);
            writeD(this._allyId);
            writeD(this._allyCrest);
            writeC(this._summon.isInsideZone(ZoneId.WATER) ? 1 : (this._summon.isFlying() ? 2 : 0));
            writeC(this._summon.getTeam().getId());
            writeF(this._collisionRadius);
            writeF(this._collisionHeight);
            writeD(this._enchantEffect);
            writeD(0);
        }
    }

    public static class PcMorphInfo extends AbstractNpcInfo {
        private final Player _pc;

        private final NpcTemplate _template;

        private final int _swimSpd;

        public PcMorphInfo(Player cha, NpcTemplate template) {
            super(cha);
            this._pc = cha;
            this._template = template;
            this._swimSpd = cha.getStat().getBaseSwimSpeed();
            this._rhand = this._template.getRightHand();
            this._lhand = this._template.getLeftHand();
            this._collisionHeight = this._template.getCollisionHeight();
            this._collisionRadius = this._template.getCollisionRadius();
            this._enchantEffect = this._template.getEnchantEffect();
        }

        protected void writeImpl() {
            writeC(22);
            writeD(this._pc.getObjectId());
            writeD(this._pc.getPolyId() + 1000000);
            writeD(1);
            writeD(this._x);
            writeD(this._y);
            writeD(this._z);
            writeD(this._heading);
            writeD(0);
            writeD(this._mAtkSpd);
            writeD(this._pAtkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeD(this._swimSpd);
            writeD(this._swimSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeD(this._runSpd);
            writeD(this._walkSpd);
            writeF(this._pc.getStat().getMovementSpeedMultiplier());
            writeF(this._pc.getStat().getAttackSpeedMultiplier());
            writeF(this._collisionRadius);
            writeF(this._collisionHeight);
            writeD(this._rhand);
            writeD(0);
            writeD(this._lhand);
            writeC(1);
            writeC(this._pc.isRunning() ? 1 : 0);
            writeC(this._pc.isInCombat() ? 1 : 0);
            writeC(this._pc.isAlikeDead() ? 1 : 0);
            writeC(0);
            writeS(this._name);
            writeS(this._title);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(this._pc.getAbnormalEffect());
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeC(this._pc.isInsideZone(ZoneId.WATER) ? 1 : (this._pc.isFlying() ? 2 : 0));
            writeC(0);
            writeF(this._collisionRadius);
            writeF(this._collisionHeight);
            writeD(this._enchantEffect);
            writeD(0);
        }
    }
}
