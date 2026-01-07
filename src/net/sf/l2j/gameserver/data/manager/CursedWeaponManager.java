package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.CursedWeapon;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CursedWeaponManager implements IXmlReader {
    private final Map<Integer, CursedWeapon> _cursedWeapons = new HashMap<>();

    public CursedWeaponManager() {
    }

    public static CursedWeaponManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        if (!Config.ALLOW_CURSED_WEAPONS) {
            LOGGER.info("Cursed weapons loading is skipped.");
            return;
        }
        parseFile("./data/xml/cursedWeapons.xml");
        LOGGER.info("Loaded {} cursed weapons.", this._cursedWeapons.size());
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "item", itemNode ->
        {
            final StatSet set = parseAttributes(itemNode);
            _cursedWeapons.put(set.getInteger("id"), new CursedWeapon(set));
        }));
    }

    public void reload() {
        for (CursedWeapon cw : this._cursedWeapons.values())
            cw.endOfLife();
        this._cursedWeapons.clear();
        load();
    }

    public boolean isCursed(int itemId) {
        return this._cursedWeapons.containsKey(itemId);
    }

    public Collection<CursedWeapon> getCursedWeapons() {
        return this._cursedWeapons.values();
    }

    public Set<Integer> getCursedWeaponsIds() {
        return this._cursedWeapons.keySet();
    }

    public CursedWeapon getCursedWeapon(int itemId) {
        return this._cursedWeapons.get(itemId);
    }

    public synchronized void checkDrop(Attackable attackable, Player player) {
        if (attackable instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeGuard || attackable instanceof net.sf.l2j.gameserver.model.actor.instance.RiftInvader || attackable instanceof net.sf.l2j.gameserver.model.actor.instance.FestivalMonster || attackable instanceof net.sf.l2j.gameserver.model.actor.instance.GrandBoss || attackable instanceof net.sf.l2j.gameserver.model.actor.instance.FeedableBeast)
            return;
        for (CursedWeapon cw : this._cursedWeapons.values()) {
            if (cw.isActive())
                continue;
            if (cw.checkDrop(attackable, player))
                break;
        }
    }

    public void activate(Player player, ItemInstance item) {
        CursedWeapon cw = this._cursedWeapons.get(item.getItemId());
        if (cw == null)
            return;
        if (player.isCursedWeaponEquipped()) {
            this._cursedWeapons.get(player.getCursedWeaponEquippedId()).rankUp();
            cw.setPlayer(player);
            cw.endOfLife();
        } else {
            cw.activate(player, item);
        }
    }

    public void drop(int itemId, Creature killer) {
        CursedWeapon cw = this._cursedWeapons.get(itemId);
        if (cw == null)
            return;
        cw.dropIt(killer);
    }

    public void increaseKills(int itemId) {
        CursedWeapon cw = this._cursedWeapons.get(itemId);
        if (cw == null)
            return;
        cw.increaseKills();
    }

    public int getCurrentStage(int itemId) {
        CursedWeapon cw = this._cursedWeapons.get(itemId);
        return (cw == null) ? 0 : cw.getCurrentStage();
    }

    public void checkPlayer(Player player) {
        if (player == null)
            return;
        for (CursedWeapon cw : this._cursedWeapons.values()) {
            if (cw.isActivated() && player.getObjectId() == cw.getPlayerId()) {
                cw.setPlayer(player);
                cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
                cw.giveDemonicSkills();
                player.setCursedWeaponEquippedId(cw.getItemId());
                break;
            }
        }
    }

    private static class SingletonHolder {
        protected static final CursedWeaponManager INSTANCE = new CursedWeaponManager();
    }
}
