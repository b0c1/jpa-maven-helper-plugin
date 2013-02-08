import hu.javaportal.maven.plugin.XPathHelper;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class XPathTest {
    @Test
    public void relativePathTest() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        XPathHelper helper = XPathHelper.newInstance(getClass().getResourceAsStream("/META-INF/persistence.xml"));
        Node rootNode = helper.getNode("/persistence/persistence-unit[@name='test']");
        Node relativeNode = helper.getNode("provider", rootNode);
        Assert.assertNotNull(relativeNode);
        Assert.assertEquals(relativeNode.getTextContent(), "org.hibernate.ejb.HibernatePersistence");
    }
}