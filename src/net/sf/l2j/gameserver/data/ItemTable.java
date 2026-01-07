package net.sf.l2j.gameserver.data;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.skills.DocumentItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ItemTable {
    protected static final ItemTable _instance = new ItemTable();
    private static final Logger _log = Logger.getLogger(ItemTable.class.getName());
    private final Map<Integer, Armor> _armors = new HashMap<>();
    private final Map<Integer, EtcItem> _etcItems = new HashMap<>();
    private final Map<Integer, Weapon> _weapons = new HashMap<>();
    private Item[] _allTemplates;

    private ItemTable() {
    }

    public static ItemTable getInstance() {
        return _instance;
    }

    private static void hashFiles(String dirname, List<File> hash) {
        File dir = new File("./data/xml/" + dirname);
        if (!dir.isDirectory()) {
            _log.config("Dir " + dir.getAbsolutePath() + " doesn't exist.");
            return;
        }
        File[] files = dir.listFiles();
        for (File fItems : files) {
            if (fItems.getName().endsWith(".xml"))
                hash.add(fItems);
        }
    }

    public void load() {
        List<File> files = new ArrayList<>();
        hashFiles("items", files);
        hashFiles("items/accessories", files);
        hashFiles("items/armors", files);
        hashFiles("items/weapons", files);
        hashFiles("items/jewels", files);
        hashFiles("items/etcitems", files);
        int highest = 0;
        for (File file : files) {
            DocumentItem document = new DocumentItem(file);
            document.parse();
            for (Item item : document.getItemList()) {
                if (item == null) {
                    continue;
                }
                if (highest < item.getItemId())
                    highest = item.getItemId();
                if (item instanceof EtcItem) {
                    _etcItems.put(item.getItemId(), (EtcItem) item);
                    continue;
                }
                if (item instanceof Armor) {
                    _armors.put(item.getItemId(), (Armor) item);
                    continue;
                }
                _weapons.put(item.getItemId(), (Weapon) item);
            }
        }
        _log.info("ItemTable: Highest used itemID : " + highest);
        this._allTemplates = new Item[highest + 1];
        for (Armor item : _armors.values())
            this._allTemplates[item.getItemId()] = item;
        for (Weapon item : _weapons.values())
            this._allTemplates[item.getItemId()] = item;
        for (EtcItem item : _etcItems.values())
            this._allTemplates[item.getItemId()] = item;
    }

    public Item getTemplate(int id) {
        if (id >= this._allTemplates.length)
            return null;
        return this._allTemplates[id];
    }

    public Item[] getAllItems() {
        return this._allTemplates;
    }

    public void reload() {
        _armors.clear();
        _etcItems.clear();
        _weapons.clear();
        load();
    }

    public ItemInstance createDummyItem(int itemId) {
        Item item = getTemplate(itemId);
        if (item == null)
            return null;
        return new ItemInstance(0, item);
    }

}
