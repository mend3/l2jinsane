/**/
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DocumentSkill extends DocumentBase {
    static final Logger LOGGER = Logger.getLogger(DocumentSkill.class.getName());
    private final List<L2Skill> _skillsInFile = new ArrayList<>();
    private Skill _currentSkill;

    public DocumentSkill(File file) {
        super(file);
    }

    private void setCurrentSkill(Skill skill) {
        this._currentSkill = skill;
    }

    protected StatSet getStatsSet() {
        return this._currentSkill.sets[this._currentSkill.currentLevel];
    }

    public List<L2Skill> getSkills() {
        return this._skillsInFile;
    }

    protected String getTableValue(String name) {
        try {
            return ((String[]) this._tables.get(name))[this._currentSkill.currentLevel];
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Error in table: " + name + " of Skill Id " + this._currentSkill.id, e);
            return "";
        }
    }

    protected String getTableValue(String name, int idx) {
        try {
            return ((String[]) this._tables.get(name))[idx - 1];
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "wrong level count in skill Id " + this._currentSkill.id, e);
            return "";
        }
    }

    protected void parseDocument(Document doc) {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("skill".equalsIgnoreCase(d.getNodeName())) {
                        this.setCurrentSkill(new Skill());
                        this.parseSkill(d);
                        this._skillsInFile.addAll(this._currentSkill.skills);
                        this.resetTable();
                    }
                }
            } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
                this.setCurrentSkill(new Skill());
                this.parseSkill(n);
                this._skillsInFile.addAll(this._currentSkill.skills);
            }
        }

    }

    private void parseSkill(Node n) {
        NamedNodeMap attrs = n.getAttributes();
        int enchantLevels1 = 0;
        int enchantLevels2 = 0;
        int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
        String skillName = attrs.getNamedItem("name").getNodeValue();
        String levels = attrs.getNamedItem("levels").getNodeValue();
        int lastLvl = Integer.parseInt(levels);
        if (attrs.getNamedItem("enchantLevels1") != null) {
            enchantLevels1 = Integer.parseInt(attrs.getNamedItem("enchantLevels1").getNodeValue());
        }

        if (attrs.getNamedItem("enchantLevels2") != null) {
            enchantLevels2 = Integer.parseInt(attrs.getNamedItem("enchantLevels2").getNodeValue());
        }

        this._currentSkill.id = skillId;
        this._currentSkill.name = skillName;
        this._currentSkill.sets = new StatSet[lastLvl];
        this._currentSkill.enchsets1 = new StatSet[enchantLevels1];
        this._currentSkill.enchsets2 = new StatSet[enchantLevels2];

        for (int i = 0; i < lastLvl; ++i) {
            this._currentSkill.sets[i] = new StatSet();
            this._currentSkill.sets[i].set("skill_id", this._currentSkill.id);
            this._currentSkill.sets[i].set("level", i + 1);
            this._currentSkill.sets[i].set("name", this._currentSkill.name);
        }

        if (this._currentSkill.sets.length != lastLvl) {
            throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
        } else {
            Node first = n.getFirstChild();

            for (Node var15 = first; var15 != null; var15 = var15.getNextSibling()) {
                if ("table".equalsIgnoreCase(var15.getNodeName())) {
                    this.parseTable(var15);
                }
            }

            for (int i = 1; i <= lastLvl; ++i) {
                for (Node var16 = first; var16 != null; var16 = var16.getNextSibling()) {
                    if ("set".equalsIgnoreCase(var16.getNodeName())) {
                        this.parseBeanSet(var16, this._currentSkill.sets[i - 1], i);
                    }
                }
            }

            for (int i = 0; i < enchantLevels1; ++i) {
                this._currentSkill.enchsets1[i] = new StatSet();
                this._currentSkill.enchsets1[i].set("skill_id", this._currentSkill.id);
                this._currentSkill.enchsets1[i].set("level", i + 101);
                this._currentSkill.enchsets1[i].set("name", this._currentSkill.name);

                for (Node var17 = first; var17 != null; var17 = var17.getNextSibling()) {
                    if ("set".equalsIgnoreCase(var17.getNodeName())) {
                        this.parseBeanSet(var17, this._currentSkill.enchsets1[i], this._currentSkill.sets.length);
                    }
                }

                for (Node var18 = first; var18 != null; var18 = var18.getNextSibling()) {
                    if ("enchant1".equalsIgnoreCase(var18.getNodeName())) {
                        this.parseBeanSet(var18, this._currentSkill.enchsets1[i], i + 1);
                    }
                }
            }

            if (this._currentSkill.enchsets1.length != enchantLevels1) {
                throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels1 + " levels expected");
            } else {
                for (int i = 0; i < enchantLevels2; ++i) {
                    this._currentSkill.enchsets2[i] = new StatSet();
                    this._currentSkill.enchsets2[i].set("skill_id", this._currentSkill.id);
                    this._currentSkill.enchsets2[i].set("level", i + 141);
                    this._currentSkill.enchsets2[i].set("name", this._currentSkill.name);

                    for (Node var19 = first; var19 != null; var19 = var19.getNextSibling()) {
                        if ("set".equalsIgnoreCase(var19.getNodeName())) {
                            this.parseBeanSet(var19, this._currentSkill.enchsets2[i], this._currentSkill.sets.length);
                        }
                    }

                    for (Node var20 = first; var20 != null; var20 = var20.getNextSibling()) {
                        if ("enchant2".equalsIgnoreCase(var20.getNodeName())) {
                            this.parseBeanSet(var20, this._currentSkill.enchsets2[i], i + 1);
                        }
                    }
                }

                if (this._currentSkill.enchsets2.length != enchantLevels2) {
                    throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels2 + " levels expected");
                } else {
                    try {
                        this.makeSkills();
                    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    for (int i = 0; i < lastLvl; ++i) {
                        this._currentSkill.currentLevel = i;

                        for (Node var21 = first; var21 != null; var21 = var21.getNextSibling()) {
                            if ("cond".equalsIgnoreCase(var21.getNodeName())) {
                                Condition condition = this.parseCondition(var21.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                Node msg = var21.getAttributes().getNamedItem("msg");
                                Node msgId = var21.getAttributes().getNamedItem("msgId");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                } else if (condition != null && msgId != null) {
                                    condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
                                    Node addName = var21.getAttributes().getNamedItem("addName");
                                    if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                                        condition.addName();
                                    }
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            } else if ("for".equalsIgnoreCase(var21.getNodeName())) {
                                this.parseTemplate(var21, this._currentSkill.currentSkills.get(i));
                            }
                        }
                    }

                    for (int i = lastLvl; i < lastLvl + enchantLevels1; ++i) {
                        this._currentSkill.currentLevel = i - lastLvl;
                        boolean foundCond = false;
                        boolean foundFor = false;

                        for (Node var22 = first; var22 != null; var22 = var22.getNextSibling()) {
                            if ("enchant1cond".equalsIgnoreCase(var22.getNodeName())) {
                                foundCond = true;
                                Condition condition = this.parseCondition(var22.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                Node msg = var22.getAttributes().getNamedItem("msg");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            } else if ("enchant1for".equalsIgnoreCase(var22.getNodeName())) {
                                foundFor = true;
                                this.parseTemplate(var22, this._currentSkill.currentSkills.get(i));
                            }
                        }

                        if (!foundCond || !foundFor) {
                            this._currentSkill.currentLevel = lastLvl - 1;

                            for (Node var23 = first; var23 != null; var23 = var23.getNextSibling()) {
                                if (!foundCond && "cond".equalsIgnoreCase(var23.getNodeName())) {
                                    Condition condition = this.parseCondition(var23.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                    Node msg = var23.getAttributes().getNamedItem("msg");
                                    if (condition != null && msg != null) {
                                        condition.setMessage(msg.getNodeValue());
                                    }

                                    this._currentSkill.currentSkills.get(i).attach(condition, false);
                                } else if (!foundFor && "for".equalsIgnoreCase(var23.getNodeName())) {
                                    this.parseTemplate(var23, this._currentSkill.currentSkills.get(i));
                                }
                            }
                        }
                    }

                    for (int i = lastLvl + enchantLevels1; i < lastLvl + enchantLevels1 + enchantLevels2; ++i) {
                        boolean foundCond = false;
                        boolean foundFor = false;
                        this._currentSkill.currentLevel = i - lastLvl - enchantLevels1;

                        for (Node var24 = first; var24 != null; var24 = var24.getNextSibling()) {
                            if ("enchant2cond".equalsIgnoreCase(var24.getNodeName())) {
                                foundCond = true;
                                Condition condition = this.parseCondition(var24.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                Node msg = var24.getAttributes().getNamedItem("msg");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            } else if ("enchant2for".equalsIgnoreCase(var24.getNodeName())) {
                                foundFor = true;
                                this.parseTemplate(var24, this._currentSkill.currentSkills.get(i));
                            }
                        }

                        if (!foundCond || !foundFor) {
                            this._currentSkill.currentLevel = lastLvl - 1;

                            for (Node var25 = first; var25 != null; var25 = var25.getNextSibling()) {
                                if (!foundCond && "cond".equalsIgnoreCase(var25.getNodeName())) {
                                    Condition condition = this.parseCondition(var25.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                    Node msg = var25.getAttributes().getNamedItem("msg");
                                    if (condition != null && msg != null) {
                                        condition.setMessage(msg.getNodeValue());
                                    }

                                    this._currentSkill.currentSkills.get(i).attach(condition, false);
                                } else if (!foundFor && "for".equalsIgnoreCase(var25.getNodeName())) {
                                    this.parseTemplate(var25, this._currentSkill.currentSkills.get(i));
                                }
                            }
                        }
                    }

                    this._currentSkill.skills.addAll(this._currentSkill.currentSkills);
                }
            }
        }
    }

    private void makeSkills() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int count = 0;
        this._currentSkill.currentSkills = new ArrayList<>(this._currentSkill.sets.length + this._currentSkill.enchsets1.length + this._currentSkill.enchsets2.length);

        for (int i = 0; i < this._currentSkill.sets.length; ++i) {
            try {
                this._currentSkill.currentSkills.add(i, this._currentSkill.sets[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.sets[i]));
                ++count;
            } catch (Exception e) {
                int var10002 = this._currentSkill.sets[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.sets[i]).getId();
                LOGGER.log(Level.SEVERE, "Skill id=" + var10002 + "level" + this._currentSkill.sets[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.sets[i]).getLevel(), e);
            }
        }

        int _count = count;

        for (int i = 0; i < this._currentSkill.enchsets1.length; ++i) {
            try {
                this._currentSkill.currentSkills.add(_count + i, this._currentSkill.enchsets1[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets1[i]));
                ++count;
            } catch (Exception e) {
                int var15 = this._currentSkill.enchsets1[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets1[i]).getId();
                LOGGER.log(Level.SEVERE, "Skill id=" + var15 + " level=" + this._currentSkill.enchsets1[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets1[i]).getLevel(), e);
            }
        }

        _count = count;

        for (int i = 0; i < this._currentSkill.enchsets2.length; ++i) {
            try {
                this._currentSkill.currentSkills.add(_count + i, this._currentSkill.enchsets2[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets2[i]));
                ++count;
            } catch (Exception e) {
                int var16 = this._currentSkill.enchsets2[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets2[i]).getId();
                LOGGER.log(Level.SEVERE, "Skill id=" + var16 + " level=" + this._currentSkill.enchsets2[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets2[i]).getLevel(), e);
            }
        }

    }

    public static class Skill {
        public int id;
        public String name;
        public StatSet[] sets;
        public StatSet[] enchsets1;
        public StatSet[] enchsets2;
        public int currentLevel;
        public final List<L2Skill> skills = new ArrayList<>();
        public List<L2Skill> currentSkills = new ArrayList<>();
    }
}
