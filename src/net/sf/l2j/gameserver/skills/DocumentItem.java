/**/
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class DocumentItem extends DocumentBase {
    private final List<Item> _itemsInFile = new ArrayList();
    private NewItem _currentItem;

    public DocumentItem(File file) {
        super(file);
    }

    protected StatSet getStatsSet() {
        return this._currentItem.set;
    }

    protected String getTableValue(String name) {
        return ((String[]) this._tables.get(name))[this._currentItem.currentLevel];
    }

    protected String getTableValue(String name, int idx) {
        return ((String[]) this._tables.get(name))[idx - 1];
    }

    protected void parseDocument(Document doc) {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("item".equalsIgnoreCase(d.getNodeName())) {
                        try {
                            this._currentItem = new NewItem();
                            this.parseItem(d);
                            this._itemsInFile.add(this._currentItem.item);
                            this.resetTable();
                        } catch (Exception e) {
                            _log.log(Level.WARNING, "Cannot create item " + this._currentItem.id, e);
                        }
                    }
                }
            }
        }

    }

    protected void parseItem(Node n) throws InvocationTargetException {
        int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        String className = n.getAttributes().getNamedItem("type").getNodeValue();
        String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
        this._currentItem.id = itemId;
        this._currentItem.name = itemName;
        this._currentItem.type = className;
        this._currentItem.set = new StatSet();
        this._currentItem.set.set("item_id", itemId);
        this._currentItem.set.set("name", itemName);
        Node first = n.getFirstChild();

        for (Node var10 = first; var10 != null; var10 = var10.getNextSibling()) {
            if ("table".equalsIgnoreCase(var10.getNodeName())) {
                if (this._currentItem.item != null) {
                    throw new IllegalStateException("Item created but table node found! Item " + itemId);
                }

                this.parseTable(var10);
            } else if ("set".equalsIgnoreCase(var10.getNodeName())) {
                if (this._currentItem.item != null) {
                    throw new IllegalStateException("Item created but set node found! Item " + itemId);
                }

                this.parseBeanSet(var10, this._currentItem.set, 1);
            } else if ("for".equalsIgnoreCase(var10.getNodeName())) {
                this.makeItem();
                this.parseTemplate(var10, this._currentItem.item);
            } else if ("cond".equalsIgnoreCase(var10.getNodeName())) {
                this.makeItem();
                Condition condition = this.parseCondition(var10.getFirstChild(), this._currentItem.item);
                Node msg = var10.getAttributes().getNamedItem("msg");
                Node msgId = var10.getAttributes().getNamedItem("msgId");
                if (condition != null && msg != null) {
                    condition.setMessage(msg.getNodeValue());
                } else if (condition != null && msgId != null) {
                    condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
                    Node addName = var10.getAttributes().getNamedItem("addName");
                    if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                        condition.addName();
                    }
                }

                this._currentItem.item.attach(condition);
            }
        }

        this.makeItem();
    }

    private void makeItem() throws InvocationTargetException {
        if (this._currentItem.item == null) {
            try {
                Constructor<?> c = Class.forName("net.sf.l2j.gameserver.model.item.kind." + this._currentItem.type).getConstructor(StatSet.class);
                this._currentItem.item = (Item) c.newInstance(this._currentItem.set);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    public List<Item> getItemList() {
        return this._itemsInFile;
    }

    public class NewItem {
        public int id;
        public String type;
        public String name;
        public StatSet set;
        public int currentLevel;
        public Item item;
    }
}
