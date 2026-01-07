/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.StaticObject;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StaticObjectData implements IXmlReader {
    private final Map<Integer, StaticObject> _objects = new HashMap();

    protected StaticObjectData() {
    }

    public static StaticObjectData getInstance() {
        return StaticObjectData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/staticObjects.xml");
        LOGGER.info("Loaded {} static objects.", this._objects.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "object", (objectNode) -> {
                StatSet set = this.parseAttributes(objectNode);
                StaticObject obj = new StaticObject(IdFactory.getInstance().getNextId());
                obj.setStaticObjectId(set.getInteger("id"));
                obj.setType(set.getInteger("type"));
                obj.setMap(set.getString("texture"), set.getInteger("mapX"), set.getInteger("mapY"));
                obj.spawnMe(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
                this._objects.put(obj.getObjectId(), obj);
            });
        });
    }

    public Collection<StaticObject> getStaticObjects() {
        return this._objects.values();
    }

    private static class SingletonHolder {
        protected static final StaticObjectData INSTANCE = new StaticObjectData();
    }
}