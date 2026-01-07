/**/
package net.sf.l2j.gameserver.model.actor;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.items.ActionType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.SummonAI;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.player.Experience;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate.SkillType;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.Iterator;
import java.util.List;

public abstract class Summon extends Playable {
    private Player _owner;
    private boolean _follow = true;
    private boolean _previousFollowStatus = true;
    private int _shotsMask = 0;

    public Summon(int objectId, NpcTemplate template, Player owner) {
        super(objectId, template);
        Iterator var4 = template.getSkills(SkillType.PASSIVE).iterator();

        while (var4.hasNext()) {
            L2Skill skill = (L2Skill) var4.next();
            this.addStatFuncs(skill.getStatFuncs(this));
        }

        this._showSummonAnimation = true;
        this._owner = owner;
    }

    public void initCharStat() {
        this.setStat(new SummonStat(this));
    }

    public SummonStat getStat() {
        return (SummonStat) super.getStat();
    }

    public void initCharStatus() {
        this.setStatus(new SummonStatus(this));
    }

    public SummonStatus getStatus() {
        return (SummonStatus) super.getStatus();
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null) {
            synchronized (this) {
                if (this._ai == null) {
                    this._ai = new SummonAI(this);
                }

                return this._ai;
            }
        } else {
            return ai;
        }
    }

    public NpcTemplate getTemplate() {
        return (NpcTemplate) super.getTemplate();
    }

    public abstract int getSummonType();

    public void updateAbnormalEffect() {
        Iterator var1 = this.getKnownType(Player.class).iterator();

        while (var1.hasNext()) {
            Player player = (Player) var1.next();
            player.sendPacket(new SummonInfo(this, player, 1));
        }

    }

    public boolean isMountable() {
        return false;
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (player == this._owner) {
            if (!this.canInteract(player)) {
                player.getAI().setIntention(IntentionType.INTERACT, this);
            } else {
                if (player.isMoving() || player.isInCombat()) {
                    player.getAI().setIntention(IntentionType.IDLE);
                }

                player.sendPacket(new MoveToPawn(player, this, 150));
                player.sendPacket(new PetStatusShow(this));
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else if (this.isAutoAttackable(player)) {
            if (GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.ATTACK, this);
                player.onActionRequest();
            }
        } else {
            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            if (GeoEngine.getInstance().canSeeTarget(player, this)) {
                player.getAI().setIntention(IntentionType.FOLLOW, this);
            }
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/admin/petinfo.htm");
            html.replace("%name%", this.getName() == null ? "N/A" : this.getName());
            html.replace("%level%", this.getLevel());
            html.replace("%exp%", this.getStat().getExp());
            String var10002 = this.getActingPlayer().getName();
            html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + var10002 + "\">" + this.getActingPlayer().getName() + "</a>");
            html.replace("%class%", this.getClass().getSimpleName());
            html.replace("%ai%", this.hasAI() ? this.getAI().getDesire().getIntention().name() : "NULL");
            int var3 = (int) this.getStatus().getCurrentHp();
            html.replace("%hp%", var3 + "/" + this.getStat().getMaxHp());
            var3 = (int) this.getStatus().getCurrentMp();
            html.replace("%mp%", var3 + "/" + this.getStat().getMaxMp());
            html.replace("%karma%", this.getKarma());
            html.replace("%undead%", this.isUndead() ? "yes" : "no");
            if (this instanceof Pet) {
                html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + this.getActingPlayer().getObjectId() + "\">view</a>");
                var3 = ((Pet) this).getCurrentFed();
                html.replace("%food%", var3 + "/" + ((Pet) this).getPetData().getMaxMeal());
                var3 = this.getInventory().getTotalWeight();
                html.replace("%load%", var3 + "/" + this.getMaxLoad());
            } else {
                html.replace("%inv%", "none");
                html.replace("%food%", "N/A");
                html.replace("%load%", "N/A");
            }

            player.sendPacket(html);
        }

        super.onActionShift(player);
    }

    public long getExpForThisLevel() {
        return this.getLevel() >= Experience.LEVEL.length ? 0L : Experience.LEVEL[this.getLevel()];
    }

    public long getExpForNextLevel() {
        return this.getLevel() >= Experience.LEVEL.length - 1 ? 0L : Experience.LEVEL[this.getLevel() + 1];
    }

    public final int getKarma() {
        return this.getOwner() != null ? this.getOwner().getKarma() : 0;
    }

    public final byte getPvpFlag() {
        return this.getOwner() != null ? this.getOwner().getPvpFlag() : 0;
    }

    public final TeamType getTeam() {
        return this.getOwner() != null ? this.getOwner().getTeam() : TeamType.NONE;
    }

    public final Player getOwner() {
        return this._owner;
    }

    public void setOwner(Player newOwner) {
        this._owner = newOwner;
    }

    public final int getNpcId() {
        return this.getTemplate().getNpcId();
    }

    public int getMaxLoad() {
        return 0;
    }

    public int getSoulShotsPerHit() {
        return this.getTemplate().getSsCount();
    }

    public int getSpiritShotsPerHit() {
        return this.getTemplate().getSpsCount();
    }

    public void followOwner() {
        this.setFollowStatus(true);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            Iterator var2 = this.getOwner().getAutoSoulShot().iterator();

            while (var2.hasNext()) {
                int itemId = (Integer) var2.next();
                switch (ItemTable.getInstance().getTemplate(itemId).getDefaultAction()) {
                    case summon_soulshot:
                    case summon_spiritshot:
                        this.getOwner().disableAutoShot(itemId);
                }
            }

            return true;
        }
    }

    public void onDecay() {
        if (this._owner.getSummon() == this) {
            this.deleteMe(this._owner);
        }
    }

    public void broadcastStatusUpdate() {
        super.broadcastStatusUpdate();
        this.updateAndBroadcastStatus(1);
    }

    public void deleteMe(Player owner) {
        owner.setSummon(null);
        owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
        this.decayMe();
        super.deleteMe();
    }

    public void unSummon(Player owner) {
        if (this.isVisible() && !this.isDead()) {
            this.abortCast();
            this.abortAttack();
            this.setTarget(null);
            this.stopHpMpRegeneration();
            this.stopAllEffects();
            this.store();
            owner.setSummon(null);
            owner.sendPacket(new PetDelete(this.getSummonType(), this.getObjectId()));
            this.decayMe();
            super.deleteMe();
            Iterator var2 = owner.getAutoSoulShot().iterator();

            while (var2.hasNext()) {
                int itemId = (Integer) var2.next();
                switch (ItemTable.getInstance().getTemplate(itemId).getDefaultAction()) {
                    case summon_soulshot:
                    case summon_spiritshot:
                        owner.disableAutoShot(itemId);
                }
            }
        }

    }

    public int getAttackRange() {
        return 36;
    }

    public boolean getFollowStatus() {
        return this._follow;
    }

    public void setFollowStatus(boolean state) {
        this._follow = state;
        if (this._follow) {
            this.getAI().setIntention(IntentionType.FOLLOW, this.getOwner());
        } else {
            this.getAI().setIntention(IntentionType.IDLE, null);
        }

    }

    public boolean isAutoAttackable(Creature attacker) {
        return this._owner.isAutoAttackable(attacker);
    }

    public int getControlItemId() {
        return 0;
    }

    public Weapon getActiveWeapon() {
        return null;
    }

    public PetInventory getInventory() {
        return null;
    }

    public void store() {
    }

    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    public Weapon getActiveWeaponItem() {
        return null;
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    public Weapon getSecondaryWeaponItem() {
        return null;
    }

    public boolean isInvul() {
        return super.isInvul() || this.getOwner().isSpawnProtected();
    }

    public Party getParty() {
        return this._owner == null ? null : this._owner.getParty();
    }

    public boolean isInParty() {
        return this._owner != null && this._owner.getParty() != null;
    }

    public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove) {
        if (skill != null && !this.isDead()) {
            if (skill.isPassive()) {
                return false;
            } else if (this.isCastingNow()) {
                return false;
            } else {
                this.getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
                WorldObject target = null;
                switch (skill.getTargetType()) {
                    case TARGET_OWNER_PET:
                        target = this.getOwner();
                        break;
                    case TARGET_PARTY:
                    case TARGET_AURA:
                    case TARGET_FRONT_AURA:
                    case TARGET_BEHIND_AURA:
                    case TARGET_AURA_UNDEAD:
                    case TARGET_SELF:
                    case TARGET_CORPSE_ALLY:
                        target = this;
                        break;
                    default:
                        target = skill.getFirstOfTargetList(this);
                }

                if (target == null) {
                    this.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
                    return false;
                } else if (this.isSkillDisabled(skill)) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addString(skill.getName()));
                    return false;
                } else if (this.getCurrentMp() < (double) (this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill))) {
                    this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
                    return false;
                } else if (this.getCurrentHp() <= (double) skill.getHpConsume()) {
                    this.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
                    return false;
                } else {
                    if (skill.isOffensive()) {
                        if (isInsidePeaceZone(this, target)) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
                            return false;
                        }

                        if (this.getOwner() != null && this.getOwner().isInOlympiadMode() && !this.getOwner().isOlympiadStart()) {
                            this.sendPacket(ActionFailed.STATIC_PACKET);
                            return false;
                        }

                        if (target instanceof Door) {
                            if (!target.isAutoAttackable(this.getOwner())) {
                                return false;
                            }
                        } else {
                            if (!target.isAttackable() && this.getOwner() != null && !this.getOwner().getAccessLevel().allowPeaceAttack()) {
                                return false;
                            }

                            if (!target.isAutoAttackable(this) && !forceUse && skill.getTargetType() != SkillTargetType.TARGET_AURA && skill.getTargetType() != SkillTargetType.TARGET_FRONT_AURA && skill.getTargetType() != SkillTargetType.TARGET_BEHIND_AURA && skill.getTargetType() != SkillTargetType.TARGET_AURA_UNDEAD && skill.getTargetType() != SkillTargetType.TARGET_CLAN && skill.getTargetType() != SkillTargetType.TARGET_ALLY && skill.getTargetType() != SkillTargetType.TARGET_PARTY && skill.getTargetType() != SkillTargetType.TARGET_SELF) {
                                return false;
                            }
                        }
                    }

                    this.getAI().setIntention(IntentionType.CAST, skill, target);
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    public void setIsImmobilized(boolean value) {
        super.setIsImmobilized(value);
        if (value) {
            this._previousFollowStatus = this.getFollowStatus();
            if (this._previousFollowStatus) {
                this.setFollowStatus(false);
            }
        } else {
            this.setFollowStatus(this._previousFollowStatus);
        }

    }

    public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
        if (!miss && this.getOwner() != null) {
            if (target.getObjectId() != this.getOwner().getObjectId()) {
                if (pcrit || mcrit) {
                    if (this instanceof Servitor) {
                        this.sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
                    } else {
                        this.sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
                    }
                }

                SystemMessage sm;
                if (target.isInvul()) {
                    if (target.isParalyzed()) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_PETRIFIED);
                    } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
                    }
                } else {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE).addNumber(damage);
                }

                this.sendPacket(sm);
                if (this.getOwner().isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == this.getOwner().getOlympiadGameId()) {
                    OlympiadGameManager.getInstance().notifyCompetitorDamage(this.getOwner(), damage);
                }
            }

        }
    }

    public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill) {
        super.reduceCurrentHp(damage, attacker, skill);
    }

    public void doCast(L2Skill skill) {
        Player actingPlayer = this.getActingPlayer();
        if (!actingPlayer.checkPvpSkill(this.getTarget(), skill) && !actingPlayer.getAccessLevel().allowPeaceAttack()) {
            actingPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            super.doCast(skill);
        }
    }

    public boolean isOutOfControl() {
        return super.isOutOfControl() || this.isBetrayed();
    }

    public boolean isInCombat() {
        return this.getOwner() != null && this.getOwner().isInCombat();
    }

    public final boolean isAttackingNow() {
        return this.isInCombat();
    }

    public Player getActingPlayer() {
        return this.getOwner();
    }

    public String toString() {
        String var10000 = super.toString();
        return var10000 + "(" + this.getNpcId() + ") Owner: " + this.getOwner();
    }

    public void sendPacket(L2GameServerPacket mov) {
        if (this.getOwner() != null) {
            this.getOwner().sendPacket(mov);
        }

    }

    public void sendPacket(SystemMessageId id) {
        if (this.getOwner() != null) {
            this.getOwner().sendPacket(id);
        }

    }

    public int getWeapon() {
        return 0;
    }

    public int getArmor() {
        return 0;
    }

    public void updateAndBroadcastStatusAndInfos(int val) {
        this.sendPacket(new PetInfo(this, val));
        this.updateEffectIcons(true);
        this.updateAndBroadcastStatus(val);
    }

    public void sendPetInfosToOwner() {
        this.sendPacket(new PetInfo(this, 2));
        this.updateEffectIcons(true);
    }

    public void updateAndBroadcastStatus(int val) {
        this.sendPacket(new PetStatusUpdate(this));
        if (this.isVisible()) {
            Iterator var2 = this.getKnownType(Player.class).iterator();

            while (var2.hasNext()) {
                Player player = (Player) var2.next();
                if (player != this.getOwner()) {
                    player.sendPacket(new SummonInfo(this, player, val));
                }
            }
        }

    }

    public void onSpawn() {
        super.onSpawn();
        if (Config.SHOW_SUMMON_CREST) {
            this.sendPacket(new SummonInfo(this, this.getOwner(), 0));
        }

        this.sendPacket(new RelationChanged(this, this.getOwner().getRelation(this.getOwner()), false));
        this.broadcastRelationsChanges();
    }

    public void broadcastRelationsChanges() {
        Iterator var1 = this.getOwner().getKnownType(Player.class).iterator();

        while (var1.hasNext()) {
            Player player = (Player) var1.next();
            player.sendPacket(new RelationChanged(this, this.getOwner().getRelation(player), this.isAutoAttackable(player)));
        }

    }

    public void sendInfo(Player activeChar) {
        if (activeChar == this.getOwner()) {
            activeChar.sendPacket(new PetInfo(this, 0));
            this.updateEffectIcons(true);
            if (this instanceof Pet) {
                activeChar.sendPacket(new PetItemList((Pet) this));
            }
        } else {
            activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
        }

    }

    public boolean isChargedShot(ShotType type) {
        return (this._shotsMask & type.getMask()) == type.getMask();
    }

    public void setChargedShot(ShotType type, boolean charged) {
        if (charged) {
            this._shotsMask |= type.getMask();
        } else {
            this._shotsMask &= ~type.getMask();
        }

    }

    public void rechargeShots(boolean physical, boolean magic) {
        if (this.getOwner().getAutoSoulShot() != null && !this.getOwner().getAutoSoulShot().isEmpty()) {
            Iterator var3 = this.getOwner().getAutoSoulShot().iterator();

            while (var3.hasNext()) {
                int itemId = (Integer) var3.next();
                ItemInstance item = this.getOwner().getInventory().getItemByItemId(itemId);
                if (item != null) {
                    IItemHandler handler;
                    if (magic && item.getItem().getDefaultAction() == ActionType.summon_spiritshot) {
                        handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                        if (handler != null) {
                            handler.useItem(this.getOwner(), item, false);
                        }
                    }

                    if (physical && item.getItem().getDefaultAction() == ActionType.summon_soulshot) {
                        handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                        if (handler != null) {
                            handler.useItem(this.getOwner(), item, false);
                        }
                    }
                } else {
                    this.getOwner().removeAutoSoulShot(itemId);
                }
            }

        }
    }

    public int getSkillLevel(int skillId) {
        Iterator var2 = this.getTemplate().getSkills().values().iterator();

        while (var2.hasNext()) {
            List<L2Skill> list = (List) var2.next();
            Iterator var4 = list.iterator();

            while (var4.hasNext()) {
                L2Skill skill = (L2Skill) var4.next();
                if (skill.getId() == skillId) {
                    return skill.getLevel();
                }
            }
        }

        return 0;
    }

    public L2Skill getSkill(int skillId) {
        Iterator var2 = this.getTemplate().getSkills().values().iterator();

        while (var2.hasNext()) {
            List<L2Skill> list = (List) var2.next();
            Iterator var4 = list.iterator();

            while (var4.hasNext()) {
                L2Skill skill = (L2Skill) var4.next();
                if (skill.getId() == skillId) {
                    return skill;
                }
            }
        }

        return null;
    }
}