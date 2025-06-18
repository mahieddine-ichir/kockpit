package com.kockpit.rules.codegen.plugin;

public abstract class BaseClassNameFormatter<T> {

    final ClassNameSanitizer classNameSanitizer = new ClassNameSanitizer();

    abstract String formatClassName(T arg0);

    protected String firstUpper(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
