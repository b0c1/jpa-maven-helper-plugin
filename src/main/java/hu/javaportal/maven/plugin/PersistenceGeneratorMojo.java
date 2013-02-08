package hu.javaportal.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.jcabi.aether.Aether;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Collect Entity classes and extend the generated content
 *
 * @goal generate-entities
 * @phase process-test-classes
 */
public class PersistenceGeneratorMojo
        extends AbstractPluginMojo {


    /**
     * Output directory. Compiled entities.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;
    /**
     * Test files output directory. Compiled test entities.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     */
    private File testOutputDirectory;


    /**
     * Location of the persistence.xml.
     *
     * @parameter default-value="${project.build.outputDirectory}${file.separator}META-INF${file.separator}persistence.xml"
     * @required
     */
    private File persistenceXml;

    /**
     * Location of the test persistence.xml.
     *
     * @parameter default-value="${project.build.testOutputDirectory}${file.separator}META-INF${file.separator}persistence.xml"
     * @required
     */
    private File testPersistenceXml;


    /**
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession session;

    /**
     * Unit Filters
     * <p/>
     * &lt;filters&gt;
     * &lt;filter&gt;
     * &lt;name&gt;Persistence unit name&lt;/name&gt;
     * &lt;packages&gt;
     * &lt;package&gt;com.xxx&lt;/package&gt;
     * &lt;package&gt;com.yyy&lt;/package&gt;
     * &lt;/packages&gt;
     * &lt;/filter&gt;
     * &lt;/filters&gt;
     *
     * @parameter
     */
    private UnitClassFilter[] filters;


    protected Set<URL> getDependenciesUris(URL[] moreUrl) throws MojoFailureException, MalformedURLException {
        File repo = this.session.getLocalRepository().getBasedir();
        Set<URL> jars = new HashSet<URL>();
        try {
            Aether aether = new Aether(this.getProject(), repo.getAbsolutePath());
            for (Artifact a : getProject().getDependencyArtifacts()) {
                List<org.sonatype.aether.artifact.Artifact> arts = aether.resolve(new DefaultArtifact(a.getGroupId(), a.getArtifactId(), a.getType(), a.getVersion()), JavaScopes.RUNTIME);
                for (org.sonatype.aether.artifact.Artifact aa : arts) {
                    jars.add(aa.getFile().toURI().toURL());
                }
            }
        } catch (DependencyResolutionException e) {
            throw new MojoFailureException(e.getMessage());
        }
        jars.addAll(Arrays.asList(moreUrl));
        return jars;
    }

    private Map<String, Set<String>> parseAnnotatedClasses(Collection<Class<? extends Annotation>> annotations, Set<URL> urls) {

        Map<String, Set<String>> response = new HashMap<String, Set<String>>();
        ConfigurationBuilder config = new ConfigurationBuilder().setUrls(ClasspathHelper.forManifest(urls)).setScanners(new TypeAnnotationsScanner(), new TypesScanner(), new SubTypesScanner(), new ResourcesScanner());
        if (filters != null && filters.length > 0) {
            for (UnitClassFilter filter : filters) {
                config = new ConfigurationBuilder().setUrls(ClasspathHelper.forManifest(urls)).setScanners(new TypeAnnotationsScanner(), new TypesScanner(), new SubTypesScanner(), new ResourcesScanner());
                if (filter.getPackages() != null && filter.getPackages().length > 0) {
                    FilterBuilder filterBuilder = new FilterBuilder();
                    for (String pkg : filter.getPackages()) {
                        filterBuilder.include(FilterBuilder.prefix(pkg));
                    }
                    config.filterInputsBy(filterBuilder);
                }
                Reflections reflections = new Reflections(config);
                for (Class<? extends Annotation> annotation : annotations) {
                    response.put(filter.getName(), reflections.getStore().getTypesAnnotatedWith(annotation.getName()));
                }
            }
        } else {
            Reflections reflections = new Reflections(config);
            for (Class<? extends Annotation> annotation : annotations) {
                response.put("default", reflections.getStore().getTypesAnnotatedWith(annotation.getName()));
            }
        }
        return response;

    }

    public XPathHelper readDocument(File persistenceFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return new XPathHelper(builder.parse(persistenceFile));
    }


    private String queryPerisistenceUnitByName(String unitName) {
        String nameFilter = "";
        if (!"default".equalsIgnoreCase(unitName)) {
            nameFilter = "[@name='" + unitName + "']";
        }
        return "/persistence/persistence-unit" + nameFilter;
    }

    private String queryAllClassByUnitName(String unitName) {
        return queryPerisistenceUnitByName(unitName) + "/class";
    }

    private Set<String> getExistingClasses(String unitName, XPathHelper xpathHelper) throws XPathExpressionException {
        NodeList nodes = xpathHelper.xpathNodes(queryAllClassByUnitName(unitName));
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i).getTextContent());
        }
        return result;
    }

    private void writeClassNodes(String unitName, XPathHelper xPathHelper, Set<String> classList) throws XPathExpressionException {
        NodeList pus = xPathHelper.xpathNodes(queryPerisistenceUnitByName(unitName));
        for (int i = 0; i < pus.getLength(); i++) {
            Node pu = pus.item(i);
            if (pu != null) {
                for (String className : classList) {
                    getLog().debug("[" + unitName + "] add class " + className);
                    Element n = (Element) xPathHelper.getDocument().createElement("class");
                    n.setTextContent(className);
                    pu.appendChild(n);
                }
            }
        }
    }


    private void removeClassNodes(String unitName, XPathHelper xPathHelper) throws XPathExpressionException {
        NodeList nodes = xPathHelper.xpathNodes(queryAllClassByUnitName(unitName));
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            node.getParentNode().removeChild(node);
        }
    }

    public void writeDocumentToFile(File persistenceXmlFile, Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult target = new StreamResult(persistenceXmlFile);
        transformer.transform(source, target);
    }

    public void writeBackClasses(File persistenceXmlFile, XPathHelper xPathHelper, Map<String, Set<String>> classListByUnitName) throws XPathExpressionException, TransformerException {
        for (String unitName : classListByUnitName.keySet()) {
            removeClassNodes(unitName, xPathHelper);
            writeClassNodes(unitName, xPathHelper, classListByUnitName.get(unitName));
        }
        writeDocumentToFile(persistenceXmlFile, xPathHelper.getDocument());
    }

    private void replaceClasses(File persistenceXmlFile, URL... moreUrl) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException, MojoFailureException, TransformerException {
        XPathHelper xPathHelper = readDocument(persistenceXmlFile);
        List<Class<? extends Annotation>> annotations = Arrays.asList(Entity.class, MappedSuperclass.class, Embeddable.class, IdClass.class);
        Map<String, Set<String>> filteredClassesByUnitName = parseAnnotatedClasses(annotations, getDependenciesUris(moreUrl));
        getLog().debug("Persistence class filled");
        for (String unitName : filteredClassesByUnitName.keySet()) {
            getLog().debug("Inspect '" + unitName + "' persistence unit already defined classes...");
            Set<String> classList = filteredClassesByUnitName.get(unitName);
            classList.addAll(getExistingClasses(unitName, xPathHelper));
        }
        getLog().debug("Write back classes to persistence.xml");
        writeBackClasses(persistenceXmlFile, xPathHelper, filteredClassesByUnitName);
    }

    public void execute() throws MojoFailureException {
        try {
            getLog().debug("Generate class to persistence files...");
            getLog().debug("Check " + persistenceXml);

            if (persistenceXml.exists()) {
                getLog().debug(persistenceXml.toString() + " found");
                replaceClasses(persistenceXml, outputDirectory.toURI().toURL());
            }
            getLog().debug("Check " + testPersistenceXml);
            if (testPersistenceXml.exists()) {
                getLog().debug(testPersistenceXml.toString() + " found");
                replaceClasses(testPersistenceXml, outputDirectory.toURI().toURL(), testOutputDirectory.toURI().toURL());
            }
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        }

    }
}
