package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.handler.admincommandhandlers.*;

import java.util.HashMap;
import java.util.Map;

public class AdminCommandHandler {
    private final Map<Integer, IAdminCommandHandler> _entries = new HashMap<>();

    protected AdminCommandHandler() {
        registerHandler(new AdminAdmin());
        registerHandler(new AdminAnnouncements());
        registerHandler(new AdminBalancer());
        registerHandler(new AdminBan());
        registerHandler(new AdminBanHWID());
        registerHandler(new AdminBookmark());
        registerHandler(new AdminBuffs());
        registerHandler(new AdminCamera());
        registerHandler(new AdminClanHall());
        registerHandler(new AdminCreateItem());
        registerHandler(new AdminCursedWeapons());
        registerHandler(new AdminDelete());
        registerHandler(new AdminDoorControl());
        registerHandler(new AdminDungeon());
        registerHandler(new AdminDungeonReload());
        registerHandler(new AdminEditChar());
        registerHandler(new AdminEditNpc());
        registerHandler(new AdminEffects());
        registerHandler(new AdminEnchant());
        registerHandler(new AdminExpSp());
        registerHandler(new AdminGeoEngine());
        registerHandler(new AdminGm());
        registerHandler(new AdminGmChat());
        registerHandler(new AdminHeal());
        registerHandler(new AdminHelpPage());
        registerHandler(new AdminKick());
        registerHandler(new AdminKnownlist());
        registerHandler(new AdminLevel());
        registerHandler(new AdminMaintenance());
        registerHandler(new AdminMammon());
        registerHandler(new AdminManor());
        registerHandler(new AdminMenu());
        registerHandler(new AdminMovieMaker());
        registerHandler(new AdminOlympiad());
        registerHandler(new AdminPartyFarm());
        registerHandler(new AdminPetition());
        registerHandler(new AdminPForge());
        registerHandler(new AdminPledge());
        registerHandler(new AdminPolymorph());
        registerHandler(new AdminRes());
        registerHandler(new AdminRideWyvern());
        registerHandler(new AdminSearch());
        registerHandler(new AdminShop());
        registerHandler(new AdminSiege());
        registerHandler(new AdminSkill());
        registerHandler(new AdminSpawn());
        registerHandler(new AdminStatus());
        registerHandler(new AdminTarget());
        registerHandler(new AdminTeleport());
        registerHandler(new AdminTournament());
        registerHandler(new AdminZone());
        registerHandler(new AdminZoneCreation());
    }

    public static AdminCommandHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void registerHandler(IAdminCommandHandler handler) {
        for (String id : handler.getAdminCommandList())
            this._entries.put(Integer.valueOf(id.hashCode()), handler);
    }

    public IAdminCommandHandler getHandler(String adminCommand) {
        String command = adminCommand;
        if (adminCommand.indexOf(" ") != -1)
            command = adminCommand.substring(0, adminCommand.indexOf(" "));
        return this._entries.get(Integer.valueOf(command.hashCode()));
    }

    public int size() {
        return this._entries.size();
    }

    private static class SingletonHolder {
        protected static final AdminCommandHandler INSTANCE = new AdminCommandHandler();
    }
}
