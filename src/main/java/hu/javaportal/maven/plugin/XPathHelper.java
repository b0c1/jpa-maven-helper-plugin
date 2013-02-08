package hu.javaportal.maven.plugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for xml parsing
 *
 * @author jhaber
 */
public class XPathHelper {

    /**
     * Iterable node list representation
     *
     * @author jhaber
     */
    public static class XPathNodeList implements Iterable<Node> {

        private final List<Node> nodes = new ArrayList<Node>();

        /**
         * Create the NodeList
         *
         * @param nodeList
         */
        public XPathNodeList(final NodeList nodeList) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                nodes.add(nodeList.item(i));
            }
        }

        /**
         * Return the node list
         *
         * @return node list
         */
        public List<Node> getNodes() {
            return nodes;
        }

        @Override
        public Iterator<Node> iterator() {
            return nodes.iterator();
        }

    }

    /**
     * Create helper instance from Document
     *
     * @param doc source document
     * @return helper instance
     */
    public static XPathHelper newInstance(final Document doc) {
        return new XPathHelper(doc);
    }

    public static XPathHelper newInstance(final InputStream data) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        return XPathHelper.newInstance(db.parse(data));
    }

    /**
     * Create helper instance from uri or xml text
     *
     * @param data      uri or xml text
     * @param isXMLData true if the data is contain xml text
     * @return helper instance
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static XPathHelper newInstance(final String data, final boolean isXMLData) throws SAXException, IOException,
            ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        if (isXMLData) {
            return XPathHelper.newInstance(db.parse(new InputSource(new StringReader(data))));
        } else {
            return XPathHelper.newInstance(db.parse(data));
        }
    }

    private final Document doc;

    private final XPathFactory factory;

    private final XPath xpath;

    /**
     * Constructor, build xpath object from document
     *
     * @param doc
     */
    private XPathHelper(final Document doc) {
        this.doc = doc;
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
    }

    /**
     * get the specified node, expression root is the document
     *
     * @param expression xpath expression
     * @return the node object
     * @throws XPathExpressionException
     */
    public Node getNode(final String expression) throws XPathExpressionException {
        return getNode(expression, doc);
    }

    /**
     * get the specified node, expression root is the parent
     *
     * @param expression xpath expression
     * @param parent     parent object (example Node which have child)
     * @return the node object
     * @throws XPathExpressionException
     */
    public Node getNode(final String expression, final Object parent) throws XPathExpressionException {
        return (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
    }

    /**
     * get the node name, if node name contain namespace prefix, remove the
     * namespace
     *
     * @param node node object
     * @return node name
     */
    public String getNodeName(final Node node) {
        String name = node.getNodeName();
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        return name;
    }

    /**
     * Return the node list, expression root is the document
     *
     * @param expression xpath expression
     * @return iterable node list
     * @throws XPathExpressionException
     */
    public XPathNodeList getNodes(final String expression) throws XPathExpressionException {
        return getNodes(expression, doc);
    }

    /**
     * Return node list, expression root is the parent object
     *
     * @param expression node list
     * @param parent     parent object (example Node which have child)
     * @return iterable node list
     * @throws XPathExpressionException
     */
    public XPathNodeList getNodes(final String expression, final Object parent) throws XPathExpressionException {
        return new XPathNodeList((NodeList) xpath.evaluate(expression, parent, XPathConstants.NODESET));
    }

    /**
     * get the node value as string, expression root is the document
     *
     * @param expression xpath expression
     * @return value as string
     * @throws XPathExpressionException
     */
    public String getString(final String expression) throws XPathExpressionException {
        return getString(expression, doc);
    }

    /**
     * get the node value as string, expression root is the parent
     *
     * @param expression xpath expression
     * @param parent     parent object (example Node which have child)
     * @return the node object
     * @throws XPathExpressionException
     */
    public String getString(final String expression, final Object parent) throws XPathExpressionException {
        return (String) xpath.evaluate(expression, parent, XPathConstants.STRING);
    }

    public Document getDoc() {
        return doc;
    }

}