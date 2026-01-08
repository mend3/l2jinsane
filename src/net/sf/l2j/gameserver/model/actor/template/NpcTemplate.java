/**/
package net.sf.l2j.gameserver.model.actor.template;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.MinionData;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.*;

public class NpcTemplate extends CreatureTemplate {
    private final int _npcId;
    private final int _idTemplate;
    private final String _type;
    private final String _name;
    private final String _title;
    private final boolean _cantBeChampionMonster;
    private final byte _level;
    private final int _exp;
    private final int _sp;
    private final int _rHand;
    private final int _lHand;
    private final int _enchantEffect;
    private final int _ssCount;
    private final int _ssRate;
    private final int _spsCount;
    private final int _spsRate;
    private final int _aggroRange;
    private final boolean _canMove;
    private final boolean _isSeedable;
    private final Map<SkillType, List<L2Skill>> _skills;
    private final Map<ScriptEventType, List<Quest>> _questEvents;
    private final boolean _usingServerSideName;
    private final boolean _usingServerSideTitle;
    private final int _dropHerbGroup;
    private final AIType _aiType;
    private final List<DropCategory> _categories;
    private final List<MinionData> _minions;
    private int _corpseTime;
    private Race _race;
    private String[] _clans;
    private int _clanRange;
    private int[] _ignoredIds;
    private List<ClassId> _teachInfo;
    private Castle _castle;

