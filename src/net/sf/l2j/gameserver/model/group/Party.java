/**/
package net.sf.l2j.gameserver.model.group;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.DimensionalRiftManager;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.enums.LootRule;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.rift.DimensionalRift;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class Party extends AbstractGroup {
    private static final double[] BONUS_EXP_SP = new double[]{(double) 1.0F, (double) 1.0F, 1.3, 1.39, (double) 1.5F, 1.54, 1.58, 1.63, 1.67, 1.71};
    private static final int PARTY_POSITION_BROADCAST = 12000;
    private final List<Player> _members = new CopyOnWriteArrayList<>();
    private final LootRule _lootRule;
    protected PartyMemberPosition _positionPacket;
    private boolean _pendingInvitation;
    private long _pendingInviteTimeout;
    private int _itemLastLoot;
    private CommandChannel _commandChannel;
    private DimensionalRift _rift;
    private Future<?> _positionBroadcastTask;

    public Party(Player leader, Player target, LootRule lootRule) {
        super(leader);
        this._members.add(leader);
        this._members.add(target);
        leader.setParty(this);
        target.setParty(this);
        this._lootRule = lootRule;
        this.recalculateLevel();
        target.sendPacket(new PartySmallWindowAll(target, this));
        leader.sendPacket(new PartySmallWindowAdd(target, this));
        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(leader));
        leader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(target));

        for (Player member : this._members) {
            member.updateEffectIcons(true);
            member.broadcastUserInfo();
        }

        this._positionBroadcastTask = ThreadPool.scheduleAtFixedRate(new PositionBroadcast(), 6000L, 12000L);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Party)) {
            return false;
        } else {
            return obj == this ? true : this.isLeader(((Party) obj).getLeader());
        }
    }

    public final List<Player> getMembers() {
        return this._members;
    }

    public int getMembersCount() {
        return this._members.size();
    }

    public boolean containsPlayer(WorldObject player) {
        return this._members.contains(player);
    }

    public void broadcastPacket(L2GameServerPacket packet) {
        for (Player member : this._members) {
            member.sendPacket(packet);
        }

    }

    public void broadcastCreatureSay(CreatureSay msg, Player broadcaster) {
        for (Player member : this._members) {
            if (!BlockList.isBlocked(member, broadcaster)) {
                member.sendPacket(msg);
            }
        }

    }

    public void recalculateLevel() {
        int newLevel = 0;

        for (Player member : this._members) {
            if (member.getLevel() > newLevel) {
                newLevel = member.getLevel();
            }
        }

        this.setLevel(newLevel);
    }

    public void disband() {
        DimensionalRiftManager.getInstance().onPartyEdit(this);
        DuelManager.getInstance().onPartyEdit(this.getLeader());
        if (this._commandChannel != null) {
            this.broadcastPacket(ExCloseMPCC.STATIC_PACKET);
            if (this._commandChannel.isLeader(this.getLeader())) {
                this._commandChannel.disband();
            } else {
                this._commandChannel.removeParty(this);
            }
        }

        for (Player member : this._members) {
            member.setParty(null);
            member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
            if (member.isFestivalParticipant()) {
                FestivalOfDarknessManager.getInstance().updateParticipants(member, this);
            }

            if (member.getFusionSkill() != null) {
                member.abortCast();
            }

            for (Creature character : member.getKnownType(Creature.class)) {
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == member) {
                    character.abortCast();
                }
            }

            member.sendPacket(SystemMessageId.PARTY_DISPERSED);
        }

        this._members.clear();
        if (this._positionBroadcastTask != null) {
            this._positionBroadcastTask.cancel(false);
            this._positionBroadcastTask = null;
        }

    }

    public boolean getPendingInvitation() {
        return this._pendingInvitation;
    }

    public void setPendingInvitation(boolean val) {
        this._pendingInvitation = val;
        this._pendingInviteTimeout = System.currentTimeMillis() + 15000L;
    }

    public boolean isInvitationRequestExpired() {
        return this._pendingInviteTimeout <= System.currentTimeMillis();
    }

    private Player getRandomMember(int itemId, Creature target) {
        List<Player> availableMembers = new ArrayList<>();

        for (Player member : this._members) {
            if (member.getInventory().validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true)) {
                availableMembers.add(member);
            }
        }

        return availableMembers.isEmpty() ? null : Rnd.get(availableMembers);
    }

    private Player getNextLooter(int itemId, Creature target) {
        for (int i = 0; i < this.getMembersCount(); ++i) {
            if (++this._itemLastLoot >= this.getMembersCount()) {
                this._itemLastLoot = 0;
            }

            Player member = this._members.get(this._itemLastLoot);
            if (member.getInventory().validateCapacityByItemId(itemId) && MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true)) {
                return member;
            }
        }

        return null;
    }

    private Player getActualLooter(Player player, int itemId, boolean spoil, Creature target) {
        Player looter = player;
        switch (this._lootRule) {
            case ITEM_RANDOM:
                if (!spoil) {
                    looter = this.getRandomMember(itemId, target);
                }
                break;
            case ITEM_RANDOM_SPOIL:
                looter = this.getRandomMember(itemId, target);
                break;
            case ITEM_ORDER:
                if (!spoil) {
                    looter = this.getNextLooter(itemId, target);
                }
                break;
            case ITEM_ORDER_SPOIL:
                looter = this.getNextLooter(itemId, target);
        }

        return looter == null ? player : looter;
    }

    public void broadcastNewLeaderStatus() {
        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addCharName(this.getLeader());

        for (Player member : this._members) {
            member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
            member.sendPacket(new PartySmallWindowAll(member, this));
            member.broadcastUserInfo();
            member.sendPacket(sm);
        }

    }

    public void broadcastToPartyMembers(Player player, L2GameServerPacket msg) {
        for (Player member : this._members) {
            if (member != null && !member.equals(player)) {
                member.sendPacket(msg);
            }
        }

    }

    public void addPartyMember(Player player) {
        if (player != null && !this._members.contains(player)) {
            player.sendPacket(new PartySmallWindowAll(player, this));
            this.broadcastPacket(new PartySmallWindowAdd(player, this));
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addCharName(this.getLeader()));
            this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addCharName(player));
            DimensionalRiftManager.getInstance().onPartyEdit(this);
            DuelManager.getInstance().onPartyEdit(this.getLeader());
            this._members.add(player);
            player.setParty(this);
            if (player.getLevel() > this.getLevel()) {
                this.setLevel(player.getLevel());
            }

            for (Player member : this._members) {
                member.updateEffectIcons(true);
                member.broadcastUserInfo();
            }

            if (this._commandChannel != null) {
                player.sendPacket(ExOpenMPCC.STATIC_PACKET);
            }

        }
    }

    public void removePartyMember(String name, MessageType type) {
        this.removePartyMember(this.getPlayerByName(name), type);
    }

    public void removePartyMember(Player player, MessageType type) {
        if (player != null && this._members.contains(player)) {
            if (this._members.size() != 2 && !this.isLeader(player)) {
                DimensionalRiftManager.getInstance().onPartyEdit(this);
                DuelManager.getInstance().onPartyEdit(this.getLeader());
                this._members.remove(player);
                this.recalculateLevel();
                if (player.isFestivalParticipant()) {
                    FestivalOfDarknessManager.getInstance().updateParticipants(player, this);
                }

                if (player.getFusionSkill() != null) {
                    player.abortCast();
                }

                for (Creature character : player.getKnownType(Creature.class)) {
                    if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == player) {
                        character.abortCast();
                    }
                }

                if (type == MessageType.EXPELLED) {
                    player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY);
                    this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_EXPELLED_FROM_PARTY).addCharName(player));
                } else if (type == MessageType.LEFT || type == MessageType.DISCONNECTED) {
                    player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
                    this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY).addCharName(player));
                }

                player.setParty(null);
                player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
                this.broadcastPacket(new PartySmallWindowDelete(player));
                if (this._commandChannel != null) {
                    player.sendPacket(ExCloseMPCC.STATIC_PACKET);
                }
            } else {
                this.disband();
            }

        }
    }

    public void changePartyLeader(String name) {
        Player player = this.getPlayerByName(name);
        if (player != null && !player.isInDuel()) {
            if (!this._members.contains(player)) {
                player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
            } else if (this.isLeader(player)) {
                player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
            } else {
                if (this._commandChannel != null && this._commandChannel.isLeader(this.getLeader())) {
                    this._commandChannel.setLeader(player);
                    this._commandChannel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1).addCharName(player));
                }

                this.setLeader(player);
                this.broadcastNewLeaderStatus();
                if (player.isInPartyMatchRoom()) {
                    PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
                    room.changeLeader(player);
                }

            }
        }
    }

    private Player getPlayerByName(String name) {
        for (Player member : this._members) {
            if (member.getName().equalsIgnoreCase(name)) {
                return member;
            }
        }

        return null;
    }

    public void distributeItem(Player player, ItemInstance item) {
        if (item.getItemId() == 57) {
            this.distributeAdena(player, item.getCount(), player);
            item.destroyMe("Party", player, null);
        } else {
            Player target = this.getActualLooter(player, item.getItemId(), false, player);
            if (target != null) {
                if (item.getCount() > 1) {
                    this.broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2).addCharName(target).addItemName(item).addItemNumber(item.getCount()));
                } else if (item.getEnchantLevel() > 0) {
                    this.broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2_S3).addCharName(target).addNumber(item.getEnchantLevel()).addItemName(item));
                } else {
                    this.broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2).addCharName(target).addItemName(item));
                }

                target.addItem("Party", item, player, true);
            }
        }
    }

    public void distributeItem(Player player, IntIntHolder item, boolean spoil, Attackable target) {
        if (item != null) {
            if (item.getId() == 57) {
                this.distributeAdena(player, item.getValue(), target);
            } else {
                Player looter = this.getActualLooter(player, item.getId(), spoil, target);
                if (looter != null) {
                    looter.addItem(spoil ? "Sweep" : "Party", item.getId(), item.getValue(), player, true);
                    SystemMessage msg;
                    if (item.getValue() > 1) {
                        msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S3_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2);
                        msg.addCharName(looter);
                        msg.addItemName(item.getId());
                        msg.addItemNumber(item.getValue());
                    } else {
                        msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2);
                        msg.addCharName(looter);
                        msg.addItemName(item.getId());
                    }

                    this.broadcastToPartyMembers(looter, msg);
                }
            }
        }
    }

    public void distributeAdena(Player player, int adena, Creature target) {
        List<Player> toReward = new ArrayList<>(this._members.size());

        for (Player member : this._members) {
            if (MathUtil.checkIfInRange(Config.PARTY_RANGE, target, member, true) && member.getAdena() != Integer.MAX_VALUE) {
                toReward.add(member);
            }
        }

        if (!toReward.isEmpty()) {
            int count = adena / toReward.size();

            for (Player member : toReward) {
                member.addAdena("Party", count, player, true);
            }

        }
    }

    public void distributeXpAndSp(long xpReward, int spReward, List<Player> rewardedMembers, int topLvl, Map<Creature, RewardInfo> rewards) {
        List<Player> validMembers = new ArrayList<>();
        if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level")) {
            for (Player member : rewardedMembers) {
                if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL) {
                    validMembers.add(member);
                }
            }
        } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) {
            int sqLevelSum = 0;

            for (Player member : rewardedMembers) {
                sqLevelSum += member.getLevel() * member.getLevel();
            }

            for (Player member : rewardedMembers) {
                int sqLevel = member.getLevel() * member.getLevel();
                if ((double) (sqLevel * 100) >= (double) sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT) {
                    validMembers.add(member);
                }
            }
        } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) {
            int sqLevelSum = 0;

            for (Player member : rewardedMembers) {
                sqLevelSum += member.getLevel() * member.getLevel();
            }

            int partySize = MathUtil.limit(rewardedMembers.size(), 1, 9);

            for (Player member : rewardedMembers) {
                int sqLevel = member.getLevel() * member.getLevel();
                if ((double) sqLevel >= (double) sqLevelSum * ((double) 1.0F - (double) 1.0F / ((double) 1.0F + BONUS_EXP_SP[partySize] - BONUS_EXP_SP[partySize - 1]))) {
                    validMembers.add(member);
                }
            }
        }

        double partyRate = BONUS_EXP_SP[Math.min(validMembers.size(), 9)];
        xpReward = (long) ((double) xpReward * partyRate * Config.RATE_PARTY_XP);
        spReward = (int) ((double) spReward * partyRate * Config.RATE_PARTY_SP);
        int sqLevelSum = 0;

        for (Player member : validMembers) {
            sqLevelSum += member.getLevel() * member.getLevel();
        }

        for (Player member : rewardedMembers) {
            if (!member.isDead()) {
                if (validMembers.contains(member)) {
                    float penalty = member.hasServitor() ? ((Servitor) member.getSummon()).getExpPenalty() : 0.0F;
                    double sqLevel = member.getLevel() * member.getLevel();
                    double preCalculation = sqLevel / (double) sqLevelSum * (double) (1.0F - penalty);
                    long xp = Math.round((double) xpReward * preCalculation);
                    int sp = (int) ((double) spReward * preCalculation);
                    member.updateKarmaLoss(xp);
                    member.addExpAndSp(xp, sp, rewards);
                } else {
                    member.addExpAndSp(0L, 0);
                }
            }
        }

    }

    public LootRule getLootRule() {
        return this._lootRule;
    }

    public boolean isInCommandChannel() {
        return this._commandChannel != null;
    }

    public CommandChannel getCommandChannel() {
        return this._commandChannel;
    }

    public void setCommandChannel(CommandChannel channel) {
        this._commandChannel = channel;
    }

    public boolean isInDimensionalRift() {
        return this._rift != null;
    }

    public DimensionalRift getDimensionalRift() {
        return this._rift;
    }

    public void setDimensionalRift(DimensionalRift rift) {
        this._rift = rift;
    }

    public boolean wipedOut() {
        for (Player member : this._members) {
            if (!member.isDead()) {
                return false;
            }
        }

        return true;
    }

    protected class PositionBroadcast implements Runnable {
        public void run() {
            if (Party.this._positionPacket == null) {
                Party.this._positionPacket = new PartyMemberPosition(Party.this);
            } else {
                Party.this._positionPacket.reuse(Party.this);
            }

            Party.this.broadcastPacket(Party.this._positionPacket);
        }
    }
}
