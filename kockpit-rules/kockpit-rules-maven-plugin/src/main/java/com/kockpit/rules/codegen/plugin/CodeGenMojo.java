package com.kockpit.rules.codegen.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.kockpit.rules.codegen.plugin.model.RuleDefinition;
import com.kockpit.rules.codegen.plugin.model.Step;
import com.kockpit.rules.codegen.plugin.rendering.Rendered;
import com.kockpit.rules.codegen.plugin.rendering.StepRendererFactory;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.time.Instant;
import java.util.*;

@Setter
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class CodeGenMojo extends AbstractMojo {

    static final String TARGET_GENERATED_SOURCES_KENGINE_SRC_MAIN_JAVA = "/generated-sources/kockpit/src/main/java";

    @Parameter(name = "inputSpec", property = "rule-engine.generator.inputSpec")
    protected String inputSpec;

    @Parameter(name = "packageName", property = "rule-engine.generator.packageName")
    protected String packageName;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    // default output directory
    private String outputDirectory = TARGET_GENERATED_SOURCES_KENGINE_SRC_MAIN_JAVA;

    private final StepClassNameFormatter stepClassNameFormatter = new StepClassNameFormatter();
    private final RuleClassNameFormatter ruleClassNameFormatter = new RuleClassNameFormatter();
    private final StepRendererFactory stepRendererFactory = new StepRendererFactory();

    @SneakyThrows
    @Override
    public void execute() {
        if (isBlank(inputSpec)) {
            throw new MojoExecutionException("inputSpec must be specified");
        }
        if (isBlank(packageName)) {
            throw new MojoExecutionException("package must be specified");
        }
        Arrays.stream(inputSpec.split(","))
                .map(String::trim)
                .map(File::new)
                .map(file -> {
                    getLog().info("Reading file "+ file.getAbsolutePath());
                    return load(file);
                })
                .forEach(ruleDefinition -> {
                    MustacheFactory mf = new DefaultMustacheFactory();
                    writeSteps(ruleDefinition.getSteps(), mf, ruleDefinition, packageName);
                });
    }

    @SneakyThrows
    private RuleDefinition load(File file) {
        return new ObjectMapper().readValue(file, RuleDefinition.class);
    }

    private boolean isBlank(String inputSpec) {
        return inputSpec == null || inputSpec.trim().isEmpty();
    }

    @SneakyThrows
    void writeSteps(Collection<Step> steps, MustacheFactory mf, RuleDefinition ruleDefinition, String packageName) {
        if (steps == null || steps.isEmpty()) {
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("package", packageName);
        context.put("import", packageName);

        // render DSL
        context.put("rendered", this.render(steps));
        // prepare injections
        Set<Injection> injections = new HashSet<>();
        manageInjections(steps, injections, mf);
        context.put("injections", injections);

        // write parent rule
        writeRule(mf, ruleDefinition, context);
    }

    private void manageInjections(Collection<Step> steps, Set<Injection> injections, MustacheFactory mf) {
        if (steps == null || steps.isEmpty()) {
            return;
        }
        for (Step step : steps) {
            Injection injection = toInjection(step);
            if (! injections.contains(injection)) {
                injections.add(injection);
                writeStep(mf, step);
            }
            manageInjections(step.get_true(), injections, mf);
            manageInjections(step.get_false(), injections, mf);
        }
    }

    private Injection toInjection(Step step) {
        String className = stepClassNameFormatter.formatClassName(step);
        String name = firstLower(className);
        String description = getStepDescription(step);
        return new Injection(className, name, description);
    }

    private List<Rendered> render(Collection<Step> steps) {
        return steps.stream().map(this::render).toList();
    }

    private Rendered render(Step step) {
        return new Rendered(stepRendererFactory.getRenderer(step).render(step));
    }

    @SneakyThrows
    void writeRule(MustacheFactory mf, RuleDefinition ruleDefinition, Map<String, Object> context) {
        ClassesWriter writer;
        if (ruleDefinition.getClassName() == null) {
            String className = ruleClassNameFormatter.formatClassName(ruleDefinition);
            String filename = className + ".java";
            writer = new ClassesWriter(project, filename, packageName, outputDirectory);
            context.put("className", className);
        } else {
            String qualifiedName = ruleDefinition.getClassName();
            String className = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
            String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

            String filename = className + ".java";
            writer = new ClassesWriter(project, filename, packageName, outputDirectory);
            context.put("className", className);
            context.put("package", packageName);
        }
        context.put("name", ruleDefinition.getName());
        context.put("description", getRuleDescription(ruleDefinition));

        // generated metadata
        //2024-11-22T09:39:13+0100
        context.put("generatedDate", Instant.now());
        context.put("generatedComments", "version: 1.0.0-RC1, compiler: javac, environment: Java vX TODO");

        Mustache m = mf.compile("Rule.mustache");
        m.execute(writer, List.of(ruleDefinition, context));
        writer.close();
    }

    void writeStep(MustacheFactory mf, Step step) {
        switch (step.getType()) {
            case "predicate" -> this.writeRulePredicate(mf, step);
            case "action" -> this.writeRuleAction(mf, step);
            default -> throw new IllegalStateException("Unexpected value: " + step.getType());
        };
    }

    @SneakyThrows
    void writeRuleAction(MustacheFactory mf, Step step) {
        String className = stepClassNameFormatter.formatClassName(step);
        Mustache m = mf.compile("RuleAction.mustache");
        ClassesWriter writer = new ClassesWriter(project, "%s.java".formatted(className), packageName, outputDirectory);
        m.execute(writer, List.of(step, Map.of("package", packageName, "className", className)));
        writer.close();
    }

    @SneakyThrows
    void writeRulePredicate(MustacheFactory mf, Step step) {
        String className = stepClassNameFormatter.formatClassName(step);
        Mustache m = mf.compile("RulePredicate.mustache");
        ClassesWriter writer = new ClassesWriter(project, "%s.java".formatted(className), packageName, outputDirectory);
        m.execute(writer, List.of(step, Map.of("package", packageName, "className", className)));
        writer.close();
    }

    private String firstLower(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    private String getRuleDescription(RuleDefinition ruleDefinition) {
        return Objects.isNull(ruleDefinition.getDescription()) || ruleDefinition.getDescription().isBlank() ?
                        ruleDefinition.getName() : ruleDefinition.getDescription();
    }

    private String getStepDescription(Step step) {
        return Objects.isNull(step.getDescription()) || step.getDescription().isBlank() ?
                        step.getName() : step.getDescription();
    }
}