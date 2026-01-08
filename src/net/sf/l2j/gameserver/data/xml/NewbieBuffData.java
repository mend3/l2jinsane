/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.NewbieBuff;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NewbieBuffData implements IXmlReader {
    private final List<NewbieBuff> _buffs = new ArrayList<>();
    private int _magicLowestLevel = 100;
    private int _physicLowestLevel = 100;
    private int _magicHighestLevel = 1;
    private int _physicHighestLevel = 1;

    protected NewbieBuffData() {
    }

    public static NewbieBuffData getInstance() {
        return NewbieBuffData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/newbieBuffs.xml");
        LOGGER.info("Loaded {} newbie buffs.", this._buffs.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "buff", (buffNode) -> {
            StatSet set = this.parseAttributes(buffNode);
            int lowerLevel = set.getInteger("lowerLevel");
            int upperLevel = set.getInteger("upperLevel");
            if (set.getBool("isMagicClass")) {
                if (lowerLevel < this._magicLowestLevel) {
                    this._magicLowestLevel = lowerLevel;
                }

                if (upperLevel > this._magicHighestLevel) {
                    this._magicHighestLevel = upperLevel;
                }
            } else {
                if (lowerLevel < this._physicLowestLevel) {
                    this._physicLowestLevel = lowerLevel;
                }

                if (upperLevel > this._physicHighestLevel) {
                    this._physicHighestLevel = upperLevel;
                }
            }

            this._buffs.add(new NewbieBuff(set));
        }));
    }

    public List<NewbieBuff> getBuffs() {
        return this._buffs;
    }

    public int getMagicHighestLevel() {
        return this._magicHighestLevel;
    }

    public int getMagicLowestLevel() {
        return this._magicLowestLevel;
    }

    public int getPhysicHighestLevel() {
        return this._physicHighestLevel;
    }

    public int getPhysicLowestLevel() {
        return this._physicLowestLevel;
    }

    private static class SingletonHolder {
        protected static final NewbieBuffData INSTANCE = new NewbieBuffData();
    }
}