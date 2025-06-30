package org.kockpit.rules.executor;

import org.kockpit.rules.action.RuleContext;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
class RuleContextImpl implements RuleContext {
    private final Map<Object, Object> context;
    @Override
    public Object getAttribute(String name) {
        return context.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        context.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        context.remove(name);
    }
}
