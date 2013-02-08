# JPA Maven Helper Plugin

This maven plugin helps multi module JPA development.

## Generate entities

The generate-entities goal inspect all @Entity annotated class in classpath and dependent libraries and add to the persistence.xml (both main and test resources).
The test resources presistence.xml contain all class plus the test classpath entities.

### Configuration

#### Collect all dependencies and add it to all persistence-unit in persistence.xml (both main and test)

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>hu.javaportal.maven.plugin</groupId>
                <artifactId>jpa-maven-helper-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-entities</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

#### Collect Filtering example
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>hu.javaportal.maven.plugin</groupId>
                <artifactId>jpa-maven-helper-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-entities</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <filters>
                        <!-- If one or more filter exists, you need to write all persistence unit name -->
                        <!-- "persistence_unit_name_1" named persistence unit contains all found entity -->
                        <filter>
                            <name>persistence_unit_name_1</name>
                        </filter>

                        <!-- "persistence_unit_name_2" named persistence unit contains only com.xxx and com.yyy package units -->
                        <filter>
                            <name>persistence_unit_name_2</name>
                            <packages>
                                <package>com.xxx</package>
                                <package>com.yyy</package>
                            </packages>
                        </filter>
                    </filters>
                </configuration>
            </plugin>
        </plugins>
    </build>
```