package net.sf.l2j.gameserver.model.actor.template;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.model.holder.ItemTemplateHolder;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.List;

public class PlayerTemplate extends CreatureTemplate {
    private final ClassId _classId;

    private final int _fallingHeight;

    private final int _baseSwimSpd;

    private final double _collisionRadiusFemale;

    private final double _collisionHeightFemale;

    private final List<Location> _spawnLocations;

    private final int _classBaseLevel;

    private final double[] _hpTable;

    private final double[] _mpTable;

    private final double[] _cpTable;

    private final List<ItemTemplateHolder> _items;

    private final List<GeneralSkillNode> _skills;

    private final Weapon _fists;

    public PlayerTemplate(StatSet set) {
        super(set);
        this._classId = ClassId.VALUES[set.getInteger("id")];
        this._fallingHeight = set.getInteger("falling_height", 333);
        this._baseSwimSpd = set.getInteger("swimSpd", 1);
        this._collisionRadiusFemale = set.getDouble("radiusFemale");
        this._collisionHeightFemale = set.getDouble("heightFemale");
        this._spawnLocations = set.getList("spawnLocations");
        this._classBaseLevel = set.getInteger("baseLvl");
        this._hpTable = set.getDoubleArray("hpTable");
        this._mpTable = set.getDoubleArray("mpTable");
        this._cpTable = set.getDoubleArray("cpTable");
        this._items = set.getList("items");
        this._skills = set.getList("skills");
        this._fists = (Weapon) ItemTable.getInstance().getTemplate(set.getInteger("fists"));
    }

    @Override
    public String toString() {
        return _classId + " with " + _items.size() + " items";
    }

    public final ClassId getClassId() {
        return this._classId;
    }

    public final ClassRace getRace() {
        return this._classId.getRace();
    }

    public final String getClassName() {
        return this._classId.toString();
    }

    public final int getFallHeight() {
        return this._fallingHeight;
    }

    public final int getBaseSwimSpeed() {
        return this._baseSwimSpd;
    }

    public double getCollisionRadiusBySex(Sex sex) {
        return (sex == Sex.MALE) ? this._collisionRadius : this._collisionRadiusFemale;
    }

    public double getCollisionHeightBySex(Sex sex) {
        return (sex == Sex.MALE) ? this._collisionHeight : this._collisionHeightFemale;
    }

    public final Location getRandomSpawn() {
        Location loc = Rnd.get(this._spawnLocations);
        return (loc == null) ? Location.DUMMY_LOC : loc;
    }

    public final int getClassBaseLevel() {
        return this._classBaseLevel;
    }

    public final double getBaseHpMax(int level) {
        return this._hpTable[level - 1];
    }

    public final double getBaseMpMax(int level) {
        return this._mpTable[level - 1];
    }

    public final double getBaseCpMax(int level) {
        return this._cpTable[level - 1];
    }

    public final List<ItemTemplateHolder> getItems() {
        return this._items;
    }

    public final List<GeneralSkillNode> getSkills() {
        return this._skills;
    }

    public GeneralSkillNode findSkill(int id, int level) {
        return this._skills.stream().filter(s -> (s.getId() == id && s.getValue() == level)).findFirst().orElse(null);
    }

    public final Weapon getFists() {
        return this._fists;
    }
}
