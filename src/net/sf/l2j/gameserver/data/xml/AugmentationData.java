/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.*;

public class AugmentationData implements IXmlReader {
    private static final int STAT_START = 1;
    private static final int STAT_END = 14560;
    private static final int STAT_BLOCKSIZE = 3640;
    private static final int STAT_SUBBLOCKSIZE = 91;
    private static final int STAT_NUM = 13;
    private static final byte[] STATS1_MAP = new byte[91];
    private static final byte[] STATS2_MAP = new byte[91];
    private static final int BLUE_START = 14561;
    private static final int SKILLS_BLOCKSIZE = 178;
    private static final int BASESTAT_STR = 16341;
    private static final int BASESTAT_CON = 16342;
    private static final int BASESTAT_INT = 16343;
    private static final int BASESTAT_MEN = 16344;
    private final List<List<AugmentationData.AugmentationStat>> _augStats = new ArrayList<>(4);
    private final List<List<Integer>> _blueSkills = new ArrayList<>(10);
    private final List<List<Integer>> _purpleSkills = new ArrayList<>(10);
    private final List<List<Integer>> _redSkills = new ArrayList<>(10);
    private final Map<Integer, IntIntHolder> _allSkills = new HashMap<>();

    protected AugmentationData() {
    }

    public static AugmentationData getInstance() {
        return AugmentationData.SingletonHolder.INSTANCE;
    }

