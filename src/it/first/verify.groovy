import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


def testClassNumber(file, puName) {
    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    doc = builder.parse(file)
    def xpath = XPathFactory.newInstance().newXPath()

    nodes = xpath.evaluate('/persistence/persistence-unit[@name=\'' + puName + '\']/class', doc, XPathConstants.NODESET)
    return nodes.getLength()
}

assert testClassNumber(new File(basedir, 'first-it/target/test-classes/META-INF/persistence.xml'), 'test') == 4
assert testClassNumber(new File(basedir, 'first-it/target/classes/META-INF/persistence.xml'), 'test1') == 3
assert testClassNumber(new File(basedir, 'first-it/target/classes/META-INF/persistence.xml'), 'test2') == 1