package toolkit.services;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class TraceListener {

    private static final ThreadLocal<ServiceSecurityContext> CONTEXT_THREAD_LOCAL = new ThreadLocal<ServiceSecurityContext>();
    private static final ThreadLocal<Integer> NESTING_LEVEL_THREAD_LOCAL = new ThreadLocal<Integer>();

    public static boolean contextInitialized() {
        return CONTEXT_THREAD_LOCAL.get() != null;
    }

    public static void initContext(ServiceSecurityContext context) {
        CONTEXT_THREAD_LOCAL.set(context);
    }

    public static void exit() {
        Integer nestingLevel = NESTING_LEVEL_THREAD_LOCAL.get();
        int value = nestingLevel == null ? 0 : nestingLevel;
        if (value == 1) {
            CONTEXT_THREAD_LOCAL.set(null);
            NESTING_LEVEL_THREAD_LOCAL.set(null);
        } else {
            NESTING_LEVEL_THREAD_LOCAL.set(value - 1);
        }
    }

    public static void enter() {
        Integer nestingLevel = NESTING_LEVEL_THREAD_LOCAL.get();
        int value = nestingLevel == null ? 0 : nestingLevel;
        NESTING_LEVEL_THREAD_LOCAL.set(value + 1);
    }

    @PrePersist
    public void onPersist(Object entity) {
        ServiceSecurityContext context = CONTEXT_THREAD_LOCAL.get();
        if (context != null) {
            AbstractTraceableEntity traceableEntity = (AbstractTraceableEntity) entity;
            traceableEntity.setPersistUsername(context.getUsername());
            traceableEntity.setUpdateUsername(context.getUsername());
            Date now = new Date(System.currentTimeMillis());
            traceableEntity.setPersistTimestamp(now);
            traceableEntity.setUpdateTimestamp(now);
        }
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        ServiceSecurityContext context = CONTEXT_THREAD_LOCAL.get();
        if (context != null) {
            AbstractTraceableEntity traceableEntity = (AbstractTraceableEntity) entity;
            traceableEntity.setUpdateUsername(context.getUsername());
            Date now = new Date(System.currentTimeMillis());
            traceableEntity.setUpdateTimestamp(now);
        }
    }
}
