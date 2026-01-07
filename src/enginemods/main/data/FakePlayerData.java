package enginemods.main.data;

import enginemods.main.holders.PlayerHolder;
import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.random.Rnd;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FakePlayerData {
    private static final CLogger LOGGER = new CLogger(FakePlayerData.class.getName());
    private static final List<String> _fakesNames = new ArrayList<>();
    private static final List<String> _fakesTitles = new ArrayList<>();
    private static final List<String> _fakesClanNames = new ArrayList<>();
    private static int _contNames = 0;

    public static void load() {
        _fakesNames.clear();
        _fakesTitles.clear();
        _fakesClanNames.clear();
        loadFakeNumbers();
        loadNames();
        loadTitles();
        loadClansNames();
    }

    private static void loadFakeNumbers() {
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            if (ph.isFake())
                _contNames++;
        }
    }

    private static void loadClansNames() {
        try {
            File f = new File("./data/xml/engine/fakes/fakesClanNames.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("fake")) {
                    NamedNodeMap attrs = d.getAttributes();
                    String name = attrs.getNamedItem("clan").getNodeValue();
                    _fakesClanNames.add(name);
                }
            }
            LOGGER.info("FakePlayerData: Load " + _fakesClanNames.size() + " clans names.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadNames() {
        try {
            File f = new File("./data/xml/engine/fakes/fakesNames.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("fake")) {
                    NamedNodeMap attrs = d.getAttributes();
                    String name = attrs.getNamedItem("name").getNodeValue();
                    _fakesNames.add(name);
                }
            }
            LOGGER.info("FakePlayerData: Load " + _fakesNames.size() + " names.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadTitles() {
        try {
            File f = new File("./data/xml/engine/fakes/fakesTitles.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equalsIgnoreCase("fake")) {
                    NamedNodeMap attrs = d.getAttributes();
                    String titles = attrs.getNamedItem("title").getNodeValue();
                    _fakesTitles.add(titles);
                }
            }
            LOGGER.info("FakePlayerData: Load " + _fakesTitles.size() + " titles.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClanName() {
        return _fakesClanNames.get(Rnd.get(0, _fakesClanNames.size() - 1));
    }

    public static String getTitle() {
        return _fakesTitles.get(Rnd.get(0, _fakesTitles.size() - 1));
    }

    public static String getName() {
        if (_contNames > _fakesNames.size()) {
            LOGGER.info("FakePlayerData: Se supero el numero maximo de fakes");
            _contNames = 0;
        }
        String name = _fakesNames.get(_contNames);
        _contNames++;
        return name;
    }
}
