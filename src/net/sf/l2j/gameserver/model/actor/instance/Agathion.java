package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.AgathionData;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

public final class Agathion extends Npc {
    private static final String BUFF_MESSAGE = "Seu Agathion concedeu um buff.";

    private final Player _owner;

    private final ScheduledFuture<?> _despawnTask;

    public Agathion(int objectId, NpcTemplate template, Player owner) {
        super(objectId, template);
        this._owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this._despawnTask = scheduleAutoDespawn();
    }

    public static boolean canBeTargeted() {
        return false;
    }

    private static Optional<int[]> getBuffDataForNpc(int npcId) {
        int[] buff = AgathionData.getBuffForNpc(npcId);
        return (buff != null && buff.length == 2) ? Optional.of(buff) : Optional.empty();
    }

    private ScheduledFuture<?> scheduleAutoDespawn() {
        long lifespan = AgathionData.getAgathionDuration(getNpcId());
        return ThreadPool.schedule(() -> {
            if (isOwnerValid())
                this._owner.despawnAgathion();
        }, lifespan);
    }

    private boolean isOwnerValid() {
        return (this._owner != null);
    }

    public boolean isInvul() {
        return true;
    }

    public void onAction(Player player) {
        if (player != null)
            player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void applyBuffToOwner() {
        if (!Config.AGATHION_BUFF)
            return;
        if (!isOwnerValid() || this._owner.isDead())
            return;
        int npcId = getNpcId();
        Optional<int[]> buffData = getBuffDataForNpc(npcId);
        buffData.ifPresent(this::processBuffApplication);
    }

    private void processBuffApplication(int[] buffData) {
        int skillId = buffData[0];
        int skillLevel = buffData[1];
        this._owner.stopSkillEffects(skillId);
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
        if (skill == null)
            return;
        broadcastSkillEffect(skill);
        skill.getEffects(this, this._owner);
        this._owner.sendMessage("Seu Agathion concedeu um buff.");
    }

    private void broadcastSkillEffect(L2Skill skill) {
        MagicSkillUse magicSkillUse = new MagicSkillUse(this, this._owner, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay());
        broadcastPacket(magicSkillUse);
        MagicSkillUse ownerMagicSkillUse = new MagicSkillUse(this._owner, this._owner, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay());
        this._owner.broadcastPacket(ownerMagicSkillUse);
    }

    public Player getOwner() {
        return this._owner;
    }

    public void deleteMe() {
        if (this._despawnTask != null && !this._despawnTask.isDone())
            this._despawnTask.cancel(false);
        if (isVisible())
            decayMe();
        super.deleteMe();
        cleanupOwnerReference();
        World.getInstance().removeObject(this);
    }

    private void cleanupOwnerReference() {
        if (isOwnerValid() && this._owner.getAgathion() == this)
            this._owner.setAgathion(null);
    }

    public void forceDespawn() {
        deleteMe();
    }
}
