package org.kockpit.rules.action;

public interface RuleContext {
    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    void removeAttribute(String name);
}
