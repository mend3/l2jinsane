package mods.xml;

import net.sf.l2j.commons.util.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

public interface IXmlReader {
    Logger LOG = Logger.getLogger(IXmlReader.class.getName());

    String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    XMLFilter XML_FILTER = new XMLFilter();

    void load();

    default void parseDatapackFile(String path) {
        parseFile(new File(".", path));
    }

    default void parseFile(File f) {
        if (!getCurrentFileFilter().accept(f)) {
            LOG.warning("{}: Could not parse {} is not a file or it doesn't exist!");
            return;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        try {
            dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new XMLErrorHandler());
            parseDocument(db.parse(f), f);
        } catch (SAXParseException e) {
            LOG.warning("{}: Could not parse file {} at line {}, column {}");
        } catch (Exception e) {
            LOG.warning("{}: Could not parse file {}");
        }
    }

    default boolean parseDirectory(File file) {
        return parseDirectory(file, false);
    }

    default boolean parseDirectory(String path) {
        return parseDirectory(new File(path), false);
    }

    default boolean parseDirectory(String path, boolean recursive) {
        return parseDirectory(new File(path), recursive);
    }

    default boolean parseDirectory(File dir, boolean recursive) {
        if (!dir.exists()) {
            LOG.warning("{}: Folder {} doesn't exist!");
            return false;
        }
        File[] files = dir.listFiles();
        if (files != null)
            for (File f : files) {
                if (recursive && f.isDirectory()) {
                    parseDirectory(f, recursive);
                } else if (getCurrentFileFilter().accept(f)) {
                    parseFile(f);
                }
            }
        return true;
    }

    default boolean parseDatapackDirectory(String path, boolean recursive) {
        return parseDirectory(new File(".", path), recursive);
    }

    default void parseDocument(Document doc, File f) {
        parseDocument(doc);
    }

    default void parseDocument(Document doc) {
        LOG.warning("{}: Parser not implemented!");
    }

    default Boolean parseBoolean(Node node, Boolean defaultValue) {
        return (node != null) ? Boolean.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Boolean parseBoolean(Node node) {
        return parseBoolean(node, null);
    }

    default Boolean parseBoolean(NamedNodeMap attrs, String name) {
        return parseBoolean(attrs.getNamedItem(name));
    }

    default Boolean parseBoolean(NamedNodeMap attrs, String name, Boolean defaultValue) {
        return parseBoolean(attrs.getNamedItem(name), defaultValue);
    }

    default Byte parseByte(Node node, Byte defaultValue) {
        return (node != null) ? Byte.valueOf(node.getNodeValue()) : defaultValue;
    }

    default StatSet parseAttributes(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        StatSet map = new StatSet();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node att = attrs.item(i);
            map.set(att.getNodeName(), att.getNodeValue());
        }
        return map;
    }

    default Byte parseByte(Node node) {
        return parseByte(node, null);
    }

    default Byte parseByte(NamedNodeMap attrs, String name) {
        return parseByte(attrs.getNamedItem(name));
    }

    default Byte parseByte(NamedNodeMap attrs, String name, Byte defaultValue) {
        return parseByte(attrs.getNamedItem(name), defaultValue);
    }

    default Short parseShort(Node node, Short defaultValue) {
        return (node != null) ? Short.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Short parseShort(Node node) {
        return parseShort(node, null);
    }

    default Short parseShort(NamedNodeMap attrs, String name) {
        return parseShort(attrs.getNamedItem(name));
    }

    default Short parseShort(NamedNodeMap attrs, String name, Short defaultValue) {
        return parseShort(attrs.getNamedItem(name), defaultValue);
    }

    default int parseInt(Node node, Integer defaultValue) {
        return (node != null) ? Integer.parseInt(node.getNodeValue()) : defaultValue;
    }

    default int parseInt(Node node) {
        return parseInt(node, -1);
    }

    default Integer parseInteger(Node node, Integer defaultValue) {
        return (node != null) ? Integer.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Integer parseInteger(Node node) {
        return parseInteger(node, null);
    }

    default Integer parseInteger(NamedNodeMap attrs, String name) {
        return parseInteger(attrs.getNamedItem(name));
    }

    default Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue) {
        return parseInteger(attrs.getNamedItem(name), defaultValue);
    }

    default Long parseLong(Node node, Long defaultValue) {
        return (node != null) ? Long.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Long parseLong(Node node) {
        return parseLong(node, null);
    }

    default Long parseLong(NamedNodeMap attrs, String name) {
        return parseLong(attrs.getNamedItem(name));
    }

    default Long parseLong(NamedNodeMap attrs, String name, Long defaultValue) {
        return parseLong(attrs.getNamedItem(name), defaultValue);
    }

    default Float parseFloat(Node node, Float defaultValue) {
        return (node != null) ? Float.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Float parseFloat(Node node) {
        return parseFloat(node, null);
    }

    default Float parseFloat(NamedNodeMap attrs, String name) {
        return parseFloat(attrs.getNamedItem(name));
    }

    default Float parseFloat(NamedNodeMap attrs, String name, Float defaultValue) {
        return parseFloat(attrs.getNamedItem(name), defaultValue);
    }

    default Double parseDouble(Node node, Double defaultValue) {
        return (node != null) ? Double.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Double parseDouble(Node node) {
        return parseDouble(node, null);
    }

    default Double parseDouble(NamedNodeMap attrs, String name) {
        return parseDouble(attrs.getNamedItem(name));
    }

    default Double parseDouble(NamedNodeMap attrs, String name, Double defaultValue) {
        return parseDouble(attrs.getNamedItem(name), defaultValue);
    }

    default String parseString(Node node, String defaultValue) {
        return (node != null) ? node.getNodeValue() : defaultValue;
    }

    default String parseString(Node node) {
        return parseString(node, null);
    }

    default String parseString(NamedNodeMap attrs, String name) {
        return parseString(attrs.getNamedItem(name));
    }

    default String parseString(NamedNodeMap attrs, String name, String defaultValue) {
        return parseString(attrs.getNamedItem(name), defaultValue);
    }

    default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz, T defaultValue) {
        if (node == null)
            return defaultValue;
        try {
            return Enum.valueOf(clazz, node.getNodeValue());
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid value specified for node: {} specified value: {} should be enum value of \"{}\" using default value: {}");
            return defaultValue;
        }
    }

    default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz) {
        return parseEnum(node, clazz, null);
    }

    default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name) {
        return parseEnum(attrs.getNamedItem(name), clazz);
    }

    default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name, T defaultValue) {
        return parseEnum(attrs.getNamedItem(name), clazz, defaultValue);
    }

    default FileFilter getCurrentFileFilter() {
        return XML_FILTER;
    }

    class XMLErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXParseException {
            throw e;
        }

        public void error(SAXParseException e) throws SAXParseException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXParseException {
            throw e;
        }
    }
}
