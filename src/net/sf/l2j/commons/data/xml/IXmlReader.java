package net.sf.l2j.commons.data.xml;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IXmlReader {
    CLogger LOGGER = new CLogger(IXmlReader.class.getName());
    String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    static boolean isNode(Node node) {
        return node.getNodeType() == 1;
    }

    static boolean isText(Node node) {
        return node.getNodeType() == 3;
    }

    void load();

    void parseDocument(Document var1, Path var2);

    default void parseFile(String path) {
        this.parseFile(Paths.get(path), false, true, true);
    }

    default void parseFile(Path path, boolean validate, boolean ignoreComments, boolean ignoreWhitespaces) {
        if (Files.isDirectory(path)) {
            final LinkedList<Path> pathsToParse = new LinkedList<>();

            try {
                Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        pathsToParse.add(file);
                        return FileVisitResult.CONTINUE;
                    }

                });
                pathsToParse.forEach((p) -> {
                    this.parseFile(p, validate, ignoreComments, ignoreWhitespaces);
                });
            } catch (IOException var9) {
                LOGGER.warn("Could not parse directory: {} ", var9, path);
            }
        } else {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(validate);
            dbf.setIgnoringComments(ignoreComments);
            dbf.setIgnoringElementContentWhitespace(ignoreWhitespaces);
            dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                db.setErrorHandler(new IXmlReader.XMLErrorHandler());
                this.parseDocument(db.parse(path.toAbsolutePath().toFile()), path);
            } catch (SAXParseException var7) {
                LOGGER.warn("Could not parse file: {} at line: {}, column: {} :", var7, path, var7.getLineNumber(), var7.getColumnNumber());
            } catch (SAXException | IOException | ParserConfigurationException var8) {
                LOGGER.warn("Could not parse file: {} ", var8, path);
            }
        }

    }

    default Boolean parseBoolean(Node node, Boolean defaultValue) {
        return node != null ? Boolean.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Boolean parseBoolean(Node node) {
        return this.parseBoolean(node, null);
    }

    default Boolean parseBoolean(NamedNodeMap attrs, String name) {
        return this.parseBoolean(attrs.getNamedItem(name));
    }

    default Boolean parseBoolean(NamedNodeMap attrs, String name, Boolean defaultValue) {
        return this.parseBoolean(attrs.getNamedItem(name), defaultValue);
    }

    default Byte parseByte(Node node, Byte defaultValue) {
        return node != null ? Byte.decode(node.getNodeValue()) : defaultValue;
    }

    default Byte parseByte(Node node) {
        return this.parseByte(node, null);
    }

    default Byte parseByte(NamedNodeMap attrs, String name) {
        return this.parseByte(attrs.getNamedItem(name));
    }

    default Byte parseByte(NamedNodeMap attrs, String name, Byte defaultValue) {
        return this.parseByte(attrs.getNamedItem(name), defaultValue);
    }

    default Short parseShort(Node node, Short defaultValue) {
        return node != null ? Short.decode(node.getNodeValue()) : defaultValue;
    }

    default Short parseShort(Node node) {
        return this.parseShort(node, null);
    }

    default Short parseShort(NamedNodeMap attrs, String name) {
        return this.parseShort(attrs.getNamedItem(name));
    }

    default Short parseShort(NamedNodeMap attrs, String name, Short defaultValue) {
        return this.parseShort(attrs.getNamedItem(name), defaultValue);
    }

    default int parseInt(Node node, Integer defaultValue) {
        return node != null ? Integer.decode(node.getNodeValue()) : defaultValue;
    }

    default int parseInt(Node node) {
        return this.parseInt(node, -1);
    }

    default Integer parseInteger(Node node, Integer defaultValue) {
        return node != null ? Integer.decode(node.getNodeValue()) : defaultValue;
    }

    default Integer parseInteger(Node node) {
        return this.parseInteger(node, null);
    }

    default Integer parseInteger(NamedNodeMap attrs, String name) {
        return this.parseInteger(attrs.getNamedItem(name));
    }

    default Integer parseInteger(NamedNodeMap attrs, String name, Integer defaultValue) {
        return this.parseInteger(attrs.getNamedItem(name), defaultValue);
    }

    default Long parseLong(Node node, Long defaultValue) {
        return node != null ? Long.decode(node.getNodeValue()) : defaultValue;
    }

    default Long parseLong(Node node) {
        return this.parseLong(node, null);
    }

    default Long parseLong(NamedNodeMap attrs, String name) {
        return this.parseLong(attrs.getNamedItem(name));
    }

    default Long parseLong(NamedNodeMap attrs, String name, Long defaultValue) {
        return this.parseLong(attrs.getNamedItem(name), defaultValue);
    }

    default Float parseFloat(Node node, Float defaultValue) {
        return node != null ? Float.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Float parseFloat(Node node) {
        return this.parseFloat(node, null);
    }

    default Float parseFloat(NamedNodeMap attrs, String name) {
        return this.parseFloat(attrs.getNamedItem(name));
    }

    default Float parseFloat(NamedNodeMap attrs, String name, Float defaultValue) {
        return this.parseFloat(attrs.getNamedItem(name), defaultValue);
    }

    default Double parseDouble(Node node, Double defaultValue) {
        return node != null ? Double.valueOf(node.getNodeValue()) : defaultValue;
    }

    default Double parseDouble(Node node) {
        return this.parseDouble(node, null);
    }

    default Double parseDouble(NamedNodeMap attrs, String name) {
        return this.parseDouble(attrs.getNamedItem(name));
    }

    default Double parseDouble(NamedNodeMap attrs, String name, Double defaultValue) {
        return this.parseDouble(attrs.getNamedItem(name), defaultValue);
    }

    default String parseString(Node node, String defaultValue) {
        return node != null ? node.getNodeValue() : defaultValue;
    }

    default String parseString(Node node) {
        return this.parseString(node, null);
    }

    default String parseString(NamedNodeMap attrs, String name) {
        return this.parseString(attrs.getNamedItem(name));
    }

    default String parseString(NamedNodeMap attrs, String name, String defaultValue) {
        return this.parseString(attrs.getNamedItem(name), defaultValue);
    }

    default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz, T defaultValue) {
        if (node == null) {
            return defaultValue;
        } else {
            try {
                return Enum.valueOf(clazz, node.getNodeValue());
            } catch (IllegalArgumentException var5) {
                LOGGER.warn("Invalid value specified for node: {} specified value: {} should be enum value of \"{}\" using default value: {}", node.getNodeName(), node.getNodeValue(), clazz.getSimpleName(), defaultValue);
                return defaultValue;
            }
        }
    }

    default <T extends Enum<T>> T parseEnum(Node node, Class<T> clazz) {
        return this.parseEnum(node, clazz, null);
    }

    default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name) {
        return this.parseEnum(attrs.getNamedItem(name), clazz);
    }

    default <T extends Enum<T>> T parseEnum(NamedNodeMap attrs, Class<T> clazz, String name, T defaultValue) {
        return this.parseEnum(attrs.getNamedItem(name), clazz, defaultValue);
    }

    default StatSet parseAttributes(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        StatSet map = new StatSet();

        for (int i = 0; i < attrs.getLength(); ++i) {
            Node att = attrs.item(i);
            map.set(att.getNodeName(), att.getNodeValue());
        }

        return map;
    }

    default void addAttributes(StatSet set, NamedNodeMap attrs) {
        for (int i = 0; i < attrs.getLength(); ++i) {
            Node att = attrs.item(i);
            set.set(att.getNodeName(), att.getNodeValue());
        }

    }

    default Map<String, Object> parseParameters(Node n) {
        Map<String, Object> parameters = new HashMap<>();

        for (Node parameters_node = n.getFirstChild(); parameters_node != null; parameters_node = parameters_node.getNextSibling()) {
            NamedNodeMap attrs = parameters_node.getAttributes();
            String var5 = parameters_node.getNodeName().toLowerCase();
            byte var6 = -1;
            switch (var5.hashCode()) {
                case 106436749:
                    if (var5.equals("param")) {
                        var6 = 0;
                    }
                    break;
                case 109496913:
                    if (var5.equals("skill")) {
                        var6 = 1;
                    }
                    break;
                case 1901043637:
                    if (var5.equals("location")) {
                        var6 = 2;
                    }
            }

            switch (var6) {
                case 0:
                    parameters.put(this.parseString(attrs, "name"), this.parseString(attrs, "value"));
                    break;
                case 1:
                    parameters.put(this.parseString(attrs, "name"), new IntIntHolder(this.parseInteger(attrs, "id"), this.parseInteger(attrs, "level")));
                    break;
                case 2:
                    parameters.put(this.parseString(attrs, "name"), new SpawnLocation(this.parseInteger(attrs, "x"), this.parseInteger(attrs, "y"), this.parseInteger(attrs, "z"), this.parseInteger(attrs, "heading", 0)));
            }
        }

        return parameters;
    }

    default Location parseLocation(Node n) {
        NamedNodeMap attrs = n.getAttributes();
        int x = this.parseInteger(attrs, "x");
        int y = this.parseInteger(attrs, "y");
        int z = this.parseInteger(attrs, "z");
        return new Location(x, y, z);
    }

    default SpawnLocation parseSpawnLocation(Node n) {
        NamedNodeMap attrs = n.getAttributes();
        int x = this.parseInteger(attrs, "x");
        int y = this.parseInteger(attrs, "y");
        int z = this.parseInteger(attrs, "z");
        int heading = this.parseInteger(attrs, "heading", 0);
        return new SpawnLocation(x, y, z, heading);
    }

    default SpawnLocation parseSpawnLocation(NamedNodeMap attrs, String name) {
        final Node node = attrs.getNamedItem(name);
        if (node == null)
            return null;

        final int[] pos = Arrays.stream(node.getNodeValue().split(";")).mapToInt(Integer::parseInt).toArray();
        return new SpawnLocation(pos[0], pos[1], pos[2], pos[3]);
    }

    default void forEach(Node node, Consumer<Node> action) {
        this.forEach(node, (a) -> {
            return true;
        }, action);
    }

    default void forEach(Node node, String nodeName, Consumer<Node> action) {
        this.forEach(node, (innerNode) -> {
            if (nodeName.contains("|")) {
                String[] nodeNames = nodeName.split("\\|");
                String[] var3 = nodeNames;
                int var4 = nodeNames.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    String name = var3[var5];
                    if (!name.isEmpty() && name.equals(innerNode.getNodeName())) {
                        return true;
                    }
                }

                return false;
            } else {
                return nodeName.equals(innerNode.getNodeName());
            }
        }, action);
    }

    default void forEach(Node node, Predicate<Node> filter, Consumer<Node> action) {
        NodeList list = node.getChildNodes();

        for (int i = 0; i < list.getLength(); ++i) {
            Node targetNode = list.item(i);
            if (filter.test(targetNode)) {
                action.accept(targetNode);
            }
        }

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