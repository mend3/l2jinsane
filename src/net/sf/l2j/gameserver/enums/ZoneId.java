/**/
package net.sf.l2j.gameserver.enums;

public enum ZoneId {
    PVP(0),
    PEACE(1),
    SIEGE(2),
    MOTHER_TREE(3),
    CLAN_HALL(4),
    NO_LANDING(5),
    WATER(6),
    JAIL(7),
    MONSTER_TRACK(8),
    CASTLE(9),
    SWAMP(10),
    NO_SUMMON_FRIEND(11),
    NO_STORE(12),
    TOWN(13),
    HQ(14),
    DANGER_AREA(15),
    CAST_ON_ARTIFACT(16),
    NO_RESTART(17),
    SCRIPT(18),
    BOSS(19),
    MULTI_FUNCTION(20),
    ARENA_EVENT(21),
    TORURNAMENT_ARENA(22),
    RANDOMZONE(23),
    PVPEVENT(24),
    AUTOFARMZONE(25),
    PARTYFARMZONE(26);

    public static final ZoneId[] VALUES = values();
    private final int _id;

    ZoneId(int id) {
        this._id = id;
    }

    // $FF: synthetic method
    private static ZoneId[] $values() {
        return new ZoneId[]{PVP, PEACE, SIEGE, MOTHER_TREE, CLAN_HALL, NO_LANDING, WATER, JAIL, MONSTER_TRACK, CASTLE, SWAMP, NO_SUMMON_FRIEND, NO_STORE, TOWN, HQ, DANGER_AREA, CAST_ON_ARTIFACT, NO_RESTART, SCRIPT, BOSS, MULTI_FUNCTION, ARENA_EVENT, TORURNAMENT_ARENA, RANDOMZONE, PVPEVENT, AUTOFARMZONE, PARTYFARMZONE};
    }

    public int getId() {
        return this._id;
    }
}
