package org.kockpit.rules.codegen.plugin;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CodeGenMojoTest {

    @Test
    void execute() {
        CodeGenMojo codeGenMojo = new CodeGenMojo();
        codeGenMojo.setInputSpec("src/test/resources/rule-flow-example.json");
        codeGenMojo.setPackageName("com.kockpit.rules");

        Build build = Mockito.mock(Build.class);
        Mockito.when(build.getDirectory()).thenReturn("target");

        MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getBuild()).thenReturn(build);


        codeGenMojo.setProject(project);
        codeGenMojo.execute();
    }

}