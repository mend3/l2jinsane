package net.sf.l2j.gameserver.model.zone.type;

import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class L2RandomZone extends SpawnZoneType {
    public static final List<String> _classes = new ArrayList<>();
    public static final List<String> _items = new ArrayList<>();
    public static final Map<Integer, Integer> _rewards = new ConcurrentHashMap<>();
    private final Map<String, List<Player>> _zergMap = new ConcurrentHashMap<>();
    public String _name;
    public int _time;
    public final List<Location> _locations = new ArrayList<>();
    private int _id;
    private int _maxClanMembers;

    private int _maxNOClanMembers;

    public L2RandomZone(int id) {
        super(id);
    }

    public static boolean checkItem(ItemInstance item) {
        return _items == null || !_items.contains("" + item.getItemId());
    }

    public void setParameter(String name, String value) {
        switch (name) {
            case "id" -> this._id = Integer.parseInt(value);
            case "name" -> this._name = value;
            case "time" -> this._time = Integer.parseInt(value);
            case "locs" -> {
                for (String locs : value.split(";"))
                    this._locations.add(new Location(Integer.parseInt(locs.split(",")[0]), Integer.parseInt(locs.split(",")[1]), Integer.parseInt(locs.split(",")[2])));
            }
            case "disabledClasses" -> Collections.addAll(_classes, value.split(","));
            case "disabledItems" -> Collections.addAll(_items, value.split(","));
            case "rewards" -> {
                for (String id : value.split(";"))
                    _rewards.put(Integer.valueOf(id.split(",")[0]), Integer.valueOf(id.split(",")[1]));
            }
            case "MaxClanMembers" -> this._maxClanMembers = Integer.parseInt(value);
            case "MaxNOClanMembers" -> this._maxNOClanMembers = Integer.parseInt(value);
            default -> super.setParameter(name, value);
        }
    }

    protected void onEnter(Creature character) {
        if (character instanceof Player activeChar) {
            activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
            if (activeChar.getClan() == null && !activeChar.isGM()) {
                String zergNOClan = "NoClan";
                this._zergMap.computeIfAbsent("NoClan", k -> new ArrayList<>());
                if (this._zergMap.get("NoClan").size() > this._maxNOClanMembers) {
                    activeChar.sendMessage("Sorry only allowed " + this._maxClanMembers + " members by Clan.");
                    ThreadPool.execute(new KickPlayer(activeChar));
                    return;
                }
                this._zergMap.get("NoClan").add(activeChar);
            }
            if (activeChar.getClan() != null && !activeChar.isGM()) {
                String zergClan1 = activeChar.getClan().getName();
                this._zergMap.computeIfAbsent(zergClan1, k -> new ArrayList<>());
                if (this._zergMap.get(zergClan1).size() > this._maxClanMembers) {
                    activeChar.sendMessage("Sorry only allowed " + this._maxClanMembers + " members by Clan.");
                    ThreadPool.execute(new KickPlayer(activeChar));
                    return;
                }
                this._zergMap.get(zergClan1).add(activeChar);
            }
            if (_classes != null && _classes.contains("" + activeChar.getClassId().getId())) {
                activeChar.teleportTo(82725, 148596, -3468, 0);
                activeChar.sendMessage("Your class is not allowed in the Random PvP zone.");
                return;
            }
            for (ItemInstance o : activeChar.getInventory().getItems()) {
                if (o.isEquipable() && o.isEquipped() && !checkItem(o)) {
                    int slot = activeChar.getInventory().getSlotFromItem(o);
                    activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
                    activeChar.sendMessage(o.getItemName() + " unequiped because is not allowed inside this zone.");
                }
            }
            activeChar.sendPacket(new CreatureSay(0, 16, "PvP Zone", "will be changed in " + RandomZoneManager.getInstance().getLeftTime()));
            L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
            noblesse.getEffects(activeChar, activeChar);
            if (activeChar.getPvpFlag() > 0)
                PvpFlagTaskManager.getInstance().remove(activeChar);
            activeChar.setPvpFlag(1);
            activeChar.sendPacket(new CreatureSay(0, 2, character.getName(), ": Remember for exit use command .exit"));
            activeChar.broadcastUserInfo();
        }
        character.setInsideZone(ZoneId.RANDOMZONE, true);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
        character.setInsideZone(ZoneId.NO_RESTART, true);
    }

    protected void onExit(Creature character) {
        if (character instanceof Player player) {
            player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
            if (player.getClan() != null) {
                String zergClan1 = player.getClan().getName();
                if (this._zergMap.get(zergClan1) != null)
                    this._zergMap.get(zergClan1).remove(player);
            } else {
                String zergNOClan = "NoClan";
                if (this._zergMap.get("NoClan") != null)
                    this._zergMap.get("NoClan").remove(player);
            }
            player.setPvpFlag(0);
            player.broadcastUserInfo();
        }
        character.setInsideZone(ZoneId.RANDOMZONE, false);
        character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        character.setInsideZone(ZoneId.NO_RESTART, false);
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
        if (character instanceof Player activeChar) {
            L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
            noblesse.getEffects(activeChar, activeChar);
        }
    }

    public void respawnCharacter(Creature character) {
        if (character == null || !character.isDead() || !(character instanceof Player))
            return;
        character.doRevive();
        character.setCurrentHp(character.getMaxHp());
        character.setCurrentCp(character.getMaxCp());
        character.setCurrentMp(character.getMaxMp());
        L2Skill noblesse = SkillTable.getInstance().getInfo(1323, 1);
        noblesse.getEffects(character, character);
        if (RandomZoneManager.getInstance().getCurrentZone() != null && character.isInsideZone(ZoneId.RANDOMZONE)) {
            character.teleportTo(RandomZoneManager.getInstance().getCurrentZone().getLoc(), 20);
        } else {
            character.teleportTo(82635, 148798, -3464, 25);
        }
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }

    public int getTime() {
        return this._time;
    }

    public Location getLoc() {
        return this._locations.get(Rnd.get(0, this._locations.size() - 1));
    }

    public Location getCurentZoneLocs() {
        return this._locations.get(RandomZoneManager.getInstance().getCurrentZone().getId());
    }

    public boolean isActive() {
        return (RandomZoneManager.getInstance().getZoneId() == getId());
    }

    private static final class KickPlayer implements Runnable {
        private Player _player;

        public KickPlayer(Player player) {
            this._player = player;
        }

        public void run() {
            if (this._player != null) {
                Summon summon = this._player.getSummon();
                if (summon != null)
                    summon.unSummon(this._player);
                this._player.teleportTo(MapRegionData.TeleportType.TOWN);
                this._player.setPvpFlag(0);
                this._player.broadcastUserInfo();
                this._player.setInsideZone(ZoneId.RANDOMZONE, false);
                this._player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
                this._player.setInsideZone(ZoneId.NO_RESTART, false);
                this._player = null;
            }
        }
    }
}
