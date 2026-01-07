package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Chest;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class Chests extends L2AttackableAIScript {
    private static final int SKILL_DELUXE_KEY = 2229;

    private static final int SKILL_BOX_KEY = 2065;

    private static final int[] NPC_IDS = new int[]{
            18265, 18266, 18267, 18268, 18269, 18270, 18271, 18272, 18273, 18274,
            18275, 18276, 18277, 18278, 18279, 18280, 18281, 18282, 18283, 18284,
            18285, 18286, 18287, 18288, 18289, 18290, 18291, 18292, 18293, 18294,
            18295, 18296, 18297, 18298, 21671, 21694, 21717, 21740, 21763, 21786,
            21801, 21802, 21803, 21804, 21805, 21806, 21807, 21808, 21809, 21810,
            21811, 21812, 21813, 21814, 21815, 21816, 21817, 21818, 21819, 21820,
            21821, 21822};

    public Chests() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(NPC_IDS, ScriptEventType.ON_ATTACK, ScriptEventType.ON_SKILL_SEE);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (npc instanceof Chest chest) {
            if (!ArraysUtil.contains((Object[]) targets, npc))
                return super.onSkillSee(npc, caster, skill, targets, isPet);
            if (!chest.isInteracted()) {
                chest.setInteracted();
                if (Rnd.get(100) < 40) {
                    int keyLevelNeeded, chance;
                    switch (skill.getId()) {
                        case 2065:
                        case 2229:
                            keyLevelNeeded = chest.getLevel() / 10 - skill.getLevel();
                            if (keyLevelNeeded < 0)
                                keyLevelNeeded *= -1;
                            chance = ((skill.getId() == 2065) ? 60 : 100) - keyLevelNeeded * 40;
                            if (Rnd.get(100) < chance) {
                                chest.setSpecialDrop();
                                chest.doDie(caster);
                            } else {
                                chest.deleteMe();
                            }
                            return super.onSkillSee(npc, caster, skill, targets, isPet);
                    }
                    chest.doCast(SkillTable.getInstance().getInfo(4143, Math.min(10, Math.round((npc.getLevel() / 10)))));
                } else {
                    attack(chest, isPet ? (Creature) caster.getSummon() : (Creature) caster);
                }
            }
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc instanceof Chest chest) {
            if (!chest.isInteracted()) {
                chest.setInteracted();
                if (Rnd.get(100) < 40)
                    chest.doCast(SkillTable.getInstance().getInfo(4143, Math.min(10, Math.round((npc.getLevel() / 10)))));
            }
        }
        return super.onAttack(npc, attacker, damage, skill);
    }
}
