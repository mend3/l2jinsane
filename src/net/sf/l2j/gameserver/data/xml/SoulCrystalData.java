/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.soulcrystal.LevelingInfo;
import net.sf.l2j.gameserver.model.soulcrystal.SoulCrystal;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SoulCrystalData implements IXmlReader {
    private final Map<Integer, SoulCrystal> _soulCrystals = new HashMap<>();
    private final Map<Integer, LevelingInfo> _levelingInfos = new HashMap<>();

    protected SoulCrystalData() {
    }

    public static SoulCrystalData getInstance() {
        return SoulCrystalData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/soulCrystals.xml");
        LOGGER.info("Loaded {} Soul Crystals data and {} NPCs data.", this._soulCrystals.size(), this._levelingInfos.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "crystals", (crystalsNode) -> {
                this.forEach(crystalsNode, "crystal", (crystalNode) -> {
                    StatSet set = this.parseAttributes(crystalNode);
                    this._soulCrystals.put(set.getInteger("initial"), new SoulCrystal(set));
                });
            });
            this.forEach(listNode, "npcs", (npcsNode) -> {
                this.forEach(npcsNode, "npc", (npcNode) -> {
                    StatSet set = this.parseAttributes(npcNode);
                    this._levelingInfos.put(set.getInteger("id"), new LevelingInfo(set));
                });
            });
        });
    }

    public final Map<Integer, SoulCrystal> getSoulCrystals() {
        return this._soulCrystals;
    }

    public final Map<Integer, LevelingInfo> getLevelingInfos() {
        return this._levelingInfos;
    }

    private static class SingletonHolder {
        protected static final SoulCrystalData INSTANCE = new SoulCrystalData();
    }
}