package net.sf.l2j.gameserver.model.location;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.PolyType;
import net.sf.l2j.gameserver.enums.TowerType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.FlameTower;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

public class TowerSpawnLocation extends SpawnLocation {

    public static final int LT_DISPLAY_NPC_WORKING = 13002;
    public static final int LT_DISPLAY_NPC_NON_WORKING = 13003;
    public static final int FT_DISPLAY_NPC_WORKING = 13004;
    public static final int FT_DISPLAY_NPC_NON_WORKING = 13005;

    private final TowerType _type;
    private final String _alias;
    private final Castle _castle;

    private double _hp;
    private double _pDef;
    private double _mDef;

    private String[] _zones;
    private int _upgradeLevel;

    private Npc _npc;

    public TowerSpawnLocation(TowerType type, String alias, Castle castle) {
        super(SpawnLocation.DUMMY_SPAWNLOC);

        _type = type;
        _alias = alias;
        _castle = castle;
    }

    public TowerType getType() {
        return _type;
    }

    public String getAlias() {
        return _alias;
    }

    public double getHp() {
        return _hp;
    }

    public double getPdef() {
        return _pDef;
    }

    public double getMdef() {
        return _mDef;
    }

    public void setStats(double hp, double pDef, double mDef) {
        _hp = hp;
        _pDef = pDef;
        _mDef = mDef;
    }

    public String[] getZones() {
        return _zones;
    }

    public void setZones(String[] zones) {
        _zones = zones;
    }

    public int getUpgradeLevel() {
        return _upgradeLevel;
    }

    public void setUpgradeLevel(int level) {
        _upgradeLevel = level;
    }

    public Npc getNpc() {
        return _npc;
    }

    public void spawnMe() {
        try {
            final StatSet npcDat = new StatSet();

            npcDat.set("id", (_type == TowerType.LIFE_CONTROL) ? LT_DISPLAY_NPC_NON_WORKING : FT_DISPLAY_NPC_NON_WORKING);
            npcDat.set("type", (_type == TowerType.LIFE_CONTROL) ? "LifeTower" : "FlameTower");

            npcDat.set("name", (_type == TowerType.LIFE_CONTROL) ? "Life Control Tower" : "Flame Control Tower");

            npcDat.set("hp", _hp);
            npcDat.set("mp", 0);

            npcDat.set("pAtk", 0);
            npcDat.set("mAtk", 0);
            npcDat.set("pDef", _pDef);
            npcDat.set("mDef", _mDef);

            npcDat.set("runSpd", 0); // Have to keep this, static object MUST BE 0 (critical error otherwise).

            npcDat.set("radius", 7);
            npcDat.set("height", 35);

            npcDat.set("undying", true);
            npcDat.set("baseDamageRange", "0;0;80;120");

            final L2Spawn spawn = new L2Spawn(new NpcTemplate(npcDat));
            spawn.setLoc(this);

            _npc = spawn.doSpawn(false);
            _npc.setResidence(_castle);
        } catch (Exception e) {
            // Do nothing.
        }
    }

    /**
     * Morph the {@link Npc} of this {@link TowerSpawnLocation}.
     */
    public void polymorph() {
        if (_npc == null)
            return;

        _npc.polymorph(PolyType.NPC, (_type == TowerType.LIFE_CONTROL) ? LT_DISPLAY_NPC_WORKING : FT_DISPLAY_NPC_WORKING);
    }

    /**
     * Reinitialize the {@link Npc} of this {@link TowerSpawnLocation}.
     */
    public void unpolymorph() {
        if (_npc == null)
            return;

        // Reset HPs to maximum.
        _npc.getStatus().setCurrentHp(_npc.getStat().getMaxHp());

        // Unpolymorph the NPC.
        _npc.unpolymorph();
    }

    /**
     * Apply Mid Victory effects ; Flame Tower is disabled, Life Tower is enabled.
     */
    public void midVictory() {
        if (_npc == null)
            return;

        // The NPC is a polymorphed Flame Tower (active), we unpolymorph it and disable the trap.
        if (_type == TowerType.TRAP_CONTROL && _npc.getPolyType() != PolyType.NPC) {
            // Unpolymorph the NPC.
            _npc.unpolymorph();

            // Disable the related zone.
            ((FlameTower) _npc).enableZones(false);
        }
        // The NPC is an unpolymorphed Life Tower (dead), we polymorph it.
        else if (_type == TowerType.LIFE_CONTROL && _npc.getPolyType() == PolyType.NPC) {
            // Reset HPs to maximum.
            _npc.getStatus().setCurrentHp(_npc.getStat().getMaxHp());

            // Polymorph, making it active.
            polymorph();
        }
    }
}
