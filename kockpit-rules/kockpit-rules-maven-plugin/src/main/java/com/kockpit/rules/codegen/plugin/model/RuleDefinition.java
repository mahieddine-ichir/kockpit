package com.kockpit.rules.codegen.plugin.model;

import lombok.Data;

import java.util.List;

@Data
public class RuleDefinition {

    private String name;

    private String description;

    private String className;

    private List<Step> steps;
}
