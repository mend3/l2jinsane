/**/
package net.sf.l2j.gameserver.network;

import net.sf.l2j.Config;

public final class FloodProtectors {
    public static boolean performAction(GameClient client, FloodProtectors.Action action) {
        int reuseDelay = action.getReuseDelay();
        if (reuseDelay == 0) {
            return true;
        } else {
            long[] value = client.getFloodProtectors();
            synchronized (value) {
                if (value[action.ordinal()] > System.currentTimeMillis()) {
                    return false;
                } else {
                    value[action.ordinal()] = System.currentTimeMillis() + (long) reuseDelay;
                    return true;
                }
            }
        }
    }

    public enum Action {
        ROLL_DICE(Config.ROLL_DICE_TIME),
        HERO_VOICE(Config.HERO_VOICE_TIME),
        SUBCLASS(Config.SUBCLASS_TIME),
        DROP_ITEM(Config.DROP_ITEM_TIME),
        SERVER_BYPASS(Config.SERVER_BYPASS_TIME),
        MULTISELL(Config.MULTISELL_TIME),
        MANUFACTURE(Config.MANUFACTURE_TIME),
        MANOR(Config.MANOR_TIME),
        SENDMAIL(Config.SENDMAIL_TIME),
        CHARACTER_SELECT(Config.CHARACTER_SELECT_TIME),
        GLOBAL_CHAT(Config.GLOBAL_CHAT_TIME),
        TRADE_CHAT(Config.TRADE_CHAT_TIME),
        SOCIAL(Config.SOCIAL_TIME);

        public static final int VALUES_LENGTH = values().length;
        private final int _reuseDelay;

        Action(int reuseDelay) {
            this._reuseDelay = reuseDelay;
        }

        // $FF: synthetic method
        private static FloodProtectors.Action[] $values() {
            return new FloodProtectors.Action[]{ROLL_DICE, HERO_VOICE, SUBCLASS, DROP_ITEM, SERVER_BYPASS, MULTISELL, MANUFACTURE, MANOR, SENDMAIL, CHARACTER_SELECT, GLOBAL_CHAT, TRADE_CHAT, SOCIAL};
        }

        public int getReuseDelay() {
            return this._reuseDelay;
        }
    }
}