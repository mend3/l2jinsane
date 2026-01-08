package enginemods.main.engine.events;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.enums.ExpSpType;
import enginemods.main.enums.ItemDropType;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Player;

public class BonusWeekend extends AbstractMods {
    public BonusWeekend() {
        this.registerMod(ConfigData.ENABLE_BonusWeekend, ConfigData.BONUS_WEEKEND_ENABLE_DAY);
    }

    public static BonusWeekend getInstance() {
        return BonusWeekend.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onNpcExpSp(Player killer, Attackable npc, NpcExpInstance instance) {
        instance.increaseRate(ExpSpType.EXP, ConfigData.BONUS_WEEKEND_RATE_EXP);
        instance.increaseRate(ExpSpType.SP, ConfigData.BONUS_WEEKEND_RATE_SP);
    }

    public void onNpcDrop(Player killer, Attackable npc, NpcDropsInstance instance) {
        instance.increaseDrop(ItemDropType.NORMAL, ConfigData.BONUS_WEEKEND_DROP, ConfigData.BONUS_WEEKEND_DROP);
        instance.increaseDrop(ItemDropType.SPOIL, ConfigData.BONUS_WEEKEND_SPOIL, ConfigData.BONUS_WEEKEND_SPOIL);
        instance.increaseDrop(ItemDropType.HERB, ConfigData.BONUS_WEEKEND_HERB, ConfigData.BONUS_WEEKEND_HERB);
        instance.increaseDrop(ItemDropType.SEED, ConfigData.BONUS_WEEKEND_SEED, ConfigData.BONUS_WEEKEND_SEED);
    }

    private static class SingletonHolder {
        protected static final BonusWeekend INSTANCE = new BonusWeekend();
    }
}
