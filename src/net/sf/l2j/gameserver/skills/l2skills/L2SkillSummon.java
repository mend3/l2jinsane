package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.model.actor.player.Experience;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class L2SkillSummon extends L2Skill {
    public static final int SKILL_CUBIC_MASTERY = 143;

    private final int _npcId;

    private final float _expPenalty;

    private final boolean _isCubic;

    private final int _activationtime;

    private final int _activationchance;

    private final int _summonTotalLifeTime;

    private final int _summonTimeLostIdle;

    private final int _summonTimeLostActive;

    private final int _itemConsumeTime;

    private final int _itemConsumeOT;

    private final int _itemConsumeIdOT;

    private final int _itemConsumeSteps;

    public L2SkillSummon(StatSet set) {
        super(set);
        this._npcId = set.getInteger("npcId", 0);
        this._expPenalty = set.getFloat("expPenalty", 0.0F);
        this._isCubic = set.getBool("isCubic", false);
        this._activationtime = set.getInteger("activationtime", 8);
        this._activationchance = set.getInteger("activationchance", 30);
        this._summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
        this._summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
        this._summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
        this._itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
        this._itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
        this._itemConsumeTime = set.getInteger("itemConsumeTime", 0);
        this._itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
    }

    public boolean checkCondition(Creature activeChar) {
        if (activeChar instanceof Player player) {
            if (isCubic()) {
                if (getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
                    return true;
                if (player.getCubics().size() > player.getSkillLevel(143)) {
                    player.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
                    return false;
                }
            } else {
                if (player.isInObserverMode())
                    return false;
                if (player.getSummon() != null) {
                    player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
                    return false;
                }
            }
        }
        return checkCondition(activeChar, null, false);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        Servitor summon = null;
        if (caster.isAlikeDead() || !(caster instanceof Player activeChar))
            return;
        if (this._npcId == 0) {
            activeChar.sendMessage("Summon skill " + getId() + " not described yet");
            return;
        }
        if (this._isCubic) {
            int _cubicSkillLevel = getLevel();
            if (_cubicSkillLevel > 100)
                _cubicSkillLevel = Math.round(((getLevel() - 100) / 7 + 8));
            if (targets.length > 1) {
                for (WorldObject obj : targets) {
                    if (obj instanceof Player player) {
                        int mastery = player.getSkillLevel(143);
                        if (mastery == 0 && !player.getCubics().isEmpty()) {
                            for (Cubic c : player.getCubics().values()) {
                                c.stopAction();
                                c = null;
                            }
                            player.getCubics().clear();
                        }
                        if (player.getCubics().containsKey(this._npcId)) {
                            Cubic cubic = player.getCubic(this._npcId);
                            cubic.stopAction();
                            cubic.cancelDisappear();
                            player.delCubic(this._npcId);
                        }
                        if (player.getCubics().size() <= mastery) {
                            player.addCubic(this._npcId, _cubicSkillLevel, getPower(), this._activationtime, this._activationchance, this._summonTotalLifeTime, player != activeChar);
                            player.broadcastUserInfo();
                        }
                    }
                }
                return;
            }
            if (activeChar.getCubics().containsKey(this._npcId)) {
                Cubic cubic = activeChar.getCubic(this._npcId);
                cubic.stopAction();
                cubic.cancelDisappear();
                activeChar.delCubic(this._npcId);
            }
            if (activeChar.getCubics().size() > activeChar.getSkillLevel(143)) {
                activeChar.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
                return;
            }
            activeChar.addCubic(this._npcId, _cubicSkillLevel, getPower(), this._activationtime, this._activationchance, this._summonTotalLifeTime, false);
            activeChar.broadcastUserInfo();
            return;
        }
        if (activeChar.getSummon() != null || activeChar.isMounted())
            return;
        NpcTemplate summonTemplate = NpcData.getInstance().getTemplate(this._npcId);
        if (summonTemplate == null) {
            _log.warning("Summon attempt for nonexisting NPC ID: " + this._npcId + ", skill ID: " + getId());
            return;
        }
        if (summonTemplate.isType("SiegeSummon")) {
            SiegeSummon siegeSummon = new SiegeSummon(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
        } else {
            summon = new Servitor(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
        }
        summon.setName(summonTemplate.getName());
        summon.setTitle(activeChar.getName());
        summon.setExpPenalty(this._expPenalty);
        if (summon.getLevel() >= Experience.LEVEL.length) {
            summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
            _log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above 75. Please rectify.");
        } else {
            summon.getStat().setExp(Experience.LEVEL[summon.getLevel() % Experience.LEVEL.length]);
        }
        summon.setCurrentHp(summon.getMaxHp());
        summon.setCurrentMp(summon.getMaxMp());
        summon.setRunning();
        activeChar.setSummon(summon);
        int x = activeChar.getX();
        int y = activeChar.getY();
        int z = activeChar.getZ();
        summon.spawnMe(GeoEngine.getInstance().canMoveToTargetLoc(x, y, z, x + 20, y + 20, z), activeChar.getHeading());
        summon.setFollowStatus(true);
    }

    public final boolean isCubic() {
        return this._isCubic;
    }

    public final int getTotalLifeTime() {
        return this._summonTotalLifeTime;
    }

    public final int getTimeLostIdle() {
        return this._summonTimeLostIdle;
    }

    public final int getTimeLostActive() {
        return this._summonTimeLostActive;
    }

    public final int getItemConsumeOT() {
        return this._itemConsumeOT;
    }

    public final int getItemConsumeIdOT() {
        return this._itemConsumeIdOT;
    }

    public final int getItemConsumeSteps() {
        return this._itemConsumeSteps;
    }

    public final int getItemConsumeTime() {
        return this._itemConsumeTime;
    }
}
