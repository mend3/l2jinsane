package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Map;

public class ColorAccordingAmountPvPorPk extends AbstractMods {
    public ColorAccordingAmountPvPorPk() {
        this.registerMod(ConfigData.ENABLE_ColorAccordingAmountPvPorPk);
    }

    private static void checkColorPlayer(Player activeChar) {
        if (!activeChar.isGM()) {
            String colorPvp = "";
            String colorPk = "";

            for (Map.Entry<Integer, String> pvp : ConfigData.PVP_COLOR_NAME.entrySet()) {
                if (activeChar.getPvpKills() >= pvp.getKey()) {
                    colorPvp = pvp.getValue();
                }
            }

            for (Map.Entry<Integer, String> pk : ConfigData.PK_COLOR_TITLE.entrySet()) {
                if (activeChar.getPkKills() >= pk.getKey()) {
                    colorPk = pk.getValue();
                }
            }

            if (!colorPvp.equals("")) {
                activeChar.getAppearance().setNameColor(Integer.decode("0x" + colorPvp));
            }

            if (!colorPk.equals("")) {
                activeChar.getAppearance().setTitleColor(Integer.decode("0x" + colorPk));
            }

            activeChar.broadcastUserInfo();
        }
    }

    public static ColorAccordingAmountPvPorPk getInstance() {
        return ColorAccordingAmountPvPorPk.SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (Util.areObjectType(Player.class, killer, victim)) {
            checkColorPlayer(killer.getActingPlayer());
        }
    }

    public void onEnterWorld(Player player) {
        checkColorPlayer(player);
    }

    private static class SingletonHolder {
        protected static final ColorAccordingAmountPvPorPk INSTANCE = new ColorAccordingAmountPvPorPk();
    }
}
