package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.RewardHolder;
import enginemods.main.util.Util;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class PvpReward extends AbstractMods {
    public PvpReward() {
        this.registerMod(ConfigData.ENABLE_PvpReward);
    }

    private static void giveRewards(Player killer, Player victim) {
        for (RewardHolder reward : ConfigData.PVP_REWARDS) {
            if (Rnd.get(100) <= reward.getRewardChance()) {
                int var10006 = reward.getRewardCount();
                killer.sendPacket(new CreatureSay(0, 2, "", "Have won " + var10006 + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName()));
                killer.getActingPlayer().getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), killer, victim);
            }
        }

    }

    public static PvpReward getInstance() {
        return PvpReward.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (Util.areObjectType(Player.class, victim) && killer.getActingPlayer() != null) {
            Player killerPc = killer.getActingPlayer();
            giveRewards(killerPc, (Player) victim);
        }
    }

    private static class SingletonHolder {
        protected static final PvpReward INSTANCE = new PvpReward();
    }

    public class PvPHolder {
        public int _victim;
        public long _time;

        public PvPHolder(int victim, long time) {
            this._victim = victim;
            this._time = time;
        }
    }
}
