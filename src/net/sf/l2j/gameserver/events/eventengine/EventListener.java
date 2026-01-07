package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;

public class EventListener {
    public static boolean isAutoAttackable(Player attacker, Player target) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().isAutoAttackable(attacker, target);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().isAutoAttackable(attacker, target);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().isAutoAttackable(attacker, target);
        return false;
    }

    public static void onKill(Player killer, Player victim) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            TvTEventManager.getInstance().getActiveEvent().onKill(killer, victim);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            CtfEventManager.getInstance().getActiveEvent().onKill(killer, victim);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            DmEventManager.getInstance().getActiveEvent().onKill(killer, victim);
    }

    public static boolean onSay(Player player, String text) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().onSay(player, text);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().onSay(player, text);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().onSay(player, text);
        return true;
    }

    public static void onInterract(Player player, Npc npc) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            TvTEventManager.getInstance().getActiveEvent().onInterract(player, npc);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            CtfEventManager.getInstance().getActiveEvent().onInterract(player, npc);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            DmEventManager.getInstance().getActiveEvent().onInterract(player, npc);
    }

    public static boolean canAttack(Player attacker, Player target) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().canAttack(attacker, target);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().canAttack(attacker, target);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().canAttack(attacker, target);
        return true;
    }

    public static boolean canHeal(Player healer, Player target) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().canHeal(healer, target);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().canHeal(healer, target);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().canHeal(healer, target);
        return true;
    }

    public static boolean canUseItem(Player player, int itemId) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().canUseItem(player, itemId);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().canUseItem(player, itemId);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().canUseItem(player, itemId);
        return true;
    }

    public static boolean allowDiePacket(Player player) {
        if (TvTEventManager.getInstance().getActiveEvent() != null)
            return TvTEventManager.getInstance().getActiveEvent().allowDiePacket(player);
        if (CtfEventManager.getInstance().getActiveEvent() != null)
            return CtfEventManager.getInstance().getActiveEvent().allowDiePacket(player);
        if (DmEventManager.getInstance().getActiveEvent() != null)
            return DmEventManager.getInstance().getActiveEvent().allowDiePacket(player);
        return true;
    }
}
