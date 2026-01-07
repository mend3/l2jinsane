package enginemods.main.instances;

import enginemods.main.enums.ExpSpType;
import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.npc.AggroInfo;
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NpcExpInstance {
    private final Map<ExpSpType, Double> _expSettings = new HashMap<>();

    public NpcExpInstance() {
        this._expSettings.put(ExpSpType.EXP, Double.valueOf(1.0D));
        this._expSettings.put(ExpSpType.SP, Double.valueOf(1.0D));
    }

    private static int[] calculateExpAndSp(Attackable npc, int diff, int damage, long totalDamage) {
        if (diff < -5)
            diff = -5;
        double xp = (long) npc.getExpReward() * damage / totalDamage;
        double sp = (long) npc.getSpReward() * damage / totalDamage;
        L2Skill hpSkill = npc.getSkill(4408);
        if (hpSkill != null) {
            xp *= hpSkill.getPower();
            sp *= hpSkill.getPower();
        }
        if (diff > 5) {
            double pow = Math.pow(0.8333333333333334D, (diff - 5));
            xp *= pow;
            sp *= pow;
        }
        if (xp <= 0.0D) {
            xp = 0.0D;
            sp = 0.0D;
        } else if (sp <= 0.0D) {
            sp = 0.0D;
        }
        int[] tmp = {(int) xp, (int) sp};
        return tmp;
    }

    public void increaseRate(ExpSpType type, double bonus) {
        double oldValue = this._expSettings.get(type);
        this._expSettings.put(type, Double.valueOf(oldValue + bonus - 1.0D));
    }

    public boolean hasSettings() {
        for (Double value : this._expSettings.values()) {
            if (value > 1.0D)
                return true;
        }
        return false;
    }

    public void init(Monster npc, Creature lastAttacker) {
        if (npc.getAggroList().isEmpty())
            return;
        Map<Creature, RewardInfo> rewards = new ConcurrentHashMap<>();
        Player maxDealer = null;
        int maxDamage = 0;
        long totalDamage = 0L;
        for (AggroInfo info : npc.getAggroList().values()) {
            if (info == null)
                continue;
            Player attacker = info.getAttacker().getActingPlayer();
            if (attacker == null)
                continue;
            int damage = info.getDamage();
            if (damage <= 1)
                continue;
            if (!MathUtil.checkIfInRange(Config.PARTY_RANGE, npc, attacker, true))
                continue;
            totalDamage += damage;
            RewardInfo reward = rewards.get(attacker);
            if (reward == null) {
                reward = new RewardInfo(attacker);
                rewards.put(attacker, reward);
            }
            reward.addDamage(damage);
            if (reward.getDamage() > maxDamage) {
                maxDealer = attacker;
                maxDamage = reward.getDamage();
            }
        }
        npc.doItemDrop((maxDealer != null && maxDealer.isOnline()) ? (Creature) maxDealer : lastAttacker);
        for (RewardInfo reward : rewards.values()) {
            if (reward == null)
                continue;
            Player attacker = reward.getAttacker().getActingPlayer();
            int damage = reward.getDamage();
            Party attackerParty = attacker.getParty();
            float penalty = attacker.hasServitor() ? ((Servitor) attacker.getSummon()).getExpPenalty() : 0.0F;
            if (attackerParty == null) {
                if (!attacker.isDead() && attacker.getKnownType(Attackable.class).contains(npc)) {
                    int i = attacker.getLevel() - npc.getLevel();
                    int[] arrayOfInt = calculateExpAndSp(npc, i, damage, totalDamage);
                    long l = arrayOfInt[0];
                    int j = arrayOfInt[1];
                    l = (long) (l * this._expSettings.get(ExpSpType.EXP));
                    j = (int) (j * this._expSettings.get(ExpSpType.SP));
                    l = (long) ((float) l * (1.0F - penalty));
                    if (npc.isOverhit() && npc.getOverhitAttacker() != null && npc.getOverhitAttacker().getActingPlayer() != null && attacker == npc.getOverhitAttacker().getActingPlayer()) {
                        attacker.sendPacket(SystemMessageId.OVER_HIT);
                        l += npc.calculateOverhitExp(l);
                    }
                    attacker.updateKarmaLoss(l);
                    attacker.addExpAndSp(l, j);
                }
                continue;
            }
            int partyDmg = 0;
            float partyMul = 1.0F;
            int partyLvl = 0;
            List<Player> rewardedMembers = new ArrayList<>();
            List<Player> groupMembers = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();
            Map<Creature, RewardInfo> playersWithPets = new HashMap<>();
            for (Player partyPlayer : groupMembers) {
                if (partyPlayer == null || partyPlayer.isDead())
                    continue;
                RewardInfo reward2 = rewards.get(partyPlayer);
                if (reward2 != null) {
                    if (MathUtil.checkIfInRange(Config.PARTY_RANGE, npc, partyPlayer, true)) {
                        partyDmg += reward2.getDamage();
                        rewardedMembers.add(partyPlayer);
                        if (partyPlayer.getLevel() > partyLvl)
                            partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
                    }
                    rewards.remove(partyPlayer);
                    playersWithPets.put(partyPlayer, reward2);
                    if (partyPlayer.hasPet() && rewards.containsKey(partyPlayer.getSummon()))
                        playersWithPets.put(partyPlayer.getSummon(), rewards.get(partyPlayer.getSummon()));
                    continue;
                }
                if (MathUtil.checkIfInRange(Config.PARTY_RANGE, npc, partyPlayer, true)) {
                    rewardedMembers.add(partyPlayer);
                    if (partyPlayer.getLevel() > partyLvl)
                        partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
                }
            }
            if (partyDmg < totalDamage)
                partyMul = partyDmg / (float) totalDamage;
            int levelDiff = partyLvl - npc.getLevel();
            int[] expSp = calculateExpAndSp(npc, levelDiff, partyDmg, totalDamage);
            long exp = expSp[0];
            int sp = expSp[1];
            exp = (long) (exp * this._expSettings.get(ExpSpType.EXP));
            sp = (int) (sp * this._expSettings.get(ExpSpType.SP));
            exp = (long) ((float) exp * partyMul);
            sp = (int) (sp * partyMul);
            if (npc.isOverhit() && npc.getOverhitAttacker() != null && npc.getOverhitAttacker().getActingPlayer() != null && attacker == npc.getOverhitAttacker().getActingPlayer()) {
                attacker.sendPacket(SystemMessageId.OVER_HIT);
                exp += npc.calculateOverhitExp(exp);
            }
            if (partyDmg > 0)
                attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, playersWithPets);
        }
    }
}
