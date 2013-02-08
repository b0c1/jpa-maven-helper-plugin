package hu.javaportal.maven.plugin;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XPathHelper {
    private final Document doc;
    private final XPathFactory factory;
    private final XPath xpath;

    public XPathHelper(Document doc) {
        this.doc = doc;
        this.factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
    }

    public NodeList xpathNodes(String p) throws XPathExpressionException {
        return (NodeList) xpath.compile(p).evaluate(doc, XPathConstants.NODESET);
    }

    public String xpathString(String p) throws XPathExpressionException {
        return (String) xpath.compile(p).evaluate(doc, XPathConstants.STRING);
    }

    public Node xpathNode(String p) throws XPathExpressionException {
        return (Node) xpath.compile(p).evaluate(doc, XPathConstants.NODE);
    }

    public Document getDocument() {
        return doc;
    }
}