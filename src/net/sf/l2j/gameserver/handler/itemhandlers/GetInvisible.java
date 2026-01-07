package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.HashMap;
import java.util.Map;

public class GetInvisible implements IItemHandler {
    private static final int REUSE_DELAY = 30000;

    private static final int INVIS_TIME = 30000;

    private static final int SKILL_ID = 922;

    private static final int SKILL_LVL = 1;

    private static final Map<Integer, Long> REUSE_LIST = new HashMap<>();

    private static final Map<Integer, Boolean> INVIS_STATUS = new HashMap<>();

    private static long getReuseTime(Player player) {
        long now = System.currentTimeMillis();
        if (REUSE_LIST.containsKey(Integer.valueOf(player.getObjectId())))
            return now - REUSE_LIST.get(Integer.valueOf(player.getObjectId()));
        return 30000L;
    }

    private static void setReuseTime(Player player) {
        REUSE_LIST.put(Integer.valueOf(player.getObjectId()), Long.valueOf(System.currentTimeMillis()));
    }

    private static boolean isInvisible(Player player) {
        return INVIS_STATUS.getOrDefault(Integer.valueOf(player.getObjectId()), Boolean.valueOf(false));
    }

    private static void setInvisibleStatus(Player player, boolean status) {
        INVIS_STATUS.put(Integer.valueOf(player.getObjectId()), Boolean.valueOf(status));
    }

    private static boolean activateInvisibility(Player player) {
        try {
            L2Skill skill = SkillTable.getInstance().getInfo(922, 1);
            if (skill == null) {
                System.out.println("Skill 922 no existe en la DB.");
                return false;
            }
            if (!skill.checkCondition(player, player.getTarget(), false)) {
                player.sendMessage("No puedes usar invisibilidad ahora.");
                return false;
            }
            if (player.useMagic(skill, false, false)) {
                player.broadcastPacket(new MagicSkillUse(player, player, 7066, 1, 1, 0));
                setInvisibleStatus(player, true);
                player.getAppearance().setInvisible();
                player.broadcastUserInfo();
                ThreadPool.schedule(() -> player.broadcastPacket(new MagicSkillUse(player, player, 922, 1, 1, 0)), 1200L);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void removeInvisibility(Player player) {
        try {
            setInvisibleStatus(player, false);
            player.getAppearance().setVisible();
            player.broadcastUserInfo();
            L2Skill skill = SkillTable.getInstance().getInfo(922, 1);
            if (skill != null)
                player.stopSkillEffects(922);
            player.sendMessage("La invisibilidad ha terminado.");
        } catch (Exception e) {
            System.out.println("Error al remover invisibilidad: " + e.getMessage());
        }
    }

    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        if (player.isOutOfControl()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (item.getLocation() != ItemInstance.ItemLocation.INVENTORY) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInOlympiadMode()) {
            player.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return;
        }
        long reuse = getReuseTime(player);
        if (reuse < 30000L) {
            long remaining = (30000L - reuse) / 1000L;
            player.sendMessage("Cooldown: " + remaining + " segundos.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (isInvisible(player)) {
            player.sendMessage("Ya eres invisible.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (activateInvisibility(player)) {
            player.sendMessage("Has activado invisibilidad durante 30 segundos.");
            ThreadPool.schedule(() -> removeInvisibility(player), 30000L);
            setReuseTime(player);
        } else {
            player.sendMessage("No se pudo activar la invisibilidad.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }
}
