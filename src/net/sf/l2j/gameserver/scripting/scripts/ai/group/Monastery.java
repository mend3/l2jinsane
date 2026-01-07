package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class Monastery extends L2AttackableAIScript {
    private static final int[] BROTHERS_SEEKERS_MONKS = new int[]{22124, 22125, 22126, 22127, 22129};

    private static final int[] GUARDIANS_BEHOLDERS = new int[]{22134, 22135};

    public Monastery() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(BROTHERS_SEEKERS_MONKS, ScriptEventType.ON_AGGRO, ScriptEventType.ON_SPAWN, ScriptEventType.ON_SPELL_FINISHED);
        addEventIds(GUARDIANS_BEHOLDERS, ScriptEventType.ON_SKILL_SEE);
    }

    public String onAggro(Npc npc, Player player, boolean isPet) {
        if (!npc.isInCombat())
            if (player.getActiveWeaponInstance() != null) {
                npc.setTarget(player);
                npc.broadcastNpcSay(((player.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ((player.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ", move your weapon away!");
                switch (npc.getNpcId()) {
                    case 22124:
                    case 22126:
                        npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
                        return super.onAggro(npc, player, isPet);
                }
                attack((Attackable) npc, player);
            } else if (((Attackable) npc).getMostHated() == null) {
                return null;
            }
        return super.onAggro(npc, player, isPet);
    }

    public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet) {
        if (skill.getSkillType() == L2SkillType.AGGDAMAGE)
            for (WorldObject obj : targets) {
                if (obj.equals(npc)) {
                    npc.broadcastNpcSay(((caster.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ((caster.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ", move your weapon away!");
                    attack((Attackable) npc, caster);
                    break;
                }
            }
        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    public String onSpawn(Npc npc) {
        for (Player target : npc.getKnownTypeInRadius(Player.class, npc.getTemplate().getAggroRange())) {
            if (!target.isDead() && GeoEngine.getInstance().canSeeTarget(npc, target))
                if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null) {
                    npc.setTarget(target);
                    npc.broadcastNpcSay(((target.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ((target.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + ", move your weapon away!");
                    switch (npc.getNpcId()) {
                        case 22124:
                        case 22126:
                        case 22127:
                            npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
                            continue;
                    }
                    attack((Attackable) npc, target);
                }
        }
        return super.onSpawn(npc);
    }

    public String onSpellFinished(Npc npc, Player player, L2Skill skill) {
        if (skill.getId() == 4589)
            attack((Attackable) npc, player);
        return super.onSpellFinished(npc, player, skill);
    }
}
