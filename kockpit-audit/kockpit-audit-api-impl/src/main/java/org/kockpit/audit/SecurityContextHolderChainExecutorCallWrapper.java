package org.kockpit.audit;

/*

public class SecurityContextHolderChainExecutorCallWrapper implements ChainExecutorCallWrapper {

    @Override
    public void initContext(Map<Object, Object> context) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        context.put(SecurityContext.class, securityContext);
    }

    @Override
    public void beforeExecution(Map<Object, Object> context) {
        SecurityContextHolder.setContext((SecurityContext) context.get(SecurityContext.class));
    }

    @Override
    public void releaseAfterExecution(Map<Object, Object> context) {
        SecurityContextHolder.clearContext();
    }
}
*/
