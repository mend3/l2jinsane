/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.model.actor.FakePc;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakePcsTable implements IXmlReader {
    private static final Logger _log = Logger.getLogger(FakePcsTable.class.getName());
    private final Map<Integer, FakePc> _fakePcs = new HashMap();

    protected FakePcsTable() {
    }

    public static FakePcsTable getInstance() {
        return FakePcsTable.SingletonHolder._instance;
    }

    public void load() {
        this.parseFile("./data/xml/fake_pcs.xml");
    }

    public void reload() {
        this._fakePcs.clear();
        this.load();
    }

    public void parseDocument(Document doc, Path path) {
        try {
            Node n = doc.getFirstChild();

            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("npc")) {
                    FakePc fpc = new FakePc();
                    int npcId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());

                    for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                        if ("appearance".equalsIgnoreCase(cd.getNodeName())) {
                            fpc.name = cd.getAttributes().getNamedItem("name").getNodeValue();
                            NamedNodeMap var10001 = cd.getAttributes();
                            fpc.nameColor = Integer.decode("0x" + var10001.getNamedItem("name_color").getNodeValue());
                            fpc.title = cd.getAttributes().getNamedItem("title").getNodeValue();
                            var10001 = cd.getAttributes();
                            fpc.titleColor = Integer.decode("0x" + var10001.getNamedItem("title_color").getNodeValue());
                            fpc.radius = Integer.parseInt(cd.getAttributes().getNamedItem("radius").getNodeValue());
                            fpc.height = Integer.parseInt(cd.getAttributes().getNamedItem("height").getNodeValue());
                            fpc.race = Integer.parseInt(cd.getAttributes().getNamedItem("race").getNodeValue());
                            fpc.sex = Integer.parseInt(cd.getAttributes().getNamedItem("sex").getNodeValue());
                            fpc.classId = Integer.parseInt(cd.getAttributes().getNamedItem("class").getNodeValue());
                            fpc.hairStyle = Integer.parseInt(cd.getAttributes().getNamedItem("hair_style").getNodeValue());
                            fpc.hairColor = Integer.parseInt(cd.getAttributes().getNamedItem("hair_color").getNodeValue());
                            fpc.face = Integer.parseInt(cd.getAttributes().getNamedItem("face").getNodeValue());
                            fpc.hero = Byte.parseByte(cd.getAttributes().getNamedItem("hero").getNodeValue());
                            fpc.enchant = Integer.parseInt(cd.getAttributes().getNamedItem("enchant").getNodeValue());
                        } else if ("items".equalsIgnoreCase(cd.getNodeName())) {
                            fpc.rightHand = Integer.parseInt(cd.getAttributes().getNamedItem("right_hand").getNodeValue());
                            fpc.leftHand = Integer.parseInt(cd.getAttributes().getNamedItem("left_hand").getNodeValue());
                            fpc.chest = Integer.parseInt(cd.getAttributes().getNamedItem("chest").getNodeValue());
                            fpc.legs = Integer.parseInt(cd.getAttributes().getNamedItem("legs").getNodeValue());
                            fpc.gloves = Integer.parseInt(cd.getAttributes().getNamedItem("gloves").getNodeValue());
                            fpc.feet = Integer.parseInt(cd.getAttributes().getNamedItem("feet").getNodeValue());
                            fpc.hair = Integer.parseInt(cd.getAttributes().getNamedItem("hair").getNodeValue());
                            fpc.hair2 = Integer.parseInt(cd.getAttributes().getNamedItem("hair2").getNodeValue());
                        } else if ("clan".equalsIgnoreCase(cd.getNodeName())) {
                            fpc.clanId = Integer.parseInt(cd.getAttributes().getNamedItem("clan_id").getNodeValue());
                            fpc.clanCrest = Integer.parseInt(cd.getAttributes().getNamedItem("clan_crest").getNodeValue());
                            fpc.allyId = Integer.parseInt(cd.getAttributes().getNamedItem("ally_id").getNodeValue());
                            fpc.allyCrest = Integer.parseInt(cd.getAttributes().getNamedItem("ally_crest").getNodeValue());
                            fpc.pledge = Integer.parseInt(cd.getAttributes().getNamedItem("pledge").getNodeValue());
                        }
                    }

                    this._fakePcs.put(npcId, fpc);
                }
            }
        } catch (Exception var8) {
            _log.log(Level.WARNING, "FakePcsTable: Error loading from database:" + var8.getMessage(), var8);
        }

        _log.info("-FakePcsTable: Loaded " + this._fakePcs.size() + " NPC to PC templates.");
    }

    public FakePc getFakePc(int npcId) {
        return this._fakePcs.get(npcId);
    }

    private static class SingletonHolder {
        protected static final FakePcsTable _instance = new FakePcsTable();
    }
}