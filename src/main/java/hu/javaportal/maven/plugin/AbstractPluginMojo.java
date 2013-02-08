package hu.javaportal.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import java.io.File;

public abstract class AbstractPluginMojo extends AbstractMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    protected File buildDirectory;

    protected MavenProject getProject() {
        if (project.getExecutionProject() != null) {
            return project.getExecutionProject();
        }

        return project;
    }
}
