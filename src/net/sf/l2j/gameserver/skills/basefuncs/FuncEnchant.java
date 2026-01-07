package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.skills.Env;

public class FuncEnchant extends Func {
    public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda) {
        super(pStat, pOrder, owner, lambda);
    }

    public void calc(Env env) {
        if (this.cond != null && !this.cond.test(env))
            return;
        ItemInstance item = (ItemInstance) this.funcOwner;
        int enchant = item.getEnchantLevel();
        if (enchant <= 0)
            return;
        int overenchant = 0;
        if (enchant > 3) {
            overenchant = enchant - 3;
            enchant = 3;
        }
        if (this.stat == Stats.MAGIC_DEFENCE || this.stat == Stats.POWER_DEFENCE) {
            env.addValue((enchant + 3 * overenchant));
            return;
        }
        if (this.stat == Stats.MAGIC_ATTACK) {
            switch (item.getItem().getItemType()) {
                case WeaponType.BOW:
                    env.addValue((4 * enchant + 8 * overenchant));
                    break;
                case WeaponType.BIGBLUNT:
                case WeaponType.BIGSWORD:
                case WeaponType.DUALFIST:
                    env.addValue((3 * enchant + 6 * overenchant));
                    break;
                case WeaponType.DUAL:
                    env.addValue((2 * enchant + 4 * overenchant));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + item.getItem().getItemType());
            }
            return;
        }
        if (item.isWeapon()) {
            WeaponType type = (WeaponType) item.getItemType();
            switch (item.getItem().getItemType()) {
                case WeaponType.BOW:
                    switch (type) {
                        case BOW:
                            env.addValue((10 * enchant + 20 * overenchant));
                            break;
                        case BIGBLUNT:
                        case BIGSWORD:
                        case DUALFIST:
                        case DUAL:
                            env.addValue((6 * enchant + 12 * overenchant));
                            break;
                    }
                    env.addValue((5 * enchant + 10 * overenchant));
                    break;
                case WeaponType.BIGBLUNT:
                    switch (type) {
                        case BOW:
                            env.addValue((8 * enchant + 16 * overenchant));
                            break;
                        case BIGBLUNT:
                        case BIGSWORD:
                        case DUALFIST:
                        case DUAL:
                            env.addValue((5 * enchant + 10 * overenchant));
                            break;
                    }
                    env.addValue((4 * enchant + 8 * overenchant));
                    break;
                case WeaponType.BIGSWORD, WeaponType.DUALFIST:
                    switch (type) {
                        case BOW:
                            env.addValue((6 * enchant + 12 * overenchant));
                            break;
                        case BIGBLUNT:
                        case BIGSWORD:
                        case DUALFIST:
                        case DUAL:
                            env.addValue((4 * enchant + 8 * overenchant));
                            break;
                    }
                    env.addValue((3 * enchant + 6 * overenchant));
                    break;
                case WeaponType.DUAL:
                    switch (type) {
                        case BOW:
                            env.addValue((4 * enchant + 8 * overenchant));
                            break;
                    }
                    env.addValue((2 * enchant + 4 * overenchant));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + item.getItem().getItemType());
            }
        }
    }
}
