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
        if (this.cond == null || this.cond.test(env)) {
            ItemInstance item = (ItemInstance) this.funcOwner;
            int enchant = item.getEnchantLevel();
            if (enchant > 0) {
                int overenchant = 0;
                if (enchant > 3) {
                    overenchant = enchant - 3;
                    enchant = 3;
                }

                if (this.stat != Stats.MAGIC_DEFENCE && this.stat != Stats.POWER_DEFENCE) {
                    if (this.stat == Stats.MAGIC_ATTACK) {
                        switch (item.getItem().getCrystalType()) {
                            case S:
                                env.addValue(4 * enchant + 8 * overenchant);
                                break;
                            case A:
                            case B:
                            case C:
                                env.addValue(3 * enchant + 6 * overenchant);
                                break;
                            case D:
                                env.addValue(2 * enchant + 4 * overenchant);
                        }

                    } else {
                        if (item.isWeapon()) {
                            WeaponType type = (WeaponType) item.getItemType();
                            switch (item.getItem().getCrystalType()) {
                                case S:
                                    switch (type) {
                                        case BOW:
                                            env.addValue(10 * enchant + 20 * overenchant);
                                            return;
                                        case BIGBLUNT:
                                        case BIGSWORD:
                                        case DUALFIST:
                                        case DUAL:
                                            env.addValue(6 * enchant + 12 * overenchant);
                                            return;
                                        default:
                                            env.addValue(5 * enchant + 10 * overenchant);
                                            return;
                                    }
                                case A:
                                    switch (type) {
                                        case BOW:
                                            env.addValue(8 * enchant + 16 * overenchant);
                                            return;
                                        case BIGBLUNT:
                                        case BIGSWORD:
                                        case DUALFIST:
                                        case DUAL:
                                            env.addValue(5 * enchant + 10 * overenchant);
                                            return;
                                        default:
                                            env.addValue(4 * enchant + 8 * overenchant);
                                            return;
                                    }
                                case B:
                                    switch (type) {
                                        case BOW:
                                            env.addValue(6 * enchant + 12 * overenchant);
                                            return;
                                        case BIGBLUNT:
                                        case BIGSWORD:
                                        case DUALFIST:
                                        case DUAL:
                                            env.addValue(4 * enchant + 8 * overenchant);
                                            return;
                                        default:
                                            env.addValue(3 * enchant + 6 * overenchant);
                                            return;
                                    }
                                case C:
                                    switch (type) {
                                        case BOW:
                                            env.addValue(6 * enchant + 12 * overenchant);
                                            return;
                                        case BIGBLUNT:
                                        case BIGSWORD:
                                        case DUALFIST:
                                        case DUAL:
                                            env.addValue(4 * enchant + 8 * overenchant);
                                            return;
                                        default:
                                            env.addValue(3 * enchant + 6 * overenchant);
                                            return;
                                    }
                                case D:
                                    switch (type) {
                                        case BOW -> env.addValue(4 * enchant + 8 * overenchant);
                                        default -> env.addValue(2 * enchant + 4 * overenchant);
                                    }
                            }
                        }

                    }
                } else {
                    env.addValue(enchant + 3 * overenchant);
                }
            }
        }
    }
}