    public void load() {
        byte idx;
        for (idx = 0; idx < 13; STATS2_MAP[idx] = idx++) {
            STATS1_MAP[idx] = idx;
        }

        int i;
        for (i = 0; i < 13; ++i) {
            for (int j = i + 1; j < 13; ++j) {
                STATS1_MAP[idx] = (byte) i;
                STATS2_MAP[idx] = (byte) j;
                ++idx;
            }
        }

        for (i = 0; i < 4; ++i) {
            this._augStats.add(new ArrayList<>());
        }

        for (i = 0; i < 10; ++i) {
            this._blueSkills.add(new ArrayList<>());
            this._purpleSkills.add(new ArrayList<>());
            this._redSkills.add(new ArrayList<>());
        }
        this.parseFile("./data/xml/augmentation");
        LOGGER.info("Loaded {} sets of augmentation stats.", this._augStats.size());
        int blue = this._blueSkills.stream().mapToInt(List::size).sum();
        int purple = this._purpleSkills.stream().mapToInt(List::size).sum();
        int red = this._redSkills.stream().mapToInt(List::size).sum();
        LOGGER.info("Loaded {} blue, {} purple and {} red Life-Stone skills.", blue, purple, red);
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "augmentation", (augmentationNode) -> {
                StatSet set = this.parseAttributes(augmentationNode);
                int augmentationId = set.getInteger("id");
                int k = (augmentationId - 14561) / 178;
                String var5 = set.getString("type");
                byte var6 = -1;
                switch (var5.hashCode()) {
                    case -976943172:
                        if (var5.equals("purple")) {
                            var6 = 1;
                        }
                        break;
                    case 112785:
                        if (var5.equals("red")) {
                            var6 = 2;
                        }
                        break;
                    case 3027034:
                        if (var5.equals("blue")) {
                            var6 = 0;
                        }
                }

                switch (var6) {
                    case 0:
                        this._blueSkills.get(k).add(augmentationId);
                        break;
                    case 1:
                        this._purpleSkills.get(k).add(augmentationId);
                        break;
                    case 2:
                        this._redSkills.get(k).add(augmentationId);
                }

                this._allSkills.put(augmentationId, new IntIntHolder(set.getInteger("skillId"), set.getInteger("skillLevel")));
            });
            this.forEach(listNode, "set", (setNode) -> {
                int order = this.parseInteger(setNode.getAttributes(), "order");
                List<AugmentationData.AugmentationStat> statList = this._augStats.get(order);
                this.forEach(setNode, "stat", (statNode) -> {
                    String statName = this.parseString(statNode.getAttributes(), "name");
                    List<Float> soloValues = new ArrayList<>();
                    List<Float> combinedValues = new ArrayList<>();
                    this.forEach(statNode, "table", (tableNode) -> {
                        String tableName = this.parseString(tableNode.getAttributes(), "name");
                        StringTokenizer data = new StringTokenizer(tableNode.getFirstChild().getNodeValue());
                        if ("#soloValues".equalsIgnoreCase(tableName)) {
                            while (data.hasMoreTokens()) {
                                soloValues.add(Float.parseFloat(data.nextToken()));
                            }
                        } else {
                            while (data.hasMoreTokens()) {
                                combinedValues.add(Float.parseFloat(data.nextToken()));
                            }
                        }

                    });
                    float[] soloValuesArr = new float[soloValues.size()];

                    for (int i = 0; i < soloValuesArr.length; ++i) {
                        soloValuesArr[i] = soloValues.get(i);
                    }

                    float[] combinedValuesArr = new float[combinedValues.size()];

                    for (int ix = 0; ix < combinedValuesArr.length; ++ix) {
                        combinedValuesArr[ix] = combinedValues.get(ix);
                    }

                    statList.add(new AugmentationData.AugmentationStat(Stats.valueOfXml(statName), soloValuesArr, combinedValuesArr));
                });
            });
        });
    }

    public L2Augmentation generateRandomAugmentation(int lifeStoneLevel, int lifeStoneGrade) {
        int stat12 = 0;
        int stat34 = 0;
        boolean generateSkill = false;
        boolean generateGlow = false;
        lifeStoneLevel = Math.min(lifeStoneLevel, 9);
        switch (lifeStoneGrade) {
            case 0:
                if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_SKILL_CHANCE) {
                    generateSkill = true;
                }

                if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_GLOW_CHANCE) {
                    generateGlow = true;
                }
                break;
            case 1:
                if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_SKILL_CHANCE) {
                    generateSkill = true;
                }

                if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_GLOW_CHANCE) {
                    generateGlow = true;
                }
                break;
            case 2:
                if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_SKILL_CHANCE) {
                    generateSkill = true;
                }

                if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE) {
                    generateGlow = true;
                }
                break;
            case 3:
                if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_SKILL_CHANCE) {
                    generateSkill = true;
                }

                if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE) {
                    generateGlow = true;
                }
        }

        if (!generateSkill && Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE) {
            stat34 = Rnd.get(16341, 16344);
        }

        int resultColor = Rnd.get(0, 100);
        if (stat34 == 0 && !generateSkill) {
            if (resultColor <= 15 * lifeStoneGrade + 40) {
                resultColor = 1;
            } else {
                resultColor = 0;
            }
        } else if (resultColor > 10 * lifeStoneGrade + 5 && stat34 == 0) {
            if (resultColor <= 10 * lifeStoneGrade + 10) {
                resultColor = 1;
            } else {
                resultColor = 2;
            }
        } else {
            resultColor = 3;
        }

        L2Skill skill = null;
        if (generateSkill) {
            switch (resultColor) {
                case 1:
                    stat34 = (Integer) ((List) this._blueSkills.get(lifeStoneLevel)).get(Rnd.get(0, this._blueSkills.get(lifeStoneLevel).size() - 1));
                    break;
                case 2:
                    stat34 = (Integer) ((List) this._purpleSkills.get(lifeStoneLevel)).get(Rnd.get(0, this._purpleSkills.get(lifeStoneLevel).size() - 1));
                    break;
                case 3:
                    stat34 = (Integer) ((List) this._redSkills.get(lifeStoneLevel)).get(Rnd.get(0, this._redSkills.get(lifeStoneLevel).size() - 1));
            }

            skill = this._allSkills.get(stat34).getSkill();
        }

        int offset;
        if (stat34 == 0) {
            int temp = Rnd.get(2, 3);
            int colorOffset = resultColor * 910 + temp * 3640 + 1;
            offset = lifeStoneLevel * 91 + colorOffset;
            stat34 = Rnd.get(offset, offset + 91 - 1);
            if (generateGlow && lifeStoneGrade >= 2) {
                offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + lifeStoneGrade * 910 + 1;
            } else {
                offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + Rnd.get(0, 1) * 910 + 1;
            }
        } else if (!generateGlow) {
            offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + 1;
        } else {
            offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + (lifeStoneGrade + resultColor) / 2 * 910 + 1;
        }

        stat12 = Rnd.get(offset, offset + 91 - 1);
        return new L2Augmentation((stat34 << 16) + stat12, skill);
    }

    public List<AugmentationData.AugStat> getAugStatsById(int augmentationId) {
        List<AugmentationData.AugStat> temp = new ArrayList<>();
        int[] stats = new int[]{'\uffff' & augmentationId, augmentationId >> 16};

        for (int i = 0; i < 2; ++i) {
            if (stats[i] >= 1 && stats[i] <= 14560) {
                int base = stats[i] - 1;
                int color = base / 3640;
                int subblock = base % 3640;
                int level = subblock / 91;
                int stat = subblock % 91;
                byte stat1 = STATS1_MAP[stat];
                byte stat2 = STATS2_MAP[stat];
                AugmentationData.AugmentationStat as;
                if (stat1 == stat2) {
                    as = (AugmentationData.AugmentationStat) ((List) this._augStats.get(color)).get(stat1);
                    temp.add(new AugmentationData.AugStat(as.getStat(), as.getSingleStatValue(level)));
                } else {
                    as = (AugmentationData.AugmentationStat) ((List) this._augStats.get(color)).get(stat1);
                    temp.add(new AugmentationData.AugStat(as.getStat(), as.getCombinedStatValue(level)));
                    as = (AugmentationData.AugmentationStat) ((List) this._augStats.get(color)).get(stat2);
                    temp.add(new AugmentationData.AugStat(as.getStat(), as.getCombinedStatValue(level)));
                }
            } else if (stats[i] >= 16341 && stats[i] <= 16344) {
                switch (stats[i]) {
                    case 16341:
                        temp.add(new AugmentationData.AugStat(Stats.STAT_STR, 1.0F));
                        break;
                    case 16342:
                        temp.add(new AugmentationData.AugStat(Stats.STAT_CON, 1.0F));
                        break;
                    case 16343:
                        temp.add(new AugmentationData.AugStat(Stats.STAT_INT, 1.0F));
                        break;
                    case 16344:
                        temp.add(new AugmentationData.AugStat(Stats.STAT_MEN, 1.0F));
                }
            }
        }

        return temp;
    }

    public L2Augmentation generateAugmentationWithSkill(int id, int level) {
        int stat12 = 0;
        int stat34 = 0;
        int lifeStoneLevel = 9;
        int lifeStoneGrade = 3;
        int resultColor = 3;
        L2Skill skill = null;

        for (int i : this._allSkills.keySet()) {
            L2Skill sk = this._allSkills.get(i).getSkill();
            if (sk.getId() == id) {
                if (sk.getLevel() == level) {
                    skill = sk;
                }

                stat34 = i;
                break;
            }
        }

        if (skill == null) {
            skill = SkillTable.getInstance().getInfo(id, level);
        }

        int i;
        for (i = 0; i < 10; ++i) {
            if (this._blueSkills.get(i).contains(stat34)) {
                resultColor = 1;
                lifeStoneLevel = i;
                break;
            }

            if (this._redSkills.get(i).contains(stat34)) {
                resultColor = 2;
                lifeStoneLevel = i;
                break;
            }

            if (this._purpleSkills.get(i).contains(stat34)) {
                resultColor = 3;
                lifeStoneLevel = i;
                break;
            }
        }

        i = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + (lifeStoneGrade + resultColor) / 2 * 910 + 1;
        stat12 = Rnd.get(i, i + 91 - 1);
        return new L2Augmentation((stat34 << 16) + stat12, skill);
    }

    public static class AugmentationStat {
        private final Stats _stat;
        private final int _singleSize;
        private final int _combinedSize;
        private final float[] _singleValues;
        private final float[] _combinedValues;

        public AugmentationStat(Stats stat, float[] sValues, float[] cValues) {
            this._stat = stat;
            this._singleSize = sValues.length;
            this._singleValues = sValues;
            this._combinedSize = cValues.length;
            this._combinedValues = cValues;
        }

        public int getSingleStatSize() {
            return this._singleSize;
        }

        public int getCombinedStatSize() {
            return this._combinedSize;
        }

        public float getSingleStatValue(int i) {
            return i < this._singleSize && i >= 0 ? this._singleValues[i] : this._singleValues[this._singleSize - 1];
        }

        public float getCombinedStatValue(int i) {
            return i < this._combinedSize && i >= 0 ? this._combinedValues[i] : this._combinedValues[this._combinedSize - 1];
        }

        public Stats getStat() {
            return this._stat;
        }
    }

    public static class AugStat {
        private final Stats _stat;
        private final float _value;

        public AugStat(Stats stat, float value) {
            this._stat = stat;
            this._value = value;
        }

        public Stats getStat() {
            return this._stat;
        }

        public float getValue() {
            return this._value;
        }
    }

    private static class SingletonHolder {
        protected static final AugmentationData INSTANCE = new AugmentationData();
    }
}