package mods.xml;

import net.sf.l2j.commons.util.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class XMLDocument {
    protected static final Logger LOG = Logger.getLogger(XMLDocument.class.getName());
    private static final DocumentBuilderFactory BUILDER = DocumentBuilderFactory.newInstance();

    static {
        BUILDER.setValidating(false);
        BUILDER.setIgnoringComments(true);
    }

    protected Document document;

    public static void parseAndFeed(NamedNodeMap attrs, StatSet set) {
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            set.set(attr.getNodeName(), attr.getNodeValue());
        }
    }

    public void loadDocument(String filePath) {
        loadDocument(new File(filePath));
    }

    public void writeDocument(Document doc, String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);
            LOG.info("XML file saved to " + fileName);
        } catch (TransformerException e) {
            LOG.warning("Error saving XML file: " + e.getMessage());
        }
    }

    public void loadDocument(File file) {
        if (!file.exists()) {
            LOG.severe("The following file or directory doesn't exist: " + file.getName());
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles())
                loadDocument(f);
        } else if (file.isFile()) {
            try {
                parseDocument(BUILDER.newDocumentBuilder().parse(file), file);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error loading XML file " + file.getName(), e);
            }
        }
    }

    public Document getDocument() {
        return this.document;
    }

    protected abstract void load();

    protected abstract void parseDocument(Document paramDocument, File paramFile);
}
