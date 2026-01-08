/**/
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.Config;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.*;
import net.sf.l2j.gameserver.model.ChanceCondition;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.basefuncs.*;
import net.sf.l2j.gameserver.skills.conditions.*;
import net.sf.l2j.gameserver.skills.effects.EffectChanceSkillTrigger;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class DocumentBase {
    static final Logger _log = Logger.getLogger(DocumentBase.class.getName());
    private final File _file;
    protected Map<String, String[]> _tables;

    DocumentBase(File pFile) {
        this._file = pFile;
        this._tables = new HashMap<>();
    }

    public void parse() {
        Document doc = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(this._file);
            this.parseDocument(doc);
        } catch (Exception e) {
            _log.log(Level.SEVERE, "Error loading file " + this._file, e);
        }

    }

    protected abstract void parseDocument(Document var1);

    protected abstract StatSet getStatsSet();

    protected abstract String getTableValue(String var1);

    protected abstract String getTableValue(String var1, int var2);

    protected void resetTable() {
        this._tables = new HashMap<>();
    }

    protected void setTable(String name, String[] table) {
        this._tables.put(name, table);
    }

    protected void parseTemplate(Node n, Object template) {
        Condition condition = null;
        n = n.getFirstChild();
        if (n != null) {
            if ("cond".equalsIgnoreCase(n.getNodeName())) {
                condition = this.parseCondition(n.getFirstChild(), template);
                Node msg = n.getAttributes().getNamedItem("msg");
                Node msgId = n.getAttributes().getNamedItem("msgId");
                if (condition != null && msg != null) {
                    condition.setMessage(msg.getNodeValue());
                } else if (condition != null && msgId != null) {
                    condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
                    Node addName = n.getAttributes().getNamedItem("addName");
                    if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                        condition.addName();
                    }
                }

                n = n.getNextSibling();
            }

            for (; n != null; n = n.getNextSibling()) {
                if ("add".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Add", condition);
                } else if ("addMul".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "AddMul", condition);
                } else if ("sub".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Sub", condition);
                } else if ("subDiv".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "SubDiv", condition);
                } else if ("mul".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Mul", condition);
                } else if ("basemul".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "BaseMul", condition);
                } else if ("div".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Div", condition);
                } else if ("set".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Set", condition);
                } else if ("enchant".equalsIgnoreCase(n.getNodeName())) {
                    this.attachFunc(n, template, "Enchant", condition);
                } else if ("effect".equalsIgnoreCase(n.getNodeName())) {
                    if (template instanceof EffectTemplate) {
                        throw new RuntimeException("Nested effects");
                    }

                    this.attachEffect(n, template, condition);
                }
            }

        }
    }

    protected void attachFunc(Node n, Object template, String name, Condition attachCond) {
        Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
        String order = n.getAttributes().getNamedItem("order").getNodeValue();
        Lambda lambda = this.getLambda(n, template);
        int ord = Integer.decode(this.getValue(order, template));
        Condition applayCond = this.parseCondition(n.getFirstChild(), template);
        FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
        if (template instanceof Item) {
            ((Item) template).attach(ft);
        } else if (template instanceof L2Skill) {
            ((L2Skill) template).attach(ft);
        } else if (template instanceof EffectTemplate) {
            ((EffectTemplate) template).attach(ft);
        }

    }

    protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc) {
        String name = n.getNodeName();
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        name = sb.toString();
        Lambda lambda = this.getLambda(n, template);
        FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.getFuncs().size(), lambda);
        calc.addFunc(ft.getFunc(new Env(), calc));
    }

    protected void attachEffect(Node n, Object template, Condition attachCond) {
        NamedNodeMap attrs = n.getAttributes();
        String name = this.getValue(attrs.getNamedItem("name").getNodeValue().intern(), template);
        int time = 1;
        int count = 1;
        if (attrs.getNamedItem("count") != null) {
            count = Integer.decode(this.getValue(attrs.getNamedItem("count").getNodeValue(), template));
        }

        if (attrs.getNamedItem("time") != null) {
            time = Integer.decode(this.getValue(attrs.getNamedItem("time").getNodeValue(), template));
        }

        if (Config.ENABLE_MODIFY_SKILL_DURATION && Config.SKILL_DURATION_LIST.containsKey(((L2Skill) template).getId())) {
            if (((L2Skill) template).getLevel() < 100) {
                time = Config.SKILL_DURATION_LIST.get(((L2Skill) template).getId());
            } else if (((L2Skill) template).getLevel() >= 100 && ((L2Skill) template).getLevel() < 140) {
                time += Config.SKILL_DURATION_LIST.get(((L2Skill) template).getId());
            } else if (((L2Skill) template).getLevel() > 140) {
                time = Config.SKILL_DURATION_LIST.get(((L2Skill) template).getId());
            }
        }

        boolean self = false;
        if (attrs.getNamedItem("self") != null && Integer.decode(this.getValue(attrs.getNamedItem("self").getNodeValue(), template)) == 1) {
            self = true;
        }

        boolean icon = true;
        if (attrs.getNamedItem("noicon") != null && Integer.decode(this.getValue(attrs.getNamedItem("noicon").getNodeValue(), template)) == 1) {
            icon = false;
        }

        Lambda lambda = this.getLambda(n, template);
        Condition applayCond = this.parseCondition(n.getFirstChild(), template);
        AbnormalEffect abnormal = AbnormalEffect.NULL;
        if (attrs.getNamedItem("abnormal") != null) {
            String abn = attrs.getNamedItem("abnormal").getNodeValue();
            abnormal = AbnormalEffect.getByName(abn);
        }

        String stackType = "none";
        if (attrs.getNamedItem("stackType") != null) {
            stackType = attrs.getNamedItem("stackType").getNodeValue();
        }

        float stackOrder = 0.0F;
        if (attrs.getNamedItem("stackOrder") != null) {
            stackOrder = Float.parseFloat(this.getValue(attrs.getNamedItem("stackOrder").getNodeValue(), template));
        }

        double effectPower = -1.0F;
        if (attrs.getNamedItem("effectPower") != null) {
            effectPower = Double.parseDouble(this.getValue(attrs.getNamedItem("effectPower").getNodeValue(), template));
        }

        L2SkillType type = null;
        if (attrs.getNamedItem("effectType") != null) {
            String typeName = this.getValue(attrs.getNamedItem("effectType").getNodeValue(), template);

            try {
                type = Enum.valueOf(L2SkillType.class, typeName);
            } catch (Exception var25) {
                throw new IllegalArgumentException("Not skilltype found for: " + typeName);
            }
        }

        boolean isChanceSkillTrigger = name.equals(EffectChanceSkillTrigger.class.getName());
        int trigId = 0;
        if (attrs.getNamedItem("triggeredId") != null) {
            trigId = Integer.parseInt(this.getValue(attrs.getNamedItem("triggeredId").getNodeValue(), template));
        } else if (isChanceSkillTrigger) {
            throw new NoSuchElementException(name + " requires triggerId");
        }

        int trigLvl = 1;
        if (attrs.getNamedItem("triggeredLevel") != null) {
            trigLvl = Integer.parseInt(this.getValue(attrs.getNamedItem("triggeredLevel").getNodeValue(), template));
        }

        String chanceCond = null;
        if (attrs.getNamedItem("chanceType") != null) {
            chanceCond = this.getValue(attrs.getNamedItem("chanceType").getNodeValue(), template);
        } else if (isChanceSkillTrigger) {
            throw new NoSuchElementException(name + " requires chanceType");
        }

        int activationChance = -1;
        if (attrs.getNamedItem("activationChance") != null) {
            activationChance = Integer.parseInt(this.getValue(attrs.getNamedItem("activationChance").getNodeValue(), template));
        }

        ChanceCondition chance = ChanceCondition.parse(chanceCond, activationChance);
        if (chance == null && isChanceSkillTrigger) {
            throw new NoSuchElementException("Invalid chance condition: " + chanceCond + " " + activationChance);
        } else {
            EffectTemplate lt = new EffectTemplate(attachCond, applayCond, name, lambda, count, time, abnormal, stackType, stackOrder, icon, effectPower, type, trigId, trigLvl, chance);
            this.parseTemplate(n, lt);
            if (template instanceof L2Skill) {
                if (self) {
                    ((L2Skill) template).attachSelf(lt);
                } else {
                    ((L2Skill) template).attach(lt);
                }
            }

        }
    }

    protected Condition parseCondition(Node n, Object template) {
        while (n != null && n.getNodeType() != 1) {
            n = n.getNextSibling();
        }

        if (n == null) {
            return null;
        } else if ("and".equalsIgnoreCase(n.getNodeName())) {
            return this.parseLogicAnd(n, template);
        } else if ("or".equalsIgnoreCase(n.getNodeName())) {
            return this.parseLogicOr(n, template);
        } else if ("not".equalsIgnoreCase(n.getNodeName())) {
            return this.parseLogicNot(n, template);
        } else if ("player".equalsIgnoreCase(n.getNodeName())) {
            return this.parsePlayerCondition(n, template);
        } else if ("target".equalsIgnoreCase(n.getNodeName())) {
            return this.parseTargetCondition(n, template);
        } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
            return this.parseSkillCondition(n);
        } else if ("using".equalsIgnoreCase(n.getNodeName())) {
            return this.parseUsingCondition(n);
        } else if ("game".equalsIgnoreCase(n.getNodeName())) {
            return this.parseGameCondition(n);
        } else {
            return null;
        }
    }

    protected Condition parseLogicAnd(Node n, Object template) {
        ConditionLogicAnd cond = new ConditionLogicAnd();

        for (Node var4 = n.getFirstChild(); var4 != null; var4 = var4.getNextSibling()) {
            if (var4.getNodeType() == 1) {
                cond.add(this.parseCondition(var4, template));
            }
        }

        if (cond.conditions == null || cond.conditions.length == 0) {
            _log.severe("Empty <and> condition in " + this._file);
        }

        return cond;
    }

    protected Condition parseLogicOr(Node n, Object template) {
        ConditionLogicOr cond = new ConditionLogicOr();

        for (Node var4 = n.getFirstChild(); var4 != null; var4 = var4.getNextSibling()) {
            if (var4.getNodeType() == 1) {
                cond.add(this.parseCondition(var4, template));
            }
        }

        if (cond.conditions == null || cond.conditions.length == 0) {
            _log.severe("Empty <or> condition in " + this._file);
        }

        return cond;
    }

    protected Condition parseLogicNot(Node n, Object template) {
        for (Node var3 = n.getFirstChild(); var3 != null; var3 = var3.getNextSibling()) {
            if (var3.getNodeType() == 1) {
                return new ConditionLogicNot(this.parseCondition(var3, template));
            }
        }

        _log.severe("Empty <not> condition in " + this._file);
        return null;
    }

    protected Condition parsePlayerCondition(Node n, Object template) {
        Condition cond = null;
        int[] ElementSeeds = new int[5];
        byte[] forces = new byte[2];
        NamedNodeMap attrs = n.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            Node a = attrs.item(i);
            if ("race".equalsIgnoreCase(a.getNodeName())) {
                ClassRace race = ClassRace.valueOf(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerRace(race));
            } else if ("level".equalsIgnoreCase(a.getNodeName())) {
                int lvl = Integer.decode(this.getValue(a.getNodeValue(), template));
                cond = this.joinAnd(cond, new ConditionPlayerLevel(lvl));
            } else if ("resting".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
            } else if ("riding".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RIDING, val));
            } else if ("flying".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
            } else if ("moving".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
            } else if ("running".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
            } else if ("behind".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
            } else if ("front".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
            } else if ("olympiad".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
            } else if ("ishero".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionPlayerIsHero(val));
            } else if ("hp".equalsIgnoreCase(a.getNodeName())) {
                int hp = Integer.decode(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerHp(hp));
            } else if ("hprate".equalsIgnoreCase(a.getNodeName())) {
                double rate = Double.parseDouble(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerHpPercentage(rate));
            } else if ("mp".equalsIgnoreCase(a.getNodeName())) {
                int hp = Integer.decode(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerMp(hp));
            } else if ("pkCount".equalsIgnoreCase(a.getNodeName())) {
                int expIndex = Integer.decode(this.getValue(a.getNodeValue(), template));
                cond = this.joinAnd(cond, new ConditionPlayerPkCount(expIndex));
            } else if ("battle_force".equalsIgnoreCase(a.getNodeName())) {
                forces[0] = Byte.decode(this.getValue(a.getNodeValue(), null));
            } else if ("spell_force".equalsIgnoreCase(a.getNodeName())) {
                forces[1] = Byte.decode(this.getValue(a.getNodeValue(), null));
            } else if ("charges".equalsIgnoreCase(a.getNodeName())) {
                int value = Integer.decode(this.getValue(a.getNodeValue(), template));
                cond = this.joinAnd(cond, new ConditionPlayerCharges(value));
            } else if ("weight".equalsIgnoreCase(a.getNodeName())) {
                int weight = Integer.decode(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerWeight(weight));
            } else if ("invSize".equalsIgnoreCase(a.getNodeName())) {
                int size = Integer.decode(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerInvSize(size));
            } else if ("pledgeClass".equalsIgnoreCase(a.getNodeName())) {
                int pledgeClass = Integer.decode(this.getValue(a.getNodeValue(), null));
                cond = this.joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
            } else if (!"clanHall".equalsIgnoreCase(a.getNodeName())) {
                if ("castle".equalsIgnoreCase(a.getNodeName())) {
                    int castle = Integer.decode(this.getValue(a.getNodeValue(), null));
                    cond = this.joinAnd(cond, new ConditionPlayerHasCastle(castle));
                } else if ("sex".equalsIgnoreCase(a.getNodeName())) {
                    int sex = Integer.decode(this.getValue(a.getNodeValue(), null));
                    cond = this.joinAnd(cond, new ConditionPlayerSex(sex));
                } else if ("active_effect_id".equalsIgnoreCase(a.getNodeName())) {
                    int effect_id = Integer.decode(this.getValue(a.getNodeValue(), template));
                    cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
                } else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName())) {
                    String val = this.getValue(a.getNodeValue(), template);
                    int effect_id = Integer.decode(this.getValue(val.split(",")[0], template));
                    int effect_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
                    cond = this.joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
                } else if ("active_skill_id".equalsIgnoreCase(a.getNodeName())) {
                    int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
                    cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
                } else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName())) {
                    String val = this.getValue(a.getNodeValue(), template);
                    int skill_id = Integer.decode(this.getValue(val.split(",")[0], template));
                    int skill_lvl = Integer.decode(this.getValue(val.split(",")[1], template));
                    cond = this.joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
                } else if ("seed_fire".equalsIgnoreCase(a.getNodeName())) {
                    ElementSeeds[0] = Integer.decode(this.getValue(a.getNodeValue(), null));
                } else if ("seed_water".equalsIgnoreCase(a.getNodeName())) {
                    ElementSeeds[1] = Integer.decode(this.getValue(a.getNodeValue(), null));
                } else if ("seed_wind".equalsIgnoreCase(a.getNodeName())) {
                    ElementSeeds[2] = Integer.decode(this.getValue(a.getNodeValue(), null));
                } else if ("seed_various".equalsIgnoreCase(a.getNodeName())) {
                    ElementSeeds[3] = Integer.decode(this.getValue(a.getNodeValue(), null));
                } else if ("seed_any".equalsIgnoreCase(a.getNodeName())) {
                    ElementSeeds[4] = Integer.decode(this.getValue(a.getNodeValue(), null));
                }
            } else {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());

                while (st.hasMoreTokens()) {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(this.getValue(item, null)));
                }

                cond = this.joinAnd(cond, new ConditionPlayerHasClanHall(array));
            }
        }

        for (int elementSeed : ElementSeeds) {
            if (elementSeed > 0) {
                cond = this.joinAnd(cond, new ConditionElementSeed(ElementSeeds));
                break;
            }
        }

        if (forces[0] + forces[1] > 0) {
            cond = this.joinAnd(cond, new ConditionForceBuff(forces));
        }

        if (cond == null) {
            _log.severe("Unrecognized <player> condition in " + this._file);
        }

        return cond;
    }

    protected Condition parseTargetCondition(Node n, Object template) {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            Node a = attrs.item(i);
            if ("hp_min_max".equalsIgnoreCase(a.getNodeName())) {
                String val = this.getValue(a.getNodeValue(), template);
                int hpMin = Integer.decode(this.getValue(val.split(",")[0], template));
                int hpMax = Integer.decode(this.getValue(val.split(",")[1], template));
                cond = this.joinAnd(cond, new ConditionTargetHpMinMax(hpMin, hpMax));
            } else if ("active_skill_id".equalsIgnoreCase(a.getNodeName())) {
                int skill_id = Integer.decode(this.getValue(a.getNodeValue(), template));
                cond = this.joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
            } else if ("race_id".equalsIgnoreCase(a.getNodeName())) {
                List<Integer> array = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");

                while (st.hasMoreTokens()) {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(this.getValue(item, null)));
                }

                cond = this.joinAnd(cond, new ConditionTargetRaceId(array));
            } else if ("npcId".equalsIgnoreCase(a.getNodeName())) {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());

                while (st.hasMoreTokens()) {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(this.getValue(item, null)));
                }

                cond = this.joinAnd(cond, new ConditionTargetNpcId(array));
            }
        }

        if (cond == null) {
            _log.severe("Unrecognized <target> condition in " + this._file);
        }

        return cond;
    }

    protected Condition parseSkillCondition(Node n) {
        NamedNodeMap attrs = n.getAttributes();
        Stats stat = Stats.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
        return new ConditionSkillStats(stat);
    }

    protected Condition parseUsingCondition(Node n) {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            Node a = attrs.item(i);
            if ("kind".equalsIgnoreCase(a.getNodeName())) {
                int mask = 0;
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");

                while (st.hasMoreTokens()) {
                    int old = mask;
                    String item = st.nextToken();

                    for (WeaponType wt : WeaponType.values()) {
                        if (wt.name().equals(item)) {
                            mask |= wt.mask();
                            break;
                        }
                    }

                    for (ArmorType at : ArmorType.values()) {
                        if (at.name().equals(item)) {
                            mask |= at.mask();
                            break;
                        }
                    }

                    if (old == mask) {
                        _log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
                    }
                }

                cond = this.joinAnd(cond, new ConditionUsingItemType(mask));
            }
        }

        if (cond == null) {
            _log.severe("Unrecognized <using> condition in " + this._file);
        }

        return cond;
    }

    protected Condition parseGameCondition(Node n) {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();

        for (int i = 0; i < attrs.getLength(); ++i) {
            Node a = attrs.item(i);
            if ("night".equalsIgnoreCase(a.getNodeName())) {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = this.joinAnd(cond, new ConditionGameTime(val));
            }
        }

        if (cond == null) {
            _log.severe("Unrecognized <game> condition in " + this._file);
        }

        return cond;
    }

    protected void parseTable(Node n) {
        NamedNodeMap attrs = n.getAttributes();
        String name = attrs.getNamedItem("name").getNodeValue();
        if (name.charAt(0) != '#') {
            throw new IllegalArgumentException("Table name must start with #");
        } else {
            StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
            List<String> array = new ArrayList<>(data.countTokens());

            while (data.hasMoreTokens()) {
                array.add(data.nextToken());
            }

            this.setTable(name, array.toArray(new String[0]));
        }
    }

    protected void parseBeanSet(Node n, StatSet set, Integer level) {
        String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
        String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
        char ch = value.isEmpty() ? 32 : value.charAt(0);
        if (ch != '#' && ch != '-' && !Character.isDigit(ch)) {
            set.set(name, value);
        } else {
            set.set(name, String.valueOf(this.getValue(value, level)));
        }

    }

    protected Lambda getLambda(Node n, Object template) {
        Node nval = n.getAttributes().getNamedItem("val");
        if (nval != null) {
            String val = nval.getNodeValue();
            if (val.charAt(0) == '#') {
                return new LambdaConst(Double.parseDouble(this.getTableValue(val)));
            } else if (val.charAt(0) == '$') {
                if (val.equalsIgnoreCase("$player_level")) {
                    return new LambdaStats(StatsType.PLAYER_LEVEL);
                } else if (val.equalsIgnoreCase("$target_level")) {
                    return new LambdaStats(StatsType.TARGET_LEVEL);
                } else if (val.equalsIgnoreCase("$player_max_hp")) {
                    return new LambdaStats(StatsType.PLAYER_MAX_HP);
                } else if (val.equalsIgnoreCase("$player_max_mp")) {
                    return new LambdaStats(StatsType.PLAYER_MAX_MP);
                } else {
                    StatSet set = this.getStatsSet();
                    String field = set.getString(val.substring(1));
                    if (field != null) {
                        return new LambdaConst(Double.parseDouble(this.getValue(field, template)));
                    } else {
                        throw new IllegalArgumentException("Unknown value " + val);
                    }
                }
            } else {
                return new LambdaConst(Double.parseDouble(val));
            }
        } else {
            LambdaCalc calc = new LambdaCalc();

            for (n = n.getFirstChild(); n != null && n.getNodeType() != 1; n = n.getNextSibling()) {
            }

            if (n != null && "val".equals(n.getNodeName())) {
                for (Node var8 = n.getFirstChild(); var8 != null; var8 = var8.getNextSibling()) {
                    if (var8.getNodeType() == 1) {
                        this.attachLambdaFunc(var8, template, calc);
                    }
                }

                return calc;
            } else {
                throw new IllegalArgumentException("Value not specified");
            }
        }
    }

    protected String getValue(String value, Object template) {
        if (value.charAt(0) == '#') {
            if (template instanceof L2Skill) {
                return this.getTableValue(value);
            } else if (template instanceof Integer) {
                return this.getTableValue(value, (Integer) template);
            } else {
                throw new IllegalStateException();
            }
        } else {
            return value;
        }
    }

    protected Condition joinAnd(Condition cond, Condition c) {
        if (cond == null) {
            return c;
        } else if (cond instanceof ConditionLogicAnd) {
            ((ConditionLogicAnd) cond).add(c);
            return cond;
        } else {
            ConditionLogicAnd and = new ConditionLogicAnd();
            and.add(cond);
            and.add(c);
            return and;
        }
    }
}
