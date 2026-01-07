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
    private final List<L2Skill> _skillsInFile = new ArrayList();
    private DocumentSkill.Skill _currentSkill;

    public DocumentSkill(File file) {
        super(file);
    }

    private void setCurrentSkill(DocumentSkill.Skill skill) {
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
        } catch (RuntimeException var3) {
            LOGGER.warn("Error in table: " + name + " of Skill Id " + this._currentSkill.id, var3);
            return "";
        }
    }

    protected String getTableValue(String name, int idx) {
        try {
            return ((String[]) this._tables.get(name))[idx - 1];
        } catch (RuntimeException var4) {
            LOGGER.warn("wrong level count in skill Id " + this._currentSkill.id, var4);
            return "";
        }
    }

    protected void parseDocument(Document doc) {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("skill".equalsIgnoreCase(d.getNodeName())) {
                        this.setCurrentSkill(new DocumentSkill.Skill(this));
                        this.parseSkill(d);
                        this._skillsInFile.addAll(this._currentSkill.skills);
                        this.resetTable();
                    }
                }
            } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
                this.setCurrentSkill(new DocumentSkill.Skill(this));
                this.parseSkill(n);
                this._skillsInFile.addAll(this._currentSkill.skills);
            }
        }

    }

    protected void parseSkill(Node n) {
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

            for (n = first; n != null; n = n.getNextSibling()) {
                if ("table".equalsIgnoreCase(n.getNodeName())) {
                    this.parseTable(n);
                }
            }

            int i;
            for (i = 1; i <= lastLvl; ++i) {
                for (n = first; n != null; n = n.getNextSibling()) {
                    if ("set".equalsIgnoreCase(n.getNodeName())) {
                        this.parseBeanSet(n, this._currentSkill.sets[i - 1], i);
                    }
                }
            }

            for (i = 0; i < enchantLevels1; ++i) {
                this._currentSkill.enchsets1[i] = new StatSet();
                this._currentSkill.enchsets1[i].set("skill_id", this._currentSkill.id);
                this._currentSkill.enchsets1[i].set("level", i + 101);
                this._currentSkill.enchsets1[i].set("name", this._currentSkill.name);

                for (n = first; n != null; n = n.getNextSibling()) {
                    if ("set".equalsIgnoreCase(n.getNodeName())) {
                        this.parseBeanSet(n, this._currentSkill.enchsets1[i], this._currentSkill.sets.length);
                    }
                }

                for (n = first; n != null; n = n.getNextSibling()) {
                    if ("enchant1".equalsIgnoreCase(n.getNodeName())) {
                        this.parseBeanSet(n, this._currentSkill.enchsets1[i], i + 1);
                    }
                }
            }

            if (this._currentSkill.enchsets1.length != enchantLevels1) {
                throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels1 + " levels expected");
            } else {
                for (i = 0; i < enchantLevels2; ++i) {
                    this._currentSkill.enchsets2[i] = new StatSet();
                    this._currentSkill.enchsets2[i].set("skill_id", this._currentSkill.id);
                    this._currentSkill.enchsets2[i].set("level", i + 141);
                    this._currentSkill.enchsets2[i].set("name", this._currentSkill.name);

                    for (n = first; n != null; n = n.getNextSibling()) {
                        if ("set".equalsIgnoreCase(n.getNodeName())) {
                            this.parseBeanSet(n, this._currentSkill.enchsets2[i], this._currentSkill.sets.length);
                        }
                    }

                    for (n = first; n != null; n = n.getNextSibling()) {
                        if ("enchant2".equalsIgnoreCase(n.getNodeName())) {
                            this.parseBeanSet(n, this._currentSkill.enchsets2[i], i + 1);
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

                    for (i = 0; i < lastLvl; ++i) {
                        this._currentSkill.currentLevel = i;

                        for (n = first; n != null; n = n.getNextSibling()) {
                            if (!"cond".equalsIgnoreCase(n.getNodeName())) {
                                if ("for".equalsIgnoreCase(n.getNodeName())) {
                                    this.parseTemplate(n, this._currentSkill.currentSkills.get(i));
                                }
                            } else {
                                Condition condition = this.parseCondition(n.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                Node msg = n.getAttributes().getNamedItem("msg");
                                Node msgId = n.getAttributes().getNamedItem("msgId");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                } else if (condition != null && msgId != null) {
                                    condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
                                    msg = n.getAttributes().getNamedItem("addName");
                                    if (msg != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                                        condition.addName();
                                    }
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            }
                        }
                    }

                    boolean foundCond;
                    boolean foundFor;
                    Condition condition;
                    for (i = lastLvl; i < lastLvl + enchantLevels1; ++i) {
                        this._currentSkill.currentLevel = i - lastLvl;
                        foundCond = false;
                        foundFor = false;
                        Node msg;

                        for (n = first; n != null; n = n.getNextSibling()) {
                            if ("enchant1cond".equalsIgnoreCase(n.getNodeName())) {
                                foundCond = true;
                                condition = this.parseCondition(n.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                msg = n.getAttributes().getNamedItem("msg");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            } else if ("enchant1for".equalsIgnoreCase(n.getNodeName())) {
                                foundFor = true;
                                this.parseTemplate(n, this._currentSkill.currentSkills.get(i));
                            }
                        }

                        if (!foundCond || !foundFor) {
                            this._currentSkill.currentLevel = lastLvl - 1;

                            for (n = first; n != null; n = n.getNextSibling()) {
                                if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
                                    condition = this.parseCondition(n.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                    msg = n.getAttributes().getNamedItem("msg");
                                    if (condition != null && msg != null) {
                                        condition.setMessage(msg.getNodeValue());
                                    }

                                    this._currentSkill.currentSkills.get(i).attach(condition, false);
                                } else if (!foundFor && "for".equalsIgnoreCase(n.getNodeName())) {
                                    this.parseTemplate(n, this._currentSkill.currentSkills.get(i));
                                }
                            }
                        }
                    }

                    for (i = lastLvl + enchantLevels1; i < lastLvl + enchantLevels1 + enchantLevels2; ++i) {
                        foundCond = false;
                        foundFor = false;
                        this._currentSkill.currentLevel = i - lastLvl - enchantLevels1;
                        Node msg;

                        for (n = first; n != null; n = n.getNextSibling()) {
                            if ("enchant2cond".equalsIgnoreCase(n.getNodeName())) {
                                foundCond = true;
                                condition = this.parseCondition(n.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                msg = n.getAttributes().getNamedItem("msg");
                                if (condition != null && msg != null) {
                                    condition.setMessage(msg.getNodeValue());
                                }

                                this._currentSkill.currentSkills.get(i).attach(condition, false);
                            } else if ("enchant2for".equalsIgnoreCase(n.getNodeName())) {
                                foundFor = true;
                                this.parseTemplate(n, this._currentSkill.currentSkills.get(i));
                            }
                        }

                        if (!foundCond || !foundFor) {
                            this._currentSkill.currentLevel = lastLvl - 1;

                            for (n = first; n != null; n = n.getNextSibling()) {
                                if (!foundCond && "cond".equalsIgnoreCase(n.getNodeName())) {
                                    condition = this.parseCondition(n.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                    msg = n.getAttributes().getNamedItem("msg");
                                    if (condition != null && msg != null) {
                                        condition.setMessage(msg.getNodeValue());
                                    }

                                    this._currentSkill.currentSkills.get(i).attach(condition, false);
                                } else if (!foundFor && "for".equalsIgnoreCase(n.getNodeName())) {
                                    this.parseTemplate(n, this._currentSkill.currentSkills.get(i));
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
        this._currentSkill.currentSkills = new ArrayList(this._currentSkill.sets.length + this._currentSkill.enchsets1.length + this._currentSkill.enchsets2.length);

        Logger var10000;
        Level var10001;
        int _count;
        int var10002;
        for (_count = 0; _count < this._currentSkill.sets.length; ++_count) {
            try {
                this._currentSkill.currentSkills.add(_count, this._currentSkill.sets[_count].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.sets[_count]));
                ++count;
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        _count = count;

        int i;
        for (i = 0; i < this._currentSkill.enchsets1.length; ++i) {
            try {
                this._currentSkill.currentSkills.add(_count + i, this._currentSkill.enchsets1[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets1[i]));
                ++count;
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        _count = count;

        for (i = 0; i < this._currentSkill.enchsets2.length; ++i) {
            try {
                this._currentSkill.currentSkills.add(_count + i, this._currentSkill.enchsets2[i].getEnum("skillType", L2SkillType.class).makeSkill(this._currentSkill.enchsets2[i]));
                ++count;
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

    }

    public class Skill {
        public int id;
        public String name;
        public StatSet[] sets;
        public StatSet[] enchsets1;
        public StatSet[] enchsets2;
        public int currentLevel;
        public List<L2Skill> skills = new ArrayList();
        public List<L2Skill> currentSkills = new ArrayList();

        public Skill(final DocumentSkill param1) {
        }
    }
}