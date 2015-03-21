package toolkit.services;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public abstract class TraceTransactionalInterceptor extends TransactionalInterceptor {
    @Inject
    private ServiceContext context;

    @Override
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        TraceListener.enter();
        try {
            if (!TraceListener.contextInitialized()) {
                TraceListener.initContext(context);
            }
            return super.aroundInvoke(ic);
        } finally {
            TraceListener.exit();
        }
    }

}