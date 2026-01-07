package enginemods.main.xmlfactory;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public final class XMLDocumentFactory {
    private final DocumentBuilder _builder;

    private XMLDocumentFactory() throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            this._builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new Exception("Failed initializing", e);
        }
    }

    public static XMLDocumentFactory getInstance() {
        return SingletonHolder._instance;
    }

    public Document loadDocument(String filePath) throws Exception {
        return loadDocument(new File(filePath));
    }

    public Document loadDocument(File file) throws Exception {
        if (!file.exists() || !file.isFile())
            throw new Exception("File: " + file.getAbsolutePath() + " doesn't exist and/or is not a file.");
        return this._builder.parse(file);
    }

    public Document newDocument() {
        return this._builder.newDocument();
    }

    private static class SingletonHolder {
        protected static final XMLDocumentFactory _instance;

        static {
            try {
                _instance = new XMLDocumentFactory();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}
