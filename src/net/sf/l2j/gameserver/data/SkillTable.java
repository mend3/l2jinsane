/**/
package net.sf.l2j.gameserver.data;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.DocumentSkill;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class SkillTable {
    private static final Logger _log = Logger.getLogger(SkillTable.class.getName());
    private static final Map<Integer, L2Skill> _skills = new HashMap<>();
    private static final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
    private static final L2Skill[] _heroSkills = new L2Skill[5];
    private static final int[] _heroSkillsId = new int[]{395, 396, 1374, 1375, 1376};
    private static final L2Skill[] _nobleSkills = new L2Skill[8];
    private static final int[] _nobleSkillsId = new int[]{325, 326, 327, 1323, 1324, 1325, 1326, 1327};

    protected SkillTable() {
    }

    public static SkillTable getInstance() {
        return SkillTable.SingletonHolder._instance;
    }

    public static int getSkillHashCode(L2Skill skill) {
        return getSkillHashCode(skill.getId(), skill.getLevel());
    }

    public static int getSkillHashCode(int skillId, int skillLevel) {
        return skillId * 256 + skillLevel;
    }

    public static L2Skill[] getHeroSkills() {
        return _heroSkills;
    }

    public static boolean isHeroSkill(int skillid) {
        int[] var1 = _heroSkillsId;
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            int id = var1[var3];
            if (id == skillid) {
                return true;
            }
        }

        return false;
    }

    public static L2Skill[] getNobleSkills() {
        return _nobleSkills;
    }

    public void load() {
        File dir = new File("./data/xml/skills");
        File[] var2 = dir.listFiles();
        int var3 = var2.length;

        int skillLvl;
        for (skillLvl = 0; skillLvl < var3; ++skillLvl) {
            File file = var2[skillLvl];
            DocumentSkill doc = new DocumentSkill(file);
            doc.parse();

            for (L2Skill skill : doc.getSkills()) {
                _skills.put(getSkillHashCode(skill), skill);
            }
        }

        _log.info("Loaded " + _skills.size() + " skills.");

        for (L2Skill skill : _skills.values()) {
            skillLvl = skill.getLevel();
            if (skillLvl < 99) {
                int skillId = skill.getId();
                int maxLvl = this.getMaxLevel(skillId);
                if (skillLvl > maxLvl) {
                    _skillMaxLevel.put(skillId, skillLvl);
                }
            }
        }

        SkillTable.FrequentSkill[] var10 = SkillTable.FrequentSkill.values();
        var3 = var10.length;

        for (skillLvl = 0; skillLvl < var3; ++skillLvl) {
            SkillTable.FrequentSkill sk = var10[skillLvl];
            sk._skill = this.getInfo(sk._id, sk._level);
        }

        int i;
        for (i = 0; i < _heroSkillsId.length; ++i) {
            _heroSkills[i] = this.getInfo(_heroSkillsId[i], 1);
        }

        for (i = 0; i < _nobleSkills.length; ++i) {
            _nobleSkills[i] = this.getInfo(_nobleSkillsId[i], 1);
        }

    }

    public void reload() {
        _skills.clear();
        _skillMaxLevel.clear();
        this.load();
    }

    public L2Skill getInfo(int skillId, int level) {
        return _skills.get(getSkillHashCode(skillId, level));
    }

    public int getMaxLevel(int skillId) {
        Integer maxLevel = _skillMaxLevel.get(skillId);
        return maxLevel != null ? maxLevel : 0;
    }

    public L2Skill[] getSiegeSkills(boolean addNoble) {
        L2Skill[] temp = new L2Skill[2 + (addNoble ? 1 : 0)];
        int i = 0;
        int var4 = i + 1;
        temp[i] = _skills.get(getSkillHashCode(246, 1));
        temp[var4++] = _skills.get(getSkillHashCode(247, 1));
        if (addNoble) {
            temp[var4++] = _skills.get(getSkillHashCode(326, 1));
        }

        return temp;
    }

    public enum FrequentSkill {
        LUCKY(194, 1),
        BLESSING_OF_PROTECTION(5182, 1),
        SEAL_OF_RULER(246, 1),
        BUILD_HEADQUARTERS(247, 1),
        STRIDER_SIEGE_ASSAULT(325, 1),
        DWARVEN_CRAFT(1321, 1),
        COMMON_CRAFT(1322, 1),
        FIREWORK(5965, 1),
        LARGE_FIREWORK(2025, 1),
        SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
        ANTHARAS_JUMP(4106, 1),
        ANTHARAS_TAIL(4107, 1),
        ANTHARAS_FEAR(4108, 1),
        ANTHARAS_DEBUFF(4109, 1),
        ANTHARAS_MOUTH(4110, 1),
        ANTHARAS_BREATH(4111, 1),
        ANTHARAS_NORMAL_ATTACK(4112, 1),
        ANTHARAS_NORMAL_ATTACK_EX(4113, 1),
        ANTHARAS_SHORT_FEAR(5092, 1),
        ANTHARAS_METEOR(5093, 1),
        QUEEN_ANT_BRANDISH(4017, 1),
        QUEEN_ANT_STRIKE(4018, 1),
        QUEEN_ANT_SPRINKLE(4019, 1),
        NURSE_HEAL_1(4020, 1),
        NURSE_HEAL_2(4024, 1),
        ZAKEN_TELE(4216, 1),
        ZAKEN_MASS_TELE(4217, 1),
        ZAKEN_DRAIN(4218, 1),
        ZAKEN_HOLD(4219, 1),
        ZAKEN_DUAL_ATTACK(4220, 1),
        ZAKEN_MASS_DUAL_ATTACK(4221, 1),
        ZAKEN_SELF_TELE(4222, 1),
        ZAKEN_NIGHT_TO_DAY(4223, 1),
        ZAKEN_DAY_TO_NIGHT(4224, 1),
        ZAKEN_REGEN_NIGHT(4227, 1),
        ZAKEN_REGEN_DAY(4242, 1),
        RAID_CURSE(4215, 1),
        RAID_CURSE2(4515, 1),
        RAID_ANTI_STRIDER_SLOW(4258, 1),
        WYVERN_BREATH(4289, 1),
        ARENA_CP_RECOVERY(4380, 1),
        VARKA_KETRA_PETRIFICATION(4578, 1),
        FAKE_PETRIFICATION(4616, 1),
        THE_VICTOR_OF_WAR(5074, 1),
        THE_VANQUISHED_OF_WAR(5075, 1);

        private final int _id;
        private final int _level;
        private L2Skill _skill = null;

        FrequentSkill(int id, int level) {
            this._id = id;
            this._level = level;
        }

        public L2Skill getSkill() {
            return this._skill;
        }

    }

    private static class SingletonHolder {
        protected static final SkillTable _instance = new SkillTable();
    }
}