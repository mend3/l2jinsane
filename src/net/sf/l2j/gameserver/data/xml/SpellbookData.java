/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SpellbookData implements IXmlReader {
    private final Map<Integer, Integer> _books = new HashMap();

    protected SpellbookData() {
    }

    public static SpellbookData getInstance() {
        return SpellbookData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/spellbooks.xml");
        LOGGER.info("Loaded {} spellbooks.", this._books.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "book", (bookNode) -> {
                NamedNodeMap attrs = bookNode.getAttributes();
                this._books.put(this.parseInteger(attrs, "skillId"), this.parseInteger(attrs, "itemId"));
            });
        });
    }

    public int getBookForSkill(int skillId, int level) {
        if (skillId == 1405) {
            if (!Config.DIVINE_SP_BOOK_NEEDED) {
                return 0;
            } else {
                switch (level) {
                    case 1:
                        return 8618;
                    case 2:
                        return 8619;
                    case 3:
                        return 8620;
                    case 4:
                        return 8621;
                    default:
                        return 0;
                }
            }
        } else if (level != 1) {
            return 0;
        } else if (!Config.SP_BOOK_NEEDED) {
            return 0;
        } else {
            return !this._books.containsKey(skillId) ? 0 : this._books.get(skillId);
        }
    }

    private static class SingletonHolder {
        protected static final SpellbookData INSTANCE = new SpellbookData();
    }
}