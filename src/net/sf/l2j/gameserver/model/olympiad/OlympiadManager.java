package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OlympiadManager {
    private final List<Integer> _nonClassBasedRegisters = new CopyOnWriteArrayList<>();

    private final Map<Integer, List<Integer>> _classBasedRegisters = new ConcurrentHashMap<>();

    private static boolean isInCompetition(Player player, boolean showMessage) {
        if (!Olympiad.getInstance().isInCompPeriod())
            return false;
        for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; ) {
            AbstractOlympiadGame game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
            if (game == null)
                continue;
            if (game.containsParticipant(player.getObjectId())) {
                if (showMessage)
                    player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
                return true;
            }
        }
        return false;
    }

    public static OlympiadManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public final List<Integer> getRegisteredNonClassBased() {
        return this._nonClassBasedRegisters;
    }

    public final Map<Integer, List<Integer>> getRegisteredClassBased() {
        return this._classBasedRegisters;
    }

    protected final List<List<Integer>> hasEnoughRegisteredClassed() {
        List<List<Integer>> result = null;
        for (Map.Entry<Integer, List<Integer>> classList : this._classBasedRegisters.entrySet()) {
            if (classList.getValue() != null && classList.getValue().size() >= Config.ALT_OLY_CLASSED) {
                if (result == null)
                    result = new ArrayList<>();
                result.add(classList.getValue());
            }
        }
        return result;
    }

    protected final boolean hasEnoughRegisteredNonClassed() {
        return (this._nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED);
    }

    protected final void clearRegistered() {
        this._nonClassBasedRegisters.clear();
        this._classBasedRegisters.clear();
    }

    public final boolean isRegistered(Player noble) {
        return isRegistered(noble, false);
    }

    private final boolean isRegistered(Player player, boolean showMessage) {
        Integer objId = Integer.valueOf(player.getObjectId());
        if (this._nonClassBasedRegisters.contains(objId)) {
            if (showMessage)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME));
            return true;
        }
        List<Integer> classed = this._classBasedRegisters.get(Integer.valueOf(player.getBaseClass()));
        if (classed != null && classed.contains(objId)) {
            if (showMessage)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS));
            return true;
        }
        return false;
    }

    public final boolean isRegisteredInComp(Player noble) {
        return (isRegistered(noble, false) || isInCompetition(noble, false));
    }

    public final boolean registerNoble(Player player, OlympiadType type) {
        List<Integer> classed;
        if (!Olympiad.getInstance().isInCompPeriod()) {
            player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            return false;
        }
        if (Olympiad.getInstance().getMillisToCompEnd() < 600000L) {
            player.sendPacket(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
            return false;
        }
        switch (type) {
            case CLASSED:
                if (!checkNoble(player))
                    return false;
                classed = this._classBasedRegisters.get(Integer.valueOf(player.getBaseClass()));
                if (classed != null) {
                    classed.add(Integer.valueOf(player.getObjectId()));
                } else {
                    classed = new CopyOnWriteArrayList<>();
                    classed.add(Integer.valueOf(player.getObjectId()));
                    this._classBasedRegisters.put(Integer.valueOf(player.getBaseClass()), classed);
                }
                player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
                break;
            case NON_CLASSED:
                if (!checkNoble(player))
                    return false;
                this._nonClassBasedRegisters.add(Integer.valueOf(player.getObjectId()));
                player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
                break;
        }
        return true;
    }

    public final boolean unRegisterNoble(Player player) {
        if (!Olympiad.getInstance().isInCompPeriod()) {
            player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
            return false;
        }
        if (!player.isNoble()) {
            player.sendPacket(SystemMessageId.NOBLESSE_ONLY);
            return false;
        }
        if (!isRegistered(player, false)) {
            player.sendPacket(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
            return false;
        }
        if (isInCompetition(player, false))
            return false;
        Integer objectId = Integer.valueOf(player.getObjectId());
        if (this._nonClassBasedRegisters.remove(objectId)) {
            player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
            return true;
        }
        List<Integer> classed = this._classBasedRegisters.get(Integer.valueOf(player.getBaseClass()));
        if (classed != null && classed.remove(objectId)) {
            this._classBasedRegisters.remove(Integer.valueOf(player.getBaseClass()));
            this._classBasedRegisters.put(Integer.valueOf(player.getBaseClass()), classed);
            player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
            return true;
        }
        return false;
    }

    public final void removeDisconnectedCompetitor(Player player) {
        OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
        if (task != null && task.isGameStarted())
            task.getGame().handleDisconnect(player);
        Integer objId = Integer.valueOf(player.getObjectId());
        if (this._nonClassBasedRegisters.remove(objId))
            return;
        List<Integer> classed = this._classBasedRegisters.get(Integer.valueOf(player.getBaseClass()));
        if (classed != null && classed.remove(objId)) {
        }
    }

    private final boolean checkNoble(Player player) {
        if (!player.isNoble()) {
            player.sendPacket(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
            return false;
        }
        if (player.isSubClassActive()) {
            player.sendPacket(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
            return false;
        }
        if (player.isCursedWeaponEquipped()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(player.getCursedWeaponEquippedId()));
            return false;
        }
        if (player.getInventoryLimit() * 0.8D <= player.getInventory().getSize()) {
            player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
            return false;
        }
        if (isRegistered(player, true))
            return false;
        if (isInCompetition(player, true))
            return false;
        StatSet statDat = Olympiad.getInstance().getNobleStats(player.getObjectId());
        if (statDat == null) {
            statDat = new StatSet();
            statDat.set("class_id", player.getBaseClass());
            statDat.set("char_name", player.getName());
            statDat.set("olympiad_points", Config.ALT_OLY_START_POINTS);
            statDat.set("competitions_done", 0);
            statDat.set("competitions_won", 0);
            statDat.set("competitions_lost", 0);
            statDat.set("competitions_drawn", 0);
            statDat.set("to_save", true);
            Olympiad.getInstance().addNobleStats(player.getObjectId(), statDat);
        }
        int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
        if (points <= 0) {
            NpcHtmlMessage message = new NpcHtmlMessage(0);
            message.setFile("data/html/olympiad/noble_nopoints1.htm");
            message.replace("%objectId%", player.getTargetId());
            player.sendPacket(message);
            return false;
        }
        return true;
    }

    private static class SingletonHolder {
        protected static final OlympiadManager INSTANCE = new OlympiadManager();
    }
}
