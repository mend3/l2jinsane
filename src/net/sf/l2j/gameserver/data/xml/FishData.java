/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.Fish;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FishData implements IXmlReader {
    private final List<Fish> _fish = new ArrayList<>();

    protected FishData() {
    }

    public static FishData getInstance() {
        return FishData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/fish.xml");
        LOGGER.info("Loaded {} fish.", this._fish.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "fish", (fishNode) -> {
                this._fish.add(new Fish(this.parseAttributes(fishNode)));
            });
        });
    }

    public Fish getFish(int lvl, int type, int group) {
        return Rnd.get(this._fish.stream().filter((f) -> {
            return f.getLevel() == lvl && f.getType() == type && f.getGroup() == group;
        }).collect(Collectors.toList()));
    }

    private static class SingletonHolder {
        protected static final FishData INSTANCE = new FishData();
    }
}