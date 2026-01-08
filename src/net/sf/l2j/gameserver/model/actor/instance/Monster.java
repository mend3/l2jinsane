package net.sf.l2j.gameserver.model.actor.instance;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.data.xml.HerbDropData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.npc.AbsorbInfo;
import net.sf.l2j.gameserver.model.actor.npc.AggroInfo;
import net.sf.l2j.gameserver.model.actor.npc.MinionList;
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Monster extends Attackable {
    private final Map<Integer, AbsorbInfo> _absorbersList = new ConcurrentHashMap<>();
    private final List<IntIntHolder> _sweepItems = new ArrayList<>();
    private final List<IntIntHolder> _harvestItems = new ArrayList<>();
    private Monster _master;
    private MinionList _minionList;
    private boolean _isRaid;
    private boolean _isMinion;
    private int _spoilerId;
    private Seed _seed;
    private int _seederObjId;
    private boolean _overhit;
    private double _overhitDamage;
    private Creature _overhitAttacker;
    private CommandChannel _firstCommandChannelAttacked;
    private CommandChannelTimer _commandChannelTimer;
    private long _commandChannelLastAttack;

    public Monster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static IntIntHolder calculateCategorizedHerbItem(DropCategory categoryDrops, int levelModifier) {
        if (categoryDrops == null) {
            return null;
        } else {
            int categoryDropChance = categoryDrops.getCategoryChance();
            switch (categoryDrops.getCategoryType()) {
                case 1 -> categoryDropChance = (int) ((double) categoryDropChance * Config.RATE_DROP_HP_HERBS);
                case 2 -> categoryDropChance = (int) ((double) categoryDropChance * Config.RATE_DROP_MP_HERBS);
                case 3 -> categoryDropChance = (int) ((double) categoryDropChance * Config.RATE_DROP_SPECIAL_HERBS);
                default -> categoryDropChance = (int) ((double) categoryDropChance * Config.RATE_DROP_COMMON_HERBS);
            }

            if (Config.DEEPBLUE_DROP_RULES) {
                int deepBlueDrop = levelModifier > 0 ? 3 : 1;
                categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
            }

            if (Rnd.get(1000000) < Math.max(1, categoryDropChance)) {
                DropData drop = categoryDrops.dropOne(false);
                if (drop == null) {
                    return null;
                }

                double dropChance = drop.getChance();
                switch (categoryDrops.getCategoryType()) {
                    case 1 -> dropChance *= Config.RATE_DROP_HP_HERBS;
                    case 2 -> dropChance *= Config.RATE_DROP_MP_HERBS;
                    case 3 -> dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
                    default -> dropChance *= Config.RATE_DROP_COMMON_HERBS;
                }

                if (dropChance < (double) 1000000.0F) {
                    dropChance = 1000000.0F;
                }

                int min = drop.getMinDrop();
                int max = drop.getMaxDrop();
                int itemCount = 0;

                for (int random = Rnd.get(1000000); (double) random < dropChance; dropChance -= 1000000.0F) {
                    if (min < max) {
                        itemCount += Rnd.get(min, max);
                    } else if (min == max) {
                        itemCount += min;
                    } else {
                        ++itemCount;
                    }
                }

                if (itemCount > 0) {
                    return new IntIntHolder(drop.getItemId(), itemCount);
                }
            }

            return null;
        }
    }

    public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (this.isRaidBoss() && attacker != null) {
            Party party = attacker.getParty();
            if (party != null) {
                CommandChannel cc = party.getCommandChannel();
                if (cc != null && cc.meetRaidWarCondition(this)) {
                    if (this._firstCommandChannelAttacked == null) {
                        synchronized (this) {
                            if (this._firstCommandChannelAttacked == null) {
                                this._firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
                                if (this._firstCommandChannelAttacked != null) {
                                    this._commandChannelTimer = new CommandChannelTimer(this);
                                    this._commandChannelLastAttack = System.currentTimeMillis();
                                    ThreadPool.schedule(this._commandChannelTimer, 10000L);
                                }
                            }
                        }
                    } else if (attacker.getParty().getCommandChannel().equals(this._firstCommandChannelAttacked)) {
                        this._commandChannelLastAttack = System.currentTimeMillis();
                    }
                }
            }
        }

        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
    }

    protected void calculateRewards(Creature lastAttacker) {
        if (!this.getAggroList().isEmpty()) {
            if (!EngineModsManager.onNpcExpSp(this, lastAttacker)) {
                Map<Creature, RewardInfo> rewards = new ConcurrentHashMap<>();
                Player maxDealer = null;
                int maxDamage = 0;
                long totalDamage = 0L;

                for (AggroInfo info : this.getAggroList().values()) {
                    if (info.getAttacker() instanceof Playable) {
                        Playable attacker = (Playable) info.getAttacker();
                        int damage = info.getDamage();
                        if (damage > 1 && MathUtil.checkIfInRange(Config.PARTY_RANGE, this, attacker, true)) {
                            Player attackerPlayer = attacker.getActingPlayer();
                            totalDamage += damage;
                            RewardInfo reward = rewards.get(attacker);
                            if (reward == null) {
                                reward = new RewardInfo(attacker);
                                rewards.put(attacker, reward);
                            }

                            reward.addDamage(damage);
                            if (attacker instanceof Summon) {
                                reward = rewards.get(attackerPlayer);
                                if (reward == null) {
                                    reward = new RewardInfo(attackerPlayer);
                                    rewards.put(attackerPlayer, reward);
                                }

                                reward.addDamage(damage);
                            }

                            if (reward.getDamage() > maxDamage) {
                                maxDealer = attackerPlayer;
                                maxDamage = reward.getDamage();
                            }
                        }
                    }
                }

                if (this.isRaidBoss() && this.getFirstCommandChannelAttacked() != null) {
                    maxDealer = this.getFirstCommandChannelAttacked().getLeader();
                }

                this.doItemDrop(this.getTemplate(), maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker);

                for (RewardInfo reward : rewards.values()) {
                    if (!(reward.getAttacker() instanceof Summon)) {
                        Player attacker = reward.getAttacker().getActingPlayer();
                        int damage = reward.getDamage();
                        Party attackerParty = attacker.getParty();
                        float penalty = attacker.hasServitor() ? ((Servitor) attacker.getSummon()).getExpPenalty() : 0.0F;
                        if (attackerParty == null) {
                            if (!attacker.isDead() && attacker.getKnownType(Attackable.class).contains(this)) {
                                int levelDiff = attacker.getLevel() - this.getLevel();
                                int[] expSp = this.calculateExpAndSp(levelDiff, damage, totalDamage);
                                long exp = expSp[0];
                                int sp = expSp[1];
                                exp = (long) ((float) exp * (1.0F - penalty));
                                if (this.isOverhit() && this._overhitAttacker != null && this._overhitAttacker.getActingPlayer() != null && attacker == this._overhitAttacker.getActingPlayer()) {
                                    attacker.sendPacket(SystemMessageId.OVER_HIT);
                                    exp += this.calculateOverhitExp(exp);
                                }

                                attacker.updateKarmaLoss(exp);
                                attacker.addExpAndSp(exp, sp, rewards);
                            }
                        } else {
                            int partyDmg = 0;
                            float partyMul = 1.0F;
                            int partyLvl = 0;
                            List<Player> rewardedMembers = new ArrayList<>();
                            List<Player> groupMembers = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();
                            Map<Creature, RewardInfo> playersWithPets = new HashMap<>();

                            for (Player partyPlayer : groupMembers) {
                                if (partyPlayer != null && !partyPlayer.isDead()) {
                                    RewardInfo reward2 = rewards.get(partyPlayer);
                                    if (reward2 != null) {
                                        if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true)) {
                                            partyDmg += reward2.getDamage();
                                            rewardedMembers.add(partyPlayer);
                                            if (partyPlayer.getLevel() > partyLvl) {
                                                partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
                                            }
                                        }

                                        rewards.remove(partyPlayer);
                                        playersWithPets.put(partyPlayer, reward2);
                                        if (partyPlayer.hasPet() && rewards.containsKey(partyPlayer.getSummon())) {
                                            playersWithPets.put(partyPlayer.getSummon(), rewards.get(partyPlayer.getSummon()));
                                        }
                                    } else if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true)) {
                                        rewardedMembers.add(partyPlayer);
                                        if (partyPlayer.getLevel() > partyLvl) {
                                            partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
                                        }
                                    }
                                }
                            }

                            if ((long) partyDmg < totalDamage) {
                                partyMul = (float) partyDmg / (float) totalDamage;
                            }

                            int levelDiff = partyLvl - this.getLevel();
                            int[] expSp = this.calculateExpAndSp(levelDiff, partyDmg, totalDamage);
                            long exp = expSp[0];
                            int sp = expSp[1];
                            exp = (long) ((float) exp * partyMul);
                            sp = (int) ((float) sp * partyMul);
                            if (this.isOverhit() && this._overhitAttacker != null && this._overhitAttacker.getActingPlayer() != null && attacker == this._overhitAttacker.getActingPlayer()) {
                                attacker.sendPacket(SystemMessageId.OVER_HIT);
                                exp += this.calculateOverhitExp(exp);
                            }

                            if (partyDmg > 0) {
                                attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, playersWithPets);
                            }
                        }
                    }
                }

            }
        }
    }

    public boolean isAutoAttackable(Creature attacker) {
        return !(attacker instanceof Monster);
    }

    public boolean isAggressive() {
        return this.getTemplate().getAggroRange() > 0;
    }

    public void onSpawn() {
        if (!this.getTemplate().getMinionData().isEmpty()) {
            this.getMinionList().spawnMinions();
        }

        super.onSpawn();
        this.setSpoilerId(0);
        this._harvestItems.clear();
        this._seed = null;
        this._seederObjId = 0;
        this.overhitEnabled(false);
        this._sweepItems.clear();
        this._absorbersList.clear();
    }

    public void onTeleported() {
        super.onTeleported();
        if (this.hasMinions()) {
            this.getMinionList().onMasterTeleported();
        }

    }

    public void deleteMe() {
        if (this.hasMinions()) {
            this.getMinionList().onMasterDeletion();
        } else if (this._master != null) {
            this._master.getMinionList().onMinionDeletion(this);
        }

        super.deleteMe();
    }

    public Monster getMaster() {
        return this._master;
    }

    public void setMaster(Monster master) {
        this._master = master;
    }

    public boolean isRaidBoss() {
        return this._isRaid && !this._isMinion;
    }

    public void setRaid(boolean isRaid) {
        this._isRaid = isRaid;
    }

    public boolean isRaidRelated() {
        return this._isRaid;
    }

    public boolean isMinion() {
        return this._isMinion;
    }

    public void setMinion(boolean isRaidMinion) {
        this._isRaid = isRaidMinion;
        this._isMinion = true;
    }

    public boolean isSpoiled() {
        return !this._sweepItems.isEmpty();
    }

    public List<IntIntHolder> getSweepItems() {
        return this._sweepItems;
    }

    public List<IntIntHolder> getHarvestItems() {
        return this._harvestItems;
    }

    public void overhitEnabled(boolean status) {
        this._overhit = status;
    }

    public void setOverhitValues(Creature attacker, double damage) {
        double overhitDmg = (this.getCurrentHp() - damage) * (double) -1.0F;
        if (overhitDmg < (double) 0.0F) {
            this.overhitEnabled(false);
            this._overhitDamage = 0.0F;
            this._overhitAttacker = null;
        } else {
            this.overhitEnabled(true);
            this._overhitDamage = overhitDmg;
            this._overhitAttacker = attacker;
        }
    }

    public Creature getOverhitAttacker() {
        return this._overhitAttacker;
    }

    public double getOverhitDamage() {
        return this._overhitDamage;
    }

    public boolean isOverhit() {
        return this._overhit;
    }

    public final int getSpoilerId() {
        return this._spoilerId;
    }

    public final void setSpoilerId(int value) {
        this._spoilerId = value;
    }

    public void addAbsorber(Player user, ItemInstance crystal) {
        AbsorbInfo ai = this._absorbersList.get(user.getObjectId());
        if (ai == null) {
            this._absorbersList.put(user.getObjectId(), new AbsorbInfo(crystal.getObjectId()));
        } else if (!ai.isRegistered()) {
            ai.setItemId(crystal.getObjectId());
        }

    }

    public void registerAbsorber(Player user) {
        AbsorbInfo ai = this._absorbersList.get(user.getObjectId());
        if (ai != null) {
            if (user.getInventory().getItemByObjectId(ai.getItemId()) != null) {
                if (!ai.isRegistered()) {
                    ai.setAbsorbedHpPercent((int) ((double) 100.0F * this.getCurrentHp() / (double) this.getMaxHp()));
                    ai.setRegistered(true);
                }

            }
        }
    }

    public AbsorbInfo getAbsorbInfo(int npcObjectId) {
        return this._absorbersList.get(npcObjectId);
    }

    private int[] calculateExpAndSp(int diff, int damage, long totalDamage) {
        double xp = (double) this.getExpReward() * (double) damage / (double) totalDamage;
        double sp = (double) this.getSpReward() * (double) damage / (double) totalDamage;
        if (diff > 5) {
            double pow = Math.pow(0.8333333333333334, diff - 5);
            xp *= pow;
            sp *= pow;
        }

        if (this.isChampion()) {
            xp *= Config.CHAMPION_REWARDS;
            sp *= Config.CHAMPION_REWARDS;
        }

        if (xp <= (double) 0.0F) {
            xp = 0.0F;
            sp = 0.0F;
        } else if (sp <= (double) 0.0F) {
            sp = 0.0F;
        }

        return new int[]{(int) xp, (int) sp};
    }

    public long calculateOverhitExp(long normalExp) {
        double overhitPercentage = this.getOverhitDamage() * (double) 100.0F / (double) this.getMaxHp();
        if (overhitPercentage > (double) 25.0F) {
            overhitPercentage = 25.0F;
        }

        double overhitExp = overhitPercentage / (double) 100.0F * (double) normalExp;
        return Math.round(overhitExp);
    }

    public boolean hasMinions() {
        return this._minionList != null;
    }

    public MinionList getMinionList() {
        if (this._minionList == null) {
            this._minionList = new MinionList(this);
        }

        return this._minionList;
    }

    public void setSeeded(Seed seed, int objectId) {
        if (this._seed == null) {
            this._seed = seed;
            this._seederObjId = objectId;
        }

    }

    public int getSeederId() {
        return this._seederObjId;
    }

    public Seed getSeed() {
        return this._seed;
    }

    public boolean isSeeded() {
        return this._seed != null;
    }

    public void setSeeded(int objectId) {
        if (this._seed != null && this._seederObjId == objectId) {
            int count = 1;

            for (L2Skill skill : this.getTemplate().getSkills(NpcTemplate.SkillType.PASSIVE)) {
                switch (skill.getId()) {
                    case 4303:
                        count *= 2;
                        break;
                    case 4304:
                        count *= 3;
                        break;
                    case 4305:
                        count *= 4;
                        break;
                    case 4306:
                        count *= 5;
                        break;
                    case 4307:
                        count *= 6;
                        break;
                    case 4308:
                        count *= 7;
                        break;
                    case 4309:
                        count *= 8;
                        break;
                    case 4310:
                        count *= 9;
                }
            }

            int diff = this.getLevel() - this._seed.getLevel() - 5;
            if (diff > 0) {
                count += diff;
            }

            this._harvestItems.add(new IntIntHolder(this._seed.getCropId(), count * Config.RATE_DROP_MANOR));
        }

    }

    public CommandChannelTimer getCommandChannelTimer() {
        return this._commandChannelTimer;
    }

    protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer) {
        this._commandChannelTimer = commandChannelTimer;
    }

    public CommandChannel getFirstCommandChannelAttacked() {
        return this._firstCommandChannelAttacked;
    }

    public void setFirstCommandChannelAttacked(CommandChannel firstCommandChannelAttacked) {
        this._firstCommandChannelAttacked = firstCommandChannelAttacked;
    }

    public long getCommandChannelLastAttack() {
        return this._commandChannelLastAttack;
    }

    public void setCommandChannelLastAttack(long channelLastAttack) {
        this._commandChannelLastAttack = channelLastAttack;
    }

    public void teleToMaster() {
        if (this._master != null) {
            int offset = (int) ((double) 100.0F + this.getCollisionRadius() + this._master.getCollisionRadius());
            int minRadius = (int) (this._master.getCollisionRadius() + (double) 30.0F);
            int newX = Rnd.get(minRadius * 2, offset * 2);
            int newY = Rnd.get(newX, offset * 2);
            newY = (int) Math.sqrt(newY * newY - newX * newX);
            if (newX > offset + minRadius) {
                newX = this._master.getX() + newX - offset;
            } else {
                newX = this._master.getX() - newX + minRadius;
            }

            if (newY > offset + minRadius) {
                newY = this._master.getY() + newY - offset;
            } else {
                newY = this._master.getY() - newY + minRadius;
            }

            this.teleportTo(newX, newY, this._master.getZ(), 0);
        }
    }

    private IntIntHolder calculateRewardItem(DropData drop, int levelModifier, boolean isSweep) {
        double dropChance = drop.getChance();
        if (Config.DEEPBLUE_DROP_RULES) {
            int deepBlueDrop = 1;
            if (levelModifier > 0) {
                deepBlueDrop = 3;
                if (drop.getItemId() == 57) {
                    deepBlueDrop *= this.isRaidBoss() ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
                    if (deepBlueDrop == 0) {
                        deepBlueDrop = 1;
                    }
                }
            }

            dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
        }

        if (drop.getItemId() == 57) {
            dropChance *= Config.RATE_DROP_ADENA;
        } else if (isSweep) {
            dropChance *= Config.RATE_DROP_SPOIL;
        } else {
            dropChance *= this.isRaidBoss() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
        }

        if (this.isChampion()) {
            dropChance *= Config.CHAMPION_REWARDS;
        }

        if (dropChance < (double) 1.0F) {
            dropChance = 1.0F;
        }

        int minCount = drop.getMinDrop();
        int maxCount = drop.getMaxDrop();
        int itemCount = 0;

        for (int random = Rnd.get(1000000); (double) random < dropChance; dropChance -= 1000000.0F) {
            if (minCount < maxCount) {
                itemCount += Rnd.get(minCount, maxCount);
            } else if (minCount == maxCount) {
                itemCount += minCount;
            } else {
                ++itemCount;
            }
        }

        if (this.isChampion() && (drop.getItemId() == 57 || drop.getItemId() >= 6360 && drop.getItemId() <= 6362)) {
            itemCount *= Config.CHAMPION_ADENAS_REWARDS;
        }

        return itemCount > 0 ? new IntIntHolder(drop.getItemId(), itemCount) : null;
    }

    private IntIntHolder calculateCategorizedRewardItem(DropCategory categoryDrops, int levelModifier) {
        if (categoryDrops == null) {
            return null;
        } else {
            int basecategoryDropChance = categoryDrops.getCategoryChance();
            int categoryDropChance = basecategoryDropChance;
            if (Config.DEEPBLUE_DROP_RULES) {
                int deepBlueDrop = levelModifier > 0 ? 3 : 1;
                categoryDropChance = (basecategoryDropChance - basecategoryDropChance * levelModifier / 100) / deepBlueDrop;
            }

            categoryDropChance = (int) ((double) categoryDropChance * (this.isRaidBoss() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS));
            if (this.isChampion()) {
                categoryDropChance *= Config.CHAMPION_REWARDS;
            }

            if (categoryDropChance < 1) {
                categoryDropChance = 1;
            }

            if (Rnd.get(1000000) < categoryDropChance) {
                DropData drop = categoryDrops.dropOne(this.isRaidBoss());
                if (drop == null) {
                    return null;
                }

                double dropChance = drop.getChance();
                if (drop.getItemId() == 57) {
                    dropChance *= Config.RATE_DROP_ADENA;
                } else {
                    dropChance *= this.isRaidBoss() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
                }

                if (this.isChampion()) {
                    dropChance *= Config.CHAMPION_REWARDS;
                }

                if (dropChance < (double) 1000000.0F) {
                    dropChance = 1000000.0F;
                }

                int min = drop.getMinDrop();
                int max = drop.getMaxDrop();
                int itemCount = 0;

                for (int random = Rnd.get(1000000); (double) random < dropChance; dropChance -= 1000000.0F) {
                    if (min < max) {
                        itemCount += Rnd.get(min, max);
                    } else if (min == max) {
                        itemCount += min;
                    } else {
                        ++itemCount;
                    }
                }

                if (this.isChampion() && (drop.getItemId() == 57 || drop.getItemId() >= 6360 && drop.getItemId() <= 6362)) {
                    itemCount *= Config.CHAMPION_ADENAS_REWARDS;
                }

                if (itemCount > 0) {
                    return new IntIntHolder(drop.getItemId(), itemCount);
                }
            }

            return null;
        }
    }

    private int calculateLevelModifierForDrop(Player lastAttacker) {
        if (Config.DEEPBLUE_DROP_RULES) {
            int highestLevel = lastAttacker.getLevel();

            for (Creature atkChar : this.getAttackByList()) {
                if (atkChar.getLevel() > highestLevel) {
                    highestLevel = atkChar.getLevel();
                }
            }

            if (highestLevel - 9 >= this.getLevel()) {
                return (highestLevel - (this.getLevel() + 8)) * 9;
            }
        }

        return 0;
    }

    public void doItemDrop(NpcTemplate npcTemplate, Creature mainDamageDealer) {
        if (mainDamageDealer != null) {
            Player player = mainDamageDealer.getActingPlayer();
            if (player != null) {
                if (!EngineModsManager.onNpcDrop(this, mainDamageDealer)) {
                    int levelModifier = this.calculateLevelModifierForDrop(player);
                    CursedWeaponManager.getInstance().checkDrop(this, player);

                    for (DropCategory cat : npcTemplate.getDropData()) {
                        IntIntHolder item = null;
                        if (cat.isSweep()) {
                            if (this.getSpoilerId() != 0) {
                                for (DropData drop : cat.getAllDrops()) {
                                    item = this.calculateRewardItem(drop, levelModifier, true);
                                    if (item != null && !player.ignoredDropContain(item.getId())) {
                                        this._sweepItems.add(item);
                                    }
                                }
                            }
                        } else {
                            if (this.isSeeded()) {
                                DropData drop = cat.dropSeedAllowedDropsOnly();
                                if (drop == null) {
                                    continue;
                                }

                                item = this.calculateRewardItem(drop, levelModifier, false);
                            } else {
                                item = this.calculateCategorizedRewardItem(cat, levelModifier);
                            }

                            if (item != null && !player.ignoredDropContain(item.getId())) {
                                if ((!this.isRaidBoss() || !Config.AUTO_LOOT_RAID) && (this.isRaidBoss() || !Config.AUTO_LOOT)) {
                                    this.dropItem(player, item);
                                } else {
                                    player.doAutoLoot(this, item);
                                }

                                if (this.isRaidBoss()) {
                                    this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addCharName(this).addItemName(item.getId()).addNumber(item.getValue()));
                                }
                            }
                        }
                    }

                    if (this.isChampion() && Config.CHAMPION_REWARD > 0) {
                        int dropChance = Config.CHAMPION_REWARD;
                        if (Config.DEEPBLUE_DROP_RULES) {
                            int deepBlueDrop = levelModifier > 0 ? 3 : 1;
                            dropChance = (Config.CHAMPION_REWARD - Config.CHAMPION_REWARD * levelModifier / 100) / deepBlueDrop;
                        }

                        if (Rnd.get(100) < dropChance) {
                            IntIntHolder item = new IntIntHolder(Config.CHAMPION_REWARD_ID, Math.max(1, Rnd.get(1, Config.CHAMPION_REWARD_QTY)));
                            if (Config.AUTO_LOOT) {
                                player.addItem("ChampionLoot", item.getId(), item.getValue(), this, true);
                            } else {
                                this.dropItem(player, item);
                            }
                        }
                    }

                    if (this.getTemplate().getDropHerbGroup() > 0) {
                        for (DropCategory cat : HerbDropData.getInstance().getHerbDroplist(this.getTemplate().getDropHerbGroup())) {
                            IntIntHolder item = calculateCategorizedHerbItem(cat, levelModifier);
                            if (item != null) {
                                if (Config.AUTO_LOOT_HERBS) {
                                    player.addItem("Loot", item.getId(), 1, this, true);
                                } else {
                                    int count = item.getValue();
                                    if (count > 1) {
                                        item.setValue(1);

                                        for (int i = 0; i < count; ++i) {
                                            this.dropItem(player, item);
                                        }
                                    } else {
                                        this.dropItem(player, item);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public void doItemDrop(Creature mainDamageDealer) {
        this.doItemDrop(this.getTemplate(), mainDamageDealer);
    }

    public void dropItem(Player mainDamageDealer, IntIntHolder holder) {
        for (int i = 0; i < holder.getValue(); ++i) {
            ItemInstance item = ItemInstance.create(holder.getId(), holder.getValue(), mainDamageDealer, this);
            item.setDropProtection(mainDamageDealer.getObjectId(), this.isRaidBoss());
            if (this.getInstance() != null) {
                item.setInstance(this.getInstance(), true);
            } else {
                item.setInstance(this.getInstance(), false);
            }

            item.dropMe(this, this.getX() + Rnd.get(-70, 70), this.getY() + Rnd.get(-70, 70), Math.max(this.getZ(), mainDamageDealer.getZ()) + 20);
            if (item.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
                break;
            }
        }

    }

    private static class CommandChannelTimer implements Runnable {
        private final Monster _monster;

        public CommandChannelTimer(Monster monster) {
            this._monster = monster;
        }

        public void run() {
            if (System.currentTimeMillis() - this._monster.getCommandChannelLastAttack() > 900000L) {
                this._monster.setCommandChannelTimer(null);
                this._monster.setFirstCommandChannelAttacked(null);
                this._monster.setCommandChannelLastAttack(0L);
            } else {
                ThreadPool.schedule(this, 10000L);
            }

        }
    }
}
