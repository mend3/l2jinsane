package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2j.gameserver.model.zone.type.TownZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public abstract class AbstractOlympiadGame {
    protected static final CLogger LOGGER = new CLogger(AbstractOlympiadGame.class.getName());

    protected static final String POINTS = "olympiad_points";

    protected static final String COMP_DONE = "competitions_done";

    protected static final String COMP_WON = "competitions_won";

    protected static final String COMP_LOST = "competitions_lost";

    protected static final String COMP_DRAWN = "competitions_drawn";

    protected final int _stadiumId;

    protected long _startTime = 0L;

    protected boolean _aborted = false;

    protected AbstractOlympiadGame(int id) {
        this._stadiumId = id;
    }

    protected static SystemMessage checkDefection(Player player) {
        if (player == null || !player.isOnline())
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
        if (player.getClient() == null || player.getClient().isDetached())
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
        if (player.isInObserverMode())
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
        if (player.isDead()) {
            player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
        }
        if (player.isSubClassActive()) {
            player.sendPacket(SystemMessageId.SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
        }
        if (player.isCursedWeaponEquipped()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(player.getCursedWeaponEquippedId()));
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
        }
        if (player.getInventoryLimit() * 0.8D <= player.getInventory().getSize()) {
            player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
        }
        return null;
    }

    protected static boolean portPlayerToArena(Participant par, Location loc, int id) {
        Player player = par.getPlayer();
        if (player == null || !player.isOnline())
            return false;
        player.getSavedLocation().set(player.getPosition());
        player.setTarget(null);
        player.setOlympiadGameId(id);
        player.setOlympiadMode(true);
        player.setOlympiadStart(false);
        player.setOlympiadSide(par.getSide());
        player.teleportTo(loc, 0);
        player.sendPacket(new ExOlympiadMode(par.getSide()));
        return true;
    }

    protected static void removals(Player player, boolean removeParty) {
        if (player == null)
            return;
        player.stopAllEffectsExceptThoseThatLastThroughDeath();
        if (player.getClan() != null)
            for (L2Skill skill : player.getClan().getClanSkills().values())
                player.removeSkill(skill.getId(), false);
        player.abortAttack();
        player.abortCast();
        player.getAppearance().setVisible();
        if (player.isHero())
            for (L2Skill skill : SkillTable.getHeroSkills())
                player.removeSkill(skill.getId(), false);
        healPlayer(player);
        if (player.isMounted()) {
            player.dismount();
        } else {
            Summon summon = player.getSummon();
            if (summon instanceof net.sf.l2j.gameserver.model.actor.instance.Pet) {
                summon.unSummon(player);
            } else if (summon != null) {
                summon.stopAllEffectsExceptThoseThatLastThroughDeath();
                summon.abortAttack();
                summon.abortCast();
            }
        }
        player.stopCubicsByOthers();
        if (removeParty) {
            Party party = player.getParty();
            if (party != null)
                party.removePartyMember(player, MessageType.EXPELLED);
        }
        player.checkItemRestriction();
        player.disableAutoShotsAll();
        ItemInstance item = player.getActiveWeaponInstance();
        if (item != null)
            item.unChargeAllShots();
        player.sendSkillList();
    }

    protected static void buffPlayer(Player player) {
        L2Skill skill = SkillTable.getInstance().getInfo(1204, 2);
        if (skill != null) {
            skill.getEffects(player, player);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(1204));
        }
        if (!player.isMageClass()) {
            skill = SkillTable.getInstance().getInfo(1086, 1);
            if (skill != null) {
                skill.getEffects(player, player);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(1086));
            }
        }
    }

    protected static void healPlayer(Player player) {
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
    }

    protected static void cleanEffects(Player player) {
        player.setOlympiadStart(false);
        player.setTarget(null);
        player.abortAttack();
        player.abortCast();
        player.getAI().setIntention(IntentionType.IDLE);
        if (player.isDead())
            player.setIsDead(false);
        Summon summon = player.getSummon();
        if (summon != null && !summon.isDead()) {
            summon.setTarget(null);
            summon.abortAttack();
            summon.abortCast();
            summon.getAI().setIntention(IntentionType.IDLE);
        }
        healPlayer(player);
        player.getStatus().startHpMpRegeneration();
    }

    protected static void playerStatusBack(Player player) {
        player.setOlympiadMode(false);
        player.setOlympiadStart(false);
        player.setOlympiadSide(-1);
        player.setOlympiadGameId(-1);
        player.sendPacket(new ExOlympiadMode(0));
        player.stopAllEffectsExceptThoseThatLastThroughDeath();
        player.clearCharges();
        Summon summon = player.getSummon();
        if (summon != null && !summon.isDead())
            summon.stopAllEffectsExceptThoseThatLastThroughDeath();
        if (player.getClan() != null) {
            player.getClan().addSkillEffects(player);
            healPlayer(player);
        }
        if (player.isHero())
            for (L2Skill skill : SkillTable.getHeroSkills())
                player.addSkill(skill, false);
        player.sendSkillList();
    }

    protected static void portPlayerBack(Player player) {
        if (player == null)
            return;
        Location loc = player.getSavedLocation();
        if (loc.equals(Location.DUMMY_LOC))
            return;
        TownZone town = MapRegionData.getTown(loc.getX(), loc.getY(), loc.getZ());
        if (town != null)
            loc = town.getRandomLoc();
        player.teleportTo(loc, 0);
        player.getSavedLocation().clean();
    }

    public static void rewardParticipant(Player player, IntIntHolder[] reward) {
        if (player == null || !player.isOnline() || reward == null)
            return;
        InventoryUpdate iu = new InventoryUpdate();
        for (IntIntHolder it : reward) {
            ItemInstance item = player.getInventory().addItem("Olympiad", it.getId(), it.getValue(), player, null);
            if (item != null) {
                iu.addModifiedItem(item);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(it.getId()).addNumber(it.getValue()));
            }
        }
        player.sendPacket(iu);
    }

    public abstract OlympiadType getType();

    public abstract String[] getPlayerNames();

    public abstract boolean containsParticipant(int paramInt);

    public abstract void sendOlympiadInfo(Creature paramCreature);

    public abstract void broadcastOlympiadInfo(OlympiadStadiumZone paramOlympiadStadiumZone);

    protected abstract void broadcastPacket(L2GameServerPacket paramL2GameServerPacket);

    protected abstract boolean checkDefection();

    protected abstract void removals();

    protected abstract void buffPlayers();

    protected abstract void healPlayers();

    protected abstract boolean portPlayersToArena(List<Location> paramList);

    protected abstract void cleanEffects();

    protected abstract void portPlayersBack();

    protected abstract void playersStatusBack();

    protected abstract void clearPlayers();

    protected abstract void handleDisconnect(Player paramPlayer);

    protected abstract void resetDamage();

    protected abstract void addDamage(Player paramPlayer, int paramInt);

    protected abstract boolean checkBattleStatus();

    protected abstract boolean haveWinner();

    protected abstract void validateWinner(OlympiadStadiumZone paramOlympiadStadiumZone);

    protected abstract int getDivider();

    protected abstract IntIntHolder[] getReward();

    public final boolean isAborted() {
        return this._aborted;
    }

    public final int getStadiumId() {
        return this._stadiumId;
    }

    protected boolean makeCompetitionStart() {
        this._startTime = System.currentTimeMillis();
        return !this._aborted;
    }

    protected final void addPointsToParticipant(Participant par, int points) {
        par.updateStat("olympiad_points", points);
        broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS).addString(par.getName()).addNumber(points));
    }

    protected final void removePointsFromParticipant(Participant par, int points) {
        par.updateStat("olympiad_points", -points);
        broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS).addString(par.getName()).addNumber(points));
    }
}
