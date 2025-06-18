package com.kockpit.rules.codegen.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClassesWriter extends Writer {

    private Log log;

    public Log getLog() {
        if (this.log == null) {
            this.log = new SystemStreamLog();
        }
        return this.log;
    }

    private final ByteArrayOutputStream outputStream;

    private final MavenProject project;

    private final String filename;

    private final String packageName;

    private final String targetDirectory;

    public ClassesWriter(MavenProject mavenProject, String filename, String packageName, String targetDirectory) {
        this.project = mavenProject;
        this.filename = filename;
        this.packageName = packageName;
        this.targetDirectory = targetDirectory;
        outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        outputStream.write(new String(cbuf, off, len).getBytes(Charset.defaultCharset()));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
        String[] folders = packageName.split("\\.");
        File file = Paths.get(project.getBuild().getDirectory() + targetDirectory, folders).toFile();
        if (!file.exists() && !file.mkdirs()) {
            getLog().error("Failed to create directory " + file.getAbsolutePath());
        }
        Path path = Paths.get(file.getPath(), filename);
        Path generatedClass = Files.write(path, outputStream.toByteArray(), StandardOpenOption.CREATE);
        //project.addCompileSourceRoot(targetDirectory);
        project.addCompileSourceRoot(file.getPath());
        getLog().debug("+ Generated class "+generatedClass);
    }
}