    public NpcTemplate(StatSet set) {
        super(set);
        this._race = NpcTemplate.Race.UNKNOWN;
        this._skills = new HashMap<>();
        this._questEvents = new HashMap<>();
        this._npcId = set.getInteger("id");
        this._idTemplate = set.getInteger("idTemplate", this._npcId);
        this._type = set.getString("type");
        this._name = set.getString("name");
        this._usingServerSideName = set.getBool("usingServerSideName", false);
        this._title = set.getString("title", "");
        this._usingServerSideTitle = set.getBool("usingServerSideTitle", false);
        this._cantBeChampionMonster = this._title.equalsIgnoreCase("Quest Monster") || this.isType("Chest");
        this._level = set.getByte("level", (byte) 1);
        this._exp = set.getInteger("exp", 0);
        this._sp = set.getInteger("sp", 0);
        this._rHand = set.getInteger("rHand", 0);
        this._lHand = set.getInteger("lHand", 0);
        this._enchantEffect = set.getInteger("enchant", 0);
        this._corpseTime = set.getInteger("corpseTime", 7);
        this._dropHerbGroup = set.getInteger("dropHerbGroup", 0);
        if (set.containsKey("raceId")) {
            this.setRace(set.getInteger("raceId"));
        }

        this._aiType = set.getEnum("aiType", AIType.class, AIType.DEFAULT);
        this._ssCount = set.getInteger("ssCount", 0);
        this._ssRate = set.getInteger("ssRate", 0);
        this._spsCount = set.getInteger("spsCount", 0);
        this._spsRate = set.getInteger("spsRate", 0);
        this._aggroRange = set.getInteger("aggro", 0);
        if (set.containsKey("clan")) {
            this._clans = set.getStringArray("clan");
            this._clanRange = set.getInteger("clanRange");
            if (set.containsKey("ignoredIds")) {
                this._ignoredIds = set.getIntegerArray("ignoredIds");
            }
        }

        this._canMove = set.getBool("canMove", true);
        this._isSeedable = set.getBool("seedable", false);
        this._categories = set.getList("drops");
        this._minions = set.getList("minions");
        if (set.containsKey("teachTo")) {
            int[] classIds = set.getIntegerArray("teachTo");
            this._teachInfo = new ArrayList<>(classIds.length);

            for (int classId : classIds) {
                this._teachInfo.add(ClassId.VALUES[classId]);
            }
        }

        this.addSkills(set.getList("skills"));

        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getRelatedNpcIds().contains(this._npcId)) {
                this._castle = castle;
                break;
            }
        }

    }

    public int getNpcId() {
        return this._npcId;
    }

    public int getIdTemplate() {
        return this._idTemplate;
    }

    public String getType() {
        return this._type;
    }

    public boolean isType(String type) {
        return this._type.equalsIgnoreCase(type);
    }

    public String getName() {
        return this._name;
    }

    public boolean isUsingServerSideName() {
        return this._usingServerSideName;
    }

    public String getTitle() {
        return this._title;
    }

    public boolean isUsingServerSideTitle() {
        return this._usingServerSideTitle;
    }

    public boolean cantBeChampion() {
        return this._cantBeChampionMonster;
    }

    public byte getLevel() {
        return this._level;
    }

    public int getRewardExp() {
        return this._exp;
    }

    public int getRewardSp() {
        return this._sp;
    }

    public int getRightHand() {
        return this._rHand;
    }

    public int getLeftHand() {
        return this._lHand;
    }

    public int getEnchantEffect() {
        return this._enchantEffect;
    }

    public int getCorpseTime() {
        return this._corpseTime;
    }

    public void setCorpseTime(int val) {
        this._corpseTime = val;
    }

    public int getDropHerbGroup() {
        return this._dropHerbGroup;
    }

    public Race getRace() {
        return this._race;
    }

    public void setRace(int raceId) {
        if (raceId >= 1 && raceId <= 23) {
            this._race = NpcTemplate.Race.VALUES[raceId];
        }
    }

    public AIType getAiType() {
        return this._aiType;
    }

    public int getSsCount() {
        return this._ssCount;
    }

    public int getSsRate() {
        return this._ssRate;
    }

    public int getSpsCount() {
        return this._spsCount;
    }

    public int getSpsRate() {
        return this._spsRate;
    }

    public int getAggroRange() {
        return this._aggroRange;
    }

    public String[] getClans() {
        return this._clans;
    }

    public int getClanRange() {
        return this._clanRange;
    }

    public int[] getIgnoredIds() {
        return this._ignoredIds;
    }

    public boolean canMove() {
        return this._canMove;
    }

    public boolean isSeedable() {
        return this._isSeedable;
    }

    public Castle getCastle() {
        return this._castle;
    }

    public List<DropCategory> getDropData() {
        return this._categories;
    }

    public List<DropData> getAllDropData() {
        List<DropData> list = new ArrayList<>();

        for (DropCategory tmp : this._categories) {
            list.addAll(tmp.getAllDrops());
        }

        return list;
    }

    public void addDropData(DropData drop, int categoryType) {
        boolean isBossType = this.isType("RaidBoss") || this.isType("GrandBoss");
        synchronized (this._categories) {
            for (DropCategory cat : this._categories) {
                if (cat.getCategoryType() == categoryType) {
                    cat.addDropData(drop, isBossType);
                    return;
                }
            }

            DropCategory cat = new DropCategory(categoryType);
            cat.addDropData(drop, isBossType);
            this._categories.add(cat);
        }
    }

    public List<MinionData> getMinionData() {
        return this._minions;
    }

    public boolean canTeach(ClassId classId) {
        return this._teachInfo != null && this._teachInfo.contains(classId.level() == 3 ? classId.getParent() : classId);
    }

    public Map<SkillType, List<L2Skill>> getSkills() {
        return this._skills;
    }

    public List<L2Skill> getSkills(SkillType type) {
        return this._skills.getOrDefault(type, Collections.emptyList());
    }

    public void addSkills(List<L2Skill> skills) {
        for (L2Skill skill : skills) {
            if (skill.isPassive()) {
                this.addSkill(NpcTemplate.SkillType.PASSIVE, skill);
            } else if (skill.isSuicideAttack()) {
                this.addSkill(NpcTemplate.SkillType.SUICIDE, skill);
            } else {
                switch (skill.getSkillType()) {
                    case GET_PLAYER:
                    case INSTANT_JUMP:
                        this.addSkill(NpcTemplate.SkillType.TELEPORT, skill);
                        break;
                    case BUFF:
                    case CONT:
                    case REFLECT:
                        this.addSkill(NpcTemplate.SkillType.BUFF, skill);
                        break;
                    case HEAL:
                    case HOT:
                    case HEAL_PERCENT:
                    case HEAL_STATIC:
                    case BALANCE_LIFE:
                    case MANARECHARGE:
                    case MANAHEAL_PERCENT:
                        this.addSkill(NpcTemplate.SkillType.HEAL, skill);
                        break;
                    case DEBUFF:
                    case ROOT:
                    case SLEEP:
                    case STUN:
                    case PARALYZE:
                    case POISON:
                    case DOT:
                    case MDOT:
                    case BLEED:
                    case MUTE:
                    case FEAR:
                    case CANCEL:
                    case NEGATE:
                    case WEAKNESS:
                    case AGGDEBUFF:
                        this.addSkill(NpcTemplate.SkillType.DEBUFF, skill);
                        break;
                    case PDAM:
                    case MDAM:
                    case BLOW:
                    case DRAIN:
                    case CHARGEDAM:
                    case FATAL:
                    case DEATHLINK:
                    case MANADAM:
                    case CPDAMPERCENT:
                    case AGGDAMAGE:
                        this.addSkill(skill.getCastRange() > 150 ? NpcTemplate.SkillType.LONG_RANGE : NpcTemplate.SkillType.SHORT_RANGE, skill);
                }
            }
        }

    }

    private void addSkill(SkillType type, L2Skill skill) {
        List<L2Skill> list = this._skills.get(type);
        if (list == null) {
            list = new ArrayList<>(5);
            list.add(skill);
            this._skills.put(type, list);
        } else {
            list.add(skill);
        }

    }

    public Map<ScriptEventType, List<Quest>> getEventQuests() {
        return this._questEvents;
    }

    public List<Quest> getEventQuests(ScriptEventType type) {
        return this._questEvents.get(type);
    }

    public void addQuestEvent(ScriptEventType type, Quest quest) {
        List<Quest> list = this._questEvents.get(type);
        if (list == null) {
            list = new ArrayList<>(5);
            list.add(quest);
            this._questEvents.put(type, list);
        } else {
            list.remove(quest);
            if (type.isMultipleRegistrationAllowed() || list.isEmpty()) {
                list.add(quest);
            }
        }

    }

    public enum SkillType {
        BUFF,
        DEBUFF,
        HEAL,
        PASSIVE,
        LONG_RANGE,
        SHORT_RANGE,
        SUICIDE,
        TELEPORT
    }

    public enum AIType {
        DEFAULT,
        ARCHER,
        MAGE,
        HEALER,
        CORPSE
    }

    public enum Race {
        UNKNOWN,
        UNDEAD,
        MAGICCREATURE,
        BEAST,
        ANIMAL,
        PLANT,
        HUMANOID,
        SPIRIT,
        ANGEL,
        DEMON,
        DRAGON,
        GIANT,
        BUG,
        FAIRIE,
        HUMAN,
        ELVE,
        DARKELVE,
        ORC,
        DWARVE,
        OTHER,
        NONLIVING,
        SIEGEWEAPON,
        DEFENDINGARMY,
        MERCENARIE;

        public static final Race[] VALUES = values();
    }
}
