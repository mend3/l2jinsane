/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.skillnode.ClanSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.EnchantSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.FishingSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.SkillNode;
import net.sf.l2j.gameserver.model.pledge.Clan;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.*;

public class SkillTreeData implements IXmlReader {
    private final List<FishingSkillNode> _fishingSkills = new LinkedList<>();
    private final List<ClanSkillNode> _clanSkills = new LinkedList<>();
    private final List<EnchantSkillNode> _enchantSkills = new LinkedList<>();

    protected SkillTreeData() {
    }

    public static SkillTreeData getInstance() {
        return SkillTreeData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/skillstrees");
        LOGGER.info("Loaded {} fishing skills.", this._fishingSkills.size());
        LOGGER.info("Loaded {} clan skills.", this._clanSkills.size());
        LOGGER.info("Loaded {} enchant skills.", this._enchantSkills.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "clanSkill", (clanSkillNode) -> {
                this._clanSkills.add(new ClanSkillNode(this.parseAttributes(clanSkillNode)));
            });
            this.forEach(listNode, "fishingSkill", (fishingSkillNode) -> {
                this._fishingSkills.add(new FishingSkillNode(this.parseAttributes(fishingSkillNode)));
            });
            this.forEach(listNode, "enchantSkill", (enchantSkillNode) -> {
                this._enchantSkills.add(new EnchantSkillNode(this.parseAttributes(enchantSkillNode)));
            });
        });
    }

    public List<FishingSkillNode> getFishingSkillsFor(Player player) {
        List<FishingSkillNode> result = new ArrayList<>();
        this._fishingSkills.stream().filter((s) -> {
            return s.getMinLvl() <= player.getLevel() && (!s.isDwarven() || player.hasDwarvenCraft() && s.isDwarven());
        }).forEach((s) -> {
            if (player.getSkillLevel(s.getId()) == s.getValue() - 1) {
                result.add(s);
            }

        });
        return result;
    }

    public FishingSkillNode getFishingSkillFor(Player player, int skillId, int skillLevel) {
        FishingSkillNode fsn = this._fishingSkills.stream().filter((s) -> {
            return s.getId() == skillId && s.getValue() == skillLevel && (!s.isDwarven() || player.hasDwarvenCraft() && s.isDwarven());
        }).findFirst().orElse(null);
        if (fsn == null) {
            return null;
        } else if (fsn.getMinLvl() > player.getLevel()) {
            return null;
        } else {
            return player.getSkillLevel(skillId) == fsn.getValue() - 1 ? fsn : null;
        }
    }

    public int getRequiredLevelForNextFishingSkill(Player player) {
        return this._fishingSkills.stream().filter((s) -> {
            return s.getMinLvl() > player.getLevel() && (!s.isDwarven() || player.hasDwarvenCraft() && s.isDwarven());
        }).min((s1, s2) -> {
            return Integer.compare(s1.getMinLvl(), s2.getMinLvl());
        }).map(SkillNode::getMinLvl).orElse(0);
    }

    public List<ClanSkillNode> getClanSkillsFor(Player player) {
        Clan clan = player.getClan();
        if (clan == null) {
            return Collections.emptyList();
        } else {
            List<ClanSkillNode> result = new ArrayList<>();
            this._clanSkills.stream().filter((s) -> {
                return s.getMinLvl() <= clan.getLevel();
            }).forEach((s) -> {
                L2Skill clanSkill = clan.getClanSkills().get(s.getId());
                if (clanSkill == null && s.getValue() == 1 || clanSkill != null && clanSkill.getLevel() == s.getValue() - 1) {
                    result.add(s);
                }

            });
            return result;
        }
    }

    public ClanSkillNode getClanSkillFor(Player player, int skillId, int skillLevel) {
        Clan clan = player.getClan();
        if (clan == null) {
            return null;
        } else {
            ClanSkillNode csn = this._clanSkills.stream().filter((s) -> {
                return s.getId() == skillId && s.getValue() == skillLevel;
            }).findFirst().orElse(null);
            if (csn == null) {
                return null;
            } else if (csn.getMinLvl() > clan.getLevel()) {
                return null;
            } else {
                L2Skill clanSkill = clan.getClanSkills().get(skillId);
                return (clanSkill != null || csn.getValue() != 1) && (clanSkill == null || clanSkill.getLevel() != csn.getValue() - 1) ? null : csn;
            }
        }
    }

    public List<EnchantSkillNode> getEnchantSkillsFor(Player player) {
        List<EnchantSkillNode> result = new ArrayList<>();
        Iterator<EnchantSkillNode> var3 = this._enchantSkills.iterator();

        while (true) {
            EnchantSkillNode esn;
            L2Skill skill;
            do {
                do {
                    if (!var3.hasNext()) {
                        return result;
                    }

                    esn = var3.next();
                    skill = player.getSkill(esn.getId());
                } while (skill == null);
            } while ((skill.getLevel() != SkillTable.getInstance().getMaxLevel(skill.getId()) || esn.getValue() != 101 && esn.getValue() != 141) && skill.getLevel() != esn.getValue() - 1);

            result.add(esn);
        }
    }

    public EnchantSkillNode getEnchantSkillFor(Player player, int skillId, int skillLevel) {
        EnchantSkillNode esn = this._enchantSkills.stream().filter((s) -> {
            return s.getId() == skillId && s.getValue() == skillLevel;
        }).findFirst().orElse(null);
        if (esn == null) {
            return null;
        } else {
            int currentSkillLevel = player.getSkillLevel(skillId);
            return (currentSkillLevel != SkillTable.getInstance().getMaxLevel(skillId) || skillLevel != 101 && skillLevel != 141) && currentSkillLevel != skillLevel - 1 ? null : esn;
        }
    }

    private static class SingletonHolder {
        protected static final SkillTreeData INSTANCE = new SkillTreeData();
    }
}